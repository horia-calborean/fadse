#!/usr/bin/env python3
"""Parse gem5 stats.txt into FADSE objective files."""

import argparse
import json
import math
from fnmatch import fnmatch
from pathlib import Path

from gem5_contract import cost_proxy
from generated_gem5_contract import SUPPORTED_OBJECTIVES
from result_store import atomic_json, atomic_text, file_lock

WORST_OBJECTIVE = 1.7976931348623157e308

SUPPORTED_PROPERTIES = tuple(SUPPORTED_OBJECTIVES)

PROPERTY_ALIASES = {
    "simTicks": "sim_ticks",
    "simSeconds": "sim_seconds",
    "hostSeconds": "host_seconds",
    "simInsts": "sim_insts",
}


def published_values(result):
    """Return every metric/objective name that write_properties can publish."""
    values = dict(result.get("metrics", {}))
    values.update(result.get("objectives", {}))
    for alias, source in PROPERTY_ALIASES.items():
        if source in values:
            values[alias] = values[source]
    return values

CYCLE_COUNTERS = [
    "system.cpu.numCycles",
    "system.cpu.commitStats0.numCycles",
    "system.cpu.commit.numCycles",
]

INSTRUCTION_COUNTERS = [
    "simInsts",
    "sim_insts",
    "system.cpu.commitStats0.numInsts",
    "system.cpu.committedInsts",
    "system.cpu.commit.committedInsts",
    "system.cpu.numInsts",
    "system.cpu.exec_context.thread_0.numInsts",
]

CPI_COUNTERS = [
    "system.cpu.cpi",
    "system.cpu.cpi_total",
]

IPC_COUNTERS = [
    "system.cpu.ipc",
    "system.cpu.ipc_total",
]

L2_MISS_COUNTERS = [
    "system.l2.overallMisses::total",
    "system.l2.demandMisses::total",
    "system.l2cache.overallMisses::total",
    "system.l2cache.demandMisses::total",
    "system.ruby.l2_cntrl*.L2cache.m_demand_misses",
    "system.l2.overall_misses::total",
    "system.l2.overallMisses",
    "system.l2.demandMisses",
]


def parse_stats_file(stats_path):
    dumps = []
    current = None
    framed = False
    with Path(stats_path).open(encoding="utf-8", errors="replace") as handle:
        for line in handle:
            stripped = line.strip()
            if stripped.startswith("---------- Begin Simulation Statistics"):
                framed = True
                current = {}
                continue
            if stripped.startswith("---------- End Simulation Statistics"):
                framed = True
                if current is not None:
                    dumps.append(current)
                current = None
                continue
            if current is None or not stripped or stripped.startswith("#"):
                continue
            tokens = stripped.split()
            if len(tokens) < 2:
                continue
            name, value = tokens[0], tokens[1]
            try:
                current[name] = float(value)
            except ValueError:
                # Preserve presence: malformed counters are not suppressed zeros.
                current[name] = float("nan")

    if dumps:
        return dumps[-1], len(dumps)
    if framed:
        return {}, 0

    fallback = {}
    with Path(stats_path).open(encoding="utf-8", errors="replace") as handle:
        for line in handle:
            tokens = line.strip().split()
            if len(tokens) < 2:
                continue
            try:
                fallback[tokens[0]] = float(tokens[1])
            except ValueError:
                fallback[tokens[0]] = float("nan")
    return fallback, 1 if fallback else 0


def first_available(stats, names):
    for name in names:
        if "*" in name:
            total = 0.0
            matched = []
            for stat_name, stat_value in stats.items():
                if fnmatch(stat_name, name) and math.isfinite(stat_value):
                    matched.append(stat_name)
                    total += stat_value
            if matched:
                return "+".join(matched), total
        else:
            value = stats.get(name)
            if value is not None and math.isfinite(value):
                return name, value
    return None, None


def build_result(stats, dump_count, architecture=None, status="ok"):
    mapping = {}
    metrics = {}

    cycles_name, cycles = first_available(stats, CYCLE_COUNTERS)
    instructions_name, instructions = first_available(stats, INSTRUCTION_COUNTERS)
    cpi_name, cpi_counter = first_available(stats, CPI_COUNTERS)
    ipc_name, ipc = first_available(stats, IPC_COUNTERS)
    l2_misses_name, l2_misses = first_available(stats, L2_MISS_COUNTERS)
    invalid_l2 = any(
        (not math.isfinite(value) or value < 0)
        for name, value in stats.items()
        if any(fnmatch(name, pattern) for pattern in L2_MISS_COUNTERS)
    )
    if invalid_l2:
        l2_misses_name, l2_misses = None, None
        if status == "ok":
            status = "failed_invalid_l2_counter"
    if not invalid_l2 and l2_misses is None and any(name.startswith(("system.l2.", "system.l2cache.")) for name in stats):
        # gem5 suppresses all-zero vector stats after m5.stats.reset(); a
        # measured window with zero L2 misses therefore emits no miss counter
        # at all even though the L2 exists. Treat it as zero misses, not a
        # parser failure.
        l2_misses_name, l2_misses = "zero_suppressed_l2_misses", 0.0

    sim_seconds_name, sim_seconds = first_available(stats, ["simSeconds", "sim_seconds"])
    sim_ticks_name, sim_ticks = first_available(stats, ["simTicks", "sim_ticks"])
    host_seconds_name, host_seconds = first_available(stats, ["hostSeconds", "host_seconds"])

    if sim_seconds_name:
        mapping["sim_seconds"] = sim_seconds_name
        metrics["sim_seconds"] = sim_seconds
    if sim_ticks_name:
        mapping["sim_ticks"] = sim_ticks_name
        metrics["sim_ticks"] = sim_ticks
    if host_seconds_name:
        mapping["host_seconds"] = host_seconds_name
        metrics["host_seconds"] = host_seconds
    if cycles_name:
        mapping["cycles"] = cycles_name
        metrics["cycles"] = cycles
    if instructions_name:
        mapping["committed_instructions"] = instructions_name
        metrics["committed_instructions"] = instructions
        metrics["sim_insts"] = instructions
    if ipc_name:
        mapping["ipc"] = ipc_name
        metrics["ipc"] = ipc
    if l2_misses_name:
        mapping["l2_misses"] = l2_misses_name
        metrics["l2_misses"] = l2_misses

    cpi = None
    if cycles is not None and instructions and instructions > 0:
        cpi = cycles / instructions
        mapping["cpi"] = "cycles/committed_instructions"
    elif cpi_counter is not None:
        cpi = cpi_counter
        mapping["cpi"] = cpi_name
    elif ipc is not None and ipc > 0:
        cpi = 1.0 / ipc
        mapping["cpi"] = "1/" + ipc_name

    if ipc is None and cpi is not None and cpi > 0:
        ipc = 1.0 / cpi
        metrics["ipc"] = ipc
        mapping["ipc"] = "1/cpi"

    l2_mpki_pos = None
    if l2_misses is not None and instructions and instructions > 0:
        metrics["l2_mpki"] = 1000.0 * l2_misses / instructions
        l2_mpki_pos = 1000.0 * l2_misses / instructions + 1e-9
        mapping["l2_mpki_pos"] = "1000*l2_misses/committed_instructions+1e-9"

    proxy = cost_proxy(architecture or {})
    objectives = {
        "cpi": cpi,
        "l2_mpki_pos": l2_mpki_pos,
        "cost_proxy": proxy,
    }

    if status == "ok":
        if cpi is None:
            status = "failed_missing_cpi"
        elif l2_mpki_pos is None:
            status = "failed_missing_l2_counter"
        elif proxy is None:
            status = "failed_missing_objective"

    return {
        "status": status,
        "dump_count": dump_count,
        "metrics": metrics,
        "objectives": objectives,
        "counter_mapping": mapping,
    }


def write_outputs(result, properties_path, json_path):
    json_path = Path(json_path)
    json_path.parent.mkdir(parents=True, exist_ok=True)
    atomic_json(json_path, result)
    # Publish the properties marker last, after the complete result JSON.
    write_properties(result, properties_path)


def write_properties(result, properties_path):
    failed = result["status"] != "ok"
    lines = [f"status = {result['status']}\n"]
    values = published_values(result)
    for name in SUPPORTED_PROPERTIES:
        value = values.get(name)
        if failed:
            value = WORST_OBJECTIVE
        elif value is None or not math.isfinite(value):
            continue
        lines.append(f"{name} = {value}\n")
    atomic_text(properties_path, "".join(lines))


def append_stat_mapping(stat_mapping_path, stats_path, result):
    if not stat_mapping_path:
        return
    stat_mapping_path = Path(stat_mapping_path)
    stat_mapping_path.parent.mkdir(parents=True, exist_ok=True)
    with file_lock(str(stat_mapping_path) + ".lock"):
        if stat_mapping_path.exists():
            with stat_mapping_path.open(encoding="utf-8") as handle:
                payload = json.load(handle)
        else:
            payload = {}
        payload[str(stats_path)] = result.get("counter_mapping", {})
        atomic_json(stat_mapping_path, payload)


def load_architecture(candidate_json):
    if not candidate_json:
        return {}
    with Path(candidate_json).open(encoding="utf-8") as handle:
        candidate = json.load(handle)
    return candidate.get("architecture", {})


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("stats_path")
    parser.add_argument("--candidate-json")
    parser.add_argument("--properties-out", required=True)
    parser.add_argument("--json-out", required=True)
    parser.add_argument("--stat-mapping-out", default="results/metadata/stat_mapping.json")
    args = parser.parse_args()

    stats, dump_count = parse_stats_file(args.stats_path)
    architecture = load_architecture(args.candidate_json)
    status = "ok" if stats else "failed_missing_stats"
    result = build_result(stats, dump_count, architecture, status=status)
    write_outputs(result, args.properties_out, args.json_out)
    append_stat_mapping(args.stat_mapping_out, args.stats_path, result)
    return 0 if result["status"] == "ok" else 2


if __name__ == "__main__":
    raise SystemExit(main())
