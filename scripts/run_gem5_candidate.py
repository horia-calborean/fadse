#!/usr/bin/env python3
"""Run one FADSE gem5 candidate and emit FADSE result files."""

import argparse
import hashlib
import json
import math
import shutil
import subprocess
import sys
import time
from pathlib import Path

GEM5_CONFIGS_DIR = Path(__file__).resolve().parents[1] / "gem5_configs"
if str(GEM5_CONFIGS_DIR) not in sys.path:
    runner_dir = str(Path(__file__).resolve().parent)
    runner_index = sys.path.index(runner_dir) if runner_dir in sys.path else 0
    sys.path.insert(runner_index + 1, str(GEM5_CONFIGS_DIR))

from candidate_hash import candidate_id, simulation_window
from dram_geometry import (
    DramGeometryError,
    MemorySizeFormatError,
    parse_capacity_bytes,
    ranks_for_capacity,
)
from gem5_contract import (
    DISCRETE_VALUES,
    OPTIONAL_DISCRETE_VALUES,
    validate_and_repair_architecture,
)
from generated_gem5_contract import CONTRACT_VERSION, SUPPORTED_OBJECTIVES
from result_store import append_csv, atomic_json, atomic_text, file_lock, running_simulator, termination_signals, terminate_simulator

from parse_gem5_stats import (
    WORST_OBJECTIVE,
    append_stat_mapping,
    build_result,
    parse_stats_file,
    published_values,
    write_outputs,
    write_properties,
)

EVALUATED_COLUMNS = [
    "candidate_id", "method", "benchmark", "seed", "run_id", "record_type", "budget_index",
    "status", "cache_hit", "width", "rob_entries", "iq_entries", "lsq_entries", "l1i_kb",
    "l1d_kb", "l2_kb", "l2_assoc", "l3_kb", "l3_assoc", "bp_global_size",
    "btb_entries", "ras_size", "prefetcher", "prefetch_degree",
    "cpi", "ipc", "l2_mpki", "l2_mpki_pos",
    "cost_proxy", "host_seconds", "sim_seconds", "stats_path", "config_ini_path",
    "is_verified", "is_final_pareto",
]

REPAIR_COLUMNS = [
    "dedupe_key", "run_id", "candidate_id", "original_config_json",
    "repaired_config_json", "repair_reason",
]


def load_candidate(path):
    with Path(path).open(encoding="utf-8") as handle:
        return json.load(handle)


def command_from_candidate(candidate, isolated_workdirs=None):
    gem5 = candidate["gem5"]
    output_dir = Path(gem5["output_dir"])
    output_dir.mkdir(parents=True, exist_ok=True)

    benchmark_binary = gem5.get("benchmark_binary") or candidate.get("benchmark")
    benchmark_name = gem5.get("benchmark_name") or candidate.get("benchmark") or Path(str(benchmark_binary)).name
    benchmark_args = gem5.get("benchmark_args", "")

    command = [
        gem5["gem5_binary"],
        "--outdir=" + str(output_dir),
        "--stats-file=stats.txt",
        gem5["config_script"],
        "--binary=" + str(benchmark_binary),
        "--benchmark-name=" + str(benchmark_name),
    ]

    if benchmark_args:
        command.append("--benchmark-args=" + str(benchmark_args))

    run_cwd = gem5.get("run_cwd")
    if run_cwd and gem5.get("isolate_cwd"):
        isolated_workdir = (output_dir / "workdir").resolve()
        if isolated_workdirs is not None:
            isolated_workdirs.append(isolated_workdir)
        run_cwd = prepare_isolated_cwd(Path(run_cwd), isolated_workdir)
    if run_cwd:
        command.append("--cwd=" + str(run_cwd))
    if gem5.get("stdin"):
        command.append("--stdin=" + str(gem5["stdin"]))
    if gem5.get("environment"):
        command.append("--env-json=" + json.dumps(gem5["environment"], sort_keys=True))

    for key, value in candidate.get("architecture", {}).items():
        command.append("--" + key.replace("_", "-") + "=" + str(value))

    warmup, measured = simulation_window(gem5)
    command.append("--warmup-insts=" + str(warmup))
    command.append("--maxinsts=" + str(measured))
    command.append("--cacheline-size=" + str(gem5.get("cacheline_size", 64)))
    command.append("--mem-size=" + str(gem5.get("mem_size", "16GB")))

    return command


def prepare_isolated_cwd(source, destination):
    """Copy a SPEC run directory into a candidate-private work directory.

    gem5 candidates write into their private output directory, preventing
    concurrent candidates from colliding on outputs or modifying shared inputs.
    Symlinks are dereferenced so writes cannot escape through a link into the
    shared SPEC tree.
    """
    source = source.resolve()
    if not source.is_dir():
        raise FileNotFoundError(f"gem5 run cwd does not exist: {source}")
    destination = destination.parent.resolve() / destination.name
    if (
        destination == source
        or destination in source.parents
        or source in destination.parents
    ):
        raise ValueError(
            f"isolated work directory must not contain or equal its source: {destination}"
        )
    # A retry must start clean. Unlink a stale workdir symlink rather than
    # following it, then remove only the candidate-private destination.
    remove_isolated_cwd(destination)
    shutil.copytree(source, destination, symlinks=False)
    return destination.resolve()


def remove_isolated_cwd(path):
    """Remove only the candidate-private worktree created by prepare_isolated_cwd."""
    path = Path(path)
    if path.is_symlink():
        path.unlink()
    elif path.exists():
        if path.is_dir():
            shutil.rmtree(path)
        else:
            path.unlink()


def validate_and_repair_candidate(candidate):
    architecture, original, repairs, invalid_reasons = validate_and_repair_architecture(
        candidate.get("architecture", {})
    )
    declared_repairs = [
        repair.get("reason") for repair in candidate.get("repairs", [])
        if isinstance(repair, dict) and repair.get("reason")
    ]
    if isinstance(candidate.get("original_architecture"), dict):
        original = candidate["original_architecture"]
    repairs = declared_repairs + repairs
    if candidate.get("contract_version", CONTRACT_VERSION) != CONTRACT_VERSION:
        invalid_reasons.append("unsupported_contract_version")
    objectives = candidate.get("objectives")
    if objectives is not None:
        if (not isinstance(objectives, list) or not objectives
                or any(not isinstance(name, str) for name in objectives)):
            invalid_reasons.append("objectives_invalid")
        else:
            invalid_reasons.extend(
                f"unsupported_objective_{name}"
                for name in objectives if name not in SUPPORTED_OBJECTIVES
            )
    memory_reason = validate_memory_size(candidate.get("gem5", {}).get("mem_size", "16GB"))
    if memory_reason:
        invalid_reasons.append(memory_reason)
    repaired_candidate = dict(candidate)
    repaired_candidate["architecture"] = architecture
    return repaired_candidate, original, repairs, invalid_reasons


def validate_memory_size(value):
    """Return an invalid reason unless memory maps to a power-of-two rank count."""
    try:
        ranks_for_capacity(parse_capacity_bytes(value))
    except MemorySizeFormatError:
        return "mem_size_invalid_format"
    except DramGeometryError:
        return "mem_size_incompatible_with_dram_geometry"
    return None


def ensure_requested_objectives(result, candidate):
    """Turn missing configured metrics into an attributable runner failure."""
    requested = list(dict.fromkeys(candidate.get("objectives") or SUPPORTED_OBJECTIVES))
    if result.get("status") != "ok":
        result["objectives"] = {name: WORST_OBJECTIVE for name in requested}
        return result
    if not candidate.get("objectives"):
        return result
    values = published_values(result)
    missing = [
        name for name in candidate["objectives"]
        if name not in values or not isinstance(values[name], (int, float))
        or not math.isfinite(values[name])
    ]
    if missing:
        result["status"] = "failed_missing_objective"
        result["missing_objectives"] = missing
        result["objectives"] = {name: WORST_OBJECTIVE for name in requested}
    return result


def append_repair_log(path, run_id, candidate_id, original, repaired, repairs):
    if not path or not repairs:
        return
    normalized_run_id = "" if run_id is None else str(run_id)
    normalized_candidate_id = "" if candidate_id is None else str(candidate_id)
    rows = [{
        "dedupe_key": hashlib.sha256(
            (normalized_run_id + "\0" + normalized_candidate_id + "\0"
             + ("" if reason is None else str(reason))).encode("utf-8")
        ).hexdigest(),
        "run_id": normalized_run_id,
        "candidate_id": normalized_candidate_id,
        "original_config_json": json.dumps(original, sort_keys=True),
        "repaired_config_json": json.dumps(repaired, sort_keys=True),
        "repair_reason": reason,
    } for reason in repairs]
    append_csv(path, REPAIR_COLUMNS, rows, unique_key="dedupe_key")


def failure_result(status, candidate):
    return {
        "status": status,
        "dump_count": 0,
        "metrics": {},
        "objectives": {},
        "counter_mapping": {},
    }


def attach_candidate_context(result, candidate, error=None):
    gem5 = candidate.get("gem5", {})
    result["candidate_id"] = candidate.get("candidate_id", "")
    result["method"] = candidate.get("method", "")
    result["benchmark"] = candidate.get("benchmark", "")
    result["seed"] = candidate.get("seed", "")
    result["output_dir"] = gem5.get("output_dir", "")
    result["error"] = error
    result.setdefault("metrics", {})
    result.setdefault("objectives", {})
    return result


def append_evaluated_csv(path, candidate, result, budget_index, command=None, host_seconds=None):
    if not path:
        return
    architecture = candidate.get("architecture", {})
    objectives = result.get("objectives", {})
    metrics = result.get("metrics", {})
    output_dir = Path(candidate.get("gem5", {}).get("output_dir", ""))
    status = result.get("status", "")
    append_csv(path, EVALUATED_COLUMNS, [{
        "candidate_id": candidate.get("candidate_id", ""),
        "method": candidate.get("method", ""),
        "benchmark": candidate.get("benchmark", ""),
        "seed": candidate.get("seed", ""),
        "run_id": candidate.get("run_id", ""),
        "record_type": "workload",
        "budget_index": budget_index,
        "status": status,
        "cache_hit": str(bool(result.get("cache_hit", False))).lower(),
        "width": architecture.get("width", ""),
        "rob_entries": architecture.get("rob_entries", ""),
        "iq_entries": architecture.get("iq_entries", ""),
        "lsq_entries": architecture.get("lsq_entries", ""),
        "l1i_kb": architecture.get("l1i_kb", ""),
        "l1d_kb": architecture.get("l1d_kb", ""),
        "l2_kb": architecture.get("l2_kb", ""),
        "l2_assoc": architecture.get("l2_assoc", ""),
        "l3_kb": architecture.get("l3_kb", ""),
        "l3_assoc": architecture.get("l3_assoc", ""),
        "bp_global_size": architecture.get("bp_global_size", ""),
        "btb_entries": architecture.get("btb_entries", ""),
        "ras_size": architecture.get("ras_size", ""),
        "prefetcher": architecture.get("prefetcher", ""),
        "prefetch_degree": architecture.get("prefetch_degree", ""),
        "cpi": objectives.get("cpi", ""),
        "ipc": metrics.get("ipc", ""),
        "l2_mpki": metrics.get("l2_mpki", ""),
        "l2_mpki_pos": objectives.get("l2_mpki_pos", ""),
        "cost_proxy": objectives.get("cost_proxy", ""),
        "host_seconds": host_seconds if host_seconds is not None else metrics.get("host_seconds", ""),
        "sim_seconds": metrics.get("sim_seconds", ""),
        "stats_path": str(output_dir / "stats.txt") if output_dir else "",
        "config_ini_path": str(output_dir / "config.ini") if output_dir else "",
        "is_verified": str(status == "ok").lower(),
        "is_final_pareto": "false",
    }])


def existing_success(properties_path):
    if not properties_path.exists():
        return False
    with properties_path.open(encoding="utf-8", errors="replace") as handle:
        for line in handle:
            if "=" not in line:
                continue
            key, value = line.split("=", 1)
            if key.strip() == "status":
                return value.strip() == "ok"
    return False


def read_text_tail(path, max_chars=4000):
    """Read a bounded UTF-8 tail without loading the complete simulator log."""
    path = Path(path)
    if not path.exists():
        return ""
    with path.open("rb") as handle:
        handle.seek(0, 2)
        handle.seek(max(0, handle.tell() - max_chars * 4))
        return handle.read().decode("utf-8", errors="replace")[-max_chars:]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--candidate-json", required=True)
    parser.add_argument("--result-json", required=True)
    parser.add_argument("--response-properties", help="Unique result snapshot owned by this request")
    parser.add_argument("--evaluated-csv")
    parser.add_argument("--repairs-csv", default="results/datasets/candidate_repairs.csv")
    parser.add_argument("--stat-mapping-out", default="results/metadata/stat_mapping.json")
    parser.add_argument("--budget-index", default="")
    parser.add_argument("--force", action="store_true")
    args = parser.parse_args()

    candidate = load_candidate(args.candidate_json)
    candidate, original_architecture, repairs, invalid_reasons = validate_and_repair_candidate(candidate)
    outputs = candidate.get("outputs", {})
    result_json = Path(args.result_json)
    result_properties = Path(outputs.get(
        "result_properties",
        result_json.with_name("fadse_result.properties"),
    ))

    output_dir = Path(candidate["gem5"]["output_dir"])
    timeout = int(candidate["gem5"].get("timeout_seconds", 3600))
    if timeout <= 0:
        raise ValueError("timeout_seconds must be positive")
    deadline = time.monotonic() + timeout
    with termination_signals():
        try:
            with file_lock(output_dir / ".candidate.lock", deadline):
                return run_locked(
                    args, candidate, original_architecture, repairs, invalid_reasons,
                    result_json, result_properties, deadline,
                )
        except TimeoutError as exc:
            # Another request owns the candidate: do not overwrite its files.
            print(str(exc), file=sys.stderr)
            return 124


def run_locked(args, candidate, original_architecture, repairs, invalid_reasons,
               result_json, result_properties, deadline):
    output_dir = Path(candidate["gem5"]["output_dir"])
    fingerprint = candidate_id(candidate)
    atomic_json(output_dir / "candidate.json", candidate)

    def log_repairs_once():
        marker_key = "\0".join((
            str(candidate.get("run_id", "")),
            str(Path(args.repairs_csv).resolve()) if args.repairs_csv else "",
        ))
        marker = output_dir / (
            ".repairs_logged-" + hashlib.sha256(marker_key.encode("utf-8")).hexdigest()[:16]
        )
        if marker.exists() or not repairs or not args.repairs_csv:
            return
        append_repair_log(
            args.repairs_csv, candidate.get("run_id", ""),
            candidate.get("candidate_id", ""), original_architecture,
            candidate.get("architecture", {}), repairs,
        )
        atomic_text(marker, "logged\n")

    def publish(result, cache_hit=False):
        ensure_requested_objectives(result, candidate)
        attach_candidate_context(result, candidate, None if result["status"] == "ok" else result["status"])
        result["fingerprint"] = fingerprint
        result["run_id"] = candidate.get("run_id", "")
        result["cache_hit"] = cache_hit
        if not cache_hit:
            write_outputs(result, result_properties, result_json)
        run_id = candidate.get("run_id")
        if run_id:
            atomic_json(output_dir / "runs" / (run_id + ".json"), result)
        budget = args.budget_index
        ledger = candidate["gem5"].get("budget_file")
        if ledger:
            budget = json.loads(Path(ledger).read_text())["used"]
        append_evaluated_csv(args.evaluated_csv, candidate, result, budget)
        if args.response_properties:
            # Snapshot under the candidate lock, including cache hits. A request
            # that never acquires the lock must not own a response file.
            write_properties(result, args.response_properties)
        return 0 if result["status"] == "ok" else (124 if result["status"] == "timeout" else 2)

    log_repairs_once()
    if invalid_reasons:
        result = failure_result("invalid_pre_gem5", candidate)
        result["invalid_reasons"] = invalid_reasons
        return publish(result)
    if not args.force and existing_success(result_properties) and result_json.exists():
        try:
            cached = json.loads(result_json.read_text())
        except (OSError, ValueError):
            cached = {}
        if cached.get("fingerprint") == fingerprint and cached.get("status") == "ok":
            return publish(cached, cache_hit=True)

    # Remove the success marker before a retry can fail during setup or launch.
    result_properties.unlink(missing_ok=True)
    # A retry must never parse the preceding attempt's statistics/window marker.
    for name in ("stats.txt", "measurement.json"):
        (output_dir / name).unlink(missing_ok=True)
    started = time.monotonic()
    command = []
    stdout_path = output_dir / "gem5.stdout.log"
    stderr_path = output_dir / "gem5.stderr.log"
    launch_error = ""
    isolated_workdirs = []
    status = "failed_launch"
    try:
        command = command_from_candidate(candidate, isolated_workdirs)
        with stdout_path.open("w", encoding="utf-8") as stdout_log, \
                stderr_path.open("w", encoding="utf-8") as stderr_log:
            with running_simulator(
                    command, candidate, deadline, stdout=stdout_log, stderr=stderr_log
            ) as process:
                if process is None:
                    status = "budget_exhausted"
                else:
                    try:
                        process.wait(timeout=max(0, deadline - time.monotonic()))
                        status = "ok" if process.returncode == 0 else "failed_gem5_exit"
                    except subprocess.TimeoutExpired:
                        terminate_simulator(process)
                        status = "timeout"
    except TimeoutError as exc:
        status = "timeout"
        launch_error = str(exc)
    except (OSError, ValueError) as exc:
        status = "failed_launch"
        launch_error = str(exc)
    finally:
        cleanup_errors = []
        for workdir in isolated_workdirs:
            try:
                remove_isolated_cwd(workdir)
            except OSError as exc:
                cleanup_errors.append(str(exc))
        if cleanup_errors:
            print("warning: isolated workdir cleanup failed: "
                  + "; ".join(cleanup_errors), file=sys.stderr)

    if status == "budget_exhausted":
        return publish(failure_result(status, candidate))

    measurement_path = output_dir / "measurement.json"
    measurement = json.loads(measurement_path.read_text()) if measurement_path.exists() else {}
    if status in ("ok", "failed_gem5_exit") and measurement.get("status", "ok") != "ok":
        status = measurement["status"]
    stats_path = output_dir / "stats.txt"
    stats, dump_count = parse_stats_file(stats_path) if stats_path.exists() else ({}, 0)
    if status == "ok" and not stats:
        status = "failed_missing_stats"
    result = build_result(stats, dump_count, candidate.get("architecture", {}), status=status)
    result["measurement"] = measurement
    result["command_line"] = command
    result["stdout_tail"] = read_text_tail(stdout_path)
    stderr_parts = [part for part in (read_text_tail(stderr_path), launch_error) if part]
    result["stderr_tail"] = "\n".join(stderr_parts)[-4000:]
    if cleanup_errors:
        result["cleanup_error"] = "\n".join(cleanup_errors)
    result["host_seconds"] = time.monotonic() - started
    append_stat_mapping(args.stat_mapping_out, stats_path, result)
    return publish(result)


if __name__ == "__main__":
    raise SystemExit(main())
