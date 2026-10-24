#!/usr/bin/env python3
"""Generate FADSE/gem5 workload manifests from an installed SPEC CPU suite.

Licensed SPEC sources, binaries, and inputs remain outside the repository.
The generated manifest and XML files contain machine-local paths and should
not be committed.
"""

import argparse
import functools
import hashlib
import html
import json
import re
import shlex
import subprocess
import sys
from collections import defaultdict
from datetime import datetime, timezone
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
DEFAULT_MANIFEST = REPO_ROOT / "workloads" / "spec2026.local.json"
DEFAULT_CONFIG_DIR = REPO_ROOT / "configs" / "designSpace" / "local" / "spec2026"

METHODS = {
    "nsga2": ("nsga2", "NSGAII", "gem5_nsga2.json"),
}

PASSTHROUGH_ENVIRONMENT = {
    "LC_ALL",
    "OMP_NUM_THREADS",
    "OMP_THREAD_LIMIT",
    "OMP_STACKSIZE",
}

XML_TEMPLATE = """<?xml version="1.0" encoding="UTF-8"?>

<design_space>
    <problem_config name="gem5" type="simulator">
        <parameter name="realSimulator" value="gem5"/>
        <parameter name="gem5_executable" value="{gem5_bin}"/>
        <parameter name="gem5_config_script" value="gem5_configs/o3_fadse_se.py"/>
        <parameter name="gem5_argument_dialect" value="fadse-o3"/>
        <parameter name="gem5_runner_script" value="scripts/run_gem5_candidate.py"/>
        <parameter name="gem5_output_dir" value="results/gem5"/>
        <parameter name="gem5_workload_manifest" value="{manifest}"/>
        <parameter name="maximumTimeOfASimulation" value="{max_sim_minutes}"/>
        <parameter name="gem5_timeout_seconds" value="{timeout_seconds}"/>
        <parameter name="gem5_warmup_insts" value="{warmup_insts}"/>
        <parameter name="gem5_maxinsts" value="{maxinsts}"/>
        <parameter name="gem5_cacheline_size" value="64"/>
        <parameter name="gem5_memory_size" value="{memory_size}"/>
        <parameter name="gem5_method" value="{gem5_method}"/>
        <parameter name="gem5_seed" value="0"/>
{launch_budget_parameter}
        <parameter name="gem5_repairs_log" value="results/datasets/candidate_repairs.csv"/>
    </problem_config>
    <parameters>
        <parameter name="width" description="O3 issue/commit width" type="integer" min="2" max="8" step="2"/>
        <parameter name="rob_entries" description="ROB entries" type="integer" min="64" max="256" step="64"/>
        <parameter name="iq_entries" description="IQ entries" type="integer" min="32" max="128" step="32"/>
        <parameter name="lsq_entries" description="LSQ entries" type="integer" min="32" max="96" step="32"/>
        <parameter name="l1i_kb" description="L1 instruction cache size" type="exp2" min="16" max="64"/>
        <parameter name="l1d_kb" description="L1 data cache size" type="exp2" min="16" max="64"/>
        <parameter name="l2_kb" description="L2 cache size" type="exp2" min="256" max="2048"/>
        <parameter name="l2_assoc" description="L2 associativity" type="exp2" min="4" max="16"/>
{extended_parameters}
    </parameters>
    <benchmarks>
{benchmark_items}
    </benchmarks>
    <metaheuristic name="{algorithm}" config_path="{metaheuristic_json}"/>
    <objectives>
        <objective name="cpi" type="float" unit="" desired="small"/>
        <objective name="l2_mpki_pos" type="float" unit="" desired="small"/>
        <objective name="cost_proxy" type="float" unit="" desired="small"/>
    </objectives>
    <database ip="127.0.0.1" port="3306" name="fadse" user="root" password="root"/>
    <clients fileName="clientsConfig.xml"/>
    <output path="results/fadse/"/>
</design_space>
"""

EXTENDED_PARAMETERS = """        <parameter name="l3_kb" description="Enabled L3 capacity" type="exp2" min="2048" max="16384"/>
        <parameter name="l3_assoc" description="L3 associativity" type="exp2" min="8" max="32"/>
        <parameter name="bp_global_size" description="Global predictor entries" type="exp2" min="1024" max="16384"/>
        <parameter name="btb_entries" description="BTB entries" type="exp2" min="512" max="8192"/>
        <parameter name="ras_size" description="Return stack entries" type="exp2" min="8" max="64"/>
        <parameter name="prefetcher" description="0 none, 1 stride, 2 tagged" type="integer" min="0" max="2"/>
        <parameter name="prefetch_degree" description="Prefetch degree; repaired to zero without prefetcher" type="exp2" min="1" max="4"/>"""


def fail(message):
    raise SystemExit(f"error: {message}")


def safe_name(value):
    return re.sub(r"[^A-Za-z0-9_.-]+", "_", value)


def infer_suite(spec_root):
    version_file = spec_root / "version.txt"
    version = version_file.read_text(errors="replace").strip() if version_file.is_file() else ""
    if version.startswith("1.") and (spec_root / "Docs" / "benchmarks" / "801.xz_s").is_dir():
        return "cpu2026"
    return "spec_cpu"


def discover_benchmarks(spec_root, requested, all_speed, all_benchmarks):
    cpu_dir = spec_root / "benchspec" / "CPU"
    if not cpu_dir.is_dir():
        fail(f"{cpu_dir} not found; --spec-root must be an installed SPEC CPU tree")
    if all_benchmarks:
        benchmarks = sorted(
            path.name
            for path in cpu_dir.iterdir()
            if path.is_dir()
            and (path.name.endswith("_s") or path.name.endswith("_r"))
            and not path.name.startswith(("998.", "999."))
        )
    elif all_speed:
        benchmarks = sorted(
            path.name
            for path in cpu_dir.iterdir()
            if path.is_dir() and path.name.endswith("_s") and not path.name.startswith("998.")
        )
    else:
        benchmarks = requested or []
    if not benchmarks:
        fail("provide --benchmarks or --all-speed")
    missing = [benchmark for benchmark in benchmarks if not (cpu_dir / benchmark).is_dir()]
    if missing:
        fail(f"benchmark directories not found: {', '.join(missing)}")
    return benchmarks


def newest_reference_run(bench_dir):
    run_root = bench_dir / "run"
    if not run_root.is_dir():
        fail(f"{bench_dir.name} has no run directory; run runcpu --action=setup --size=refspeed")
    workload_name = "refspeed" if bench_dir.name.endswith("_s") else "refrate"
    run_dirs = sorted(
        (
            path for path in run_root.iterdir()
            if path.is_dir() and workload_name in path.name and (path / "speccmds.cmd").is_file()
        ),
        key=lambda path: path.stat().st_mtime,
    )
    if not run_dirs:
        fail(
            f"{bench_dir.name} has no {workload_name} command file; "
            f"run runcpu --action=setup --size={workload_name}"
        )
    return run_dirs[-1]


def executable_map(bench_dir):
    exe_dir = bench_dir / "exe"
    if not exe_dir.is_dir():
        fail(f"{exe_dir} not found; build {bench_dir.name} first")
    binaries = {path.name: path.resolve() for path in exe_dir.iterdir() if path.is_file()}
    if not binaries:
        fail(f"no executables found in {exe_dir}")
    return binaries


@functools.cache
def verify_static(binary):
    completed = subprocess.run(
        ["file", str(binary)],
        check=False,
        capture_output=True,
        text=True,
    )
    if completed.returncode != 0 or "statically linked" not in completed.stdout:
        fail(f"gem5 SE requires a static executable: {binary} ({completed.stdout.strip()})")


def command_environment(lines):
    environment = {
        "LC_ALL": "C",
        "OMP_NUM_THREADS": "1",
        "OMP_THREAD_LIMIT": "1",
    }
    for line in lines:
        if not line.startswith("-E "):
            continue
        try:
            tokens = shlex.split(line)
        except ValueError:
            continue
        if len(tokens) >= 3 and tokens[1] in PASSTHROUGH_ENVIRONMENT:
            environment[tokens[1]] = tokens[2]
    return environment


def strip_shell_redirections(tokens):
    result = []
    index = 0
    while index < len(tokens):
        token = tokens[index]
        if token in {">", ">>", "1>", "1>>", "2>", "2>>", "<"}:
            index += 2
            continue
        if re.match(r"^(?:[012]?>|[012]?>>|<).+", token):
            index += 1
            continue
        result.append(token)
        index += 1
    return result


def parse_invocations(bench_dir, memory_size="16GB"):
    run_dir = newest_reference_run(bench_dir)
    effective_cwd = run_dir.resolve()
    binaries = executable_map(bench_dir)
    lines = (run_dir / "speccmds.cmd").read_text(errors="replace").splitlines()
    environment = command_environment(lines)
    invocations = []

    for line in lines:
        try:
            tokens = shlex.split(line)
        except ValueError:
            continue
        if len(tokens) >= 2 and tokens[0] == "-C":
            requested_cwd = Path(tokens[1])
            effective_cwd = (
                requested_cwd if requested_cwd.is_absolute()
                else effective_cwd / requested_cwd
            ).resolve()
            if not effective_cwd.is_dir():
                fail(
                    f"working directory does not exist for {bench_dir.name}: "
                    f"{effective_cwd}"
                )
            tokens = tokens[2:]
            if not tokens:
                continue
        executable_index = next(
            (index for index, token in enumerate(tokens) if Path(token).name in binaries),
            None,
        )
        if executable_index is None:
            continue

        stdin_name = ""
        for index, token in enumerate(tokens[:executable_index]):
            if token == "-i" and index + 1 < executable_index:
                stdin_name = tokens[index + 1]

        executable = binaries[Path(tokens[executable_index]).name]
        verify_static(executable)
        arguments = strip_shell_redirections(tokens[executable_index + 1:])
        stdin_path = str((effective_cwd / stdin_name).resolve()) if stdin_name else ""
        if stdin_path and not Path(stdin_path).is_file():
            fail(f"stdin file does not exist for {bench_dir.name}: {stdin_path}")
        invocation_index = len(invocations)
        identity = json.dumps(
            {
                "binary": executable.name,
                "argv": arguments,
                "stdin": Path(stdin_path).name if stdin_path else "",
                "cwd": str(effective_cwd),
                "environment": environment,
            },
            sort_keys=True,
        )
        identity_hash = hashlib.sha256(identity.encode()).hexdigest()[:10]
        invocation_id = safe_name(
            f"{bench_dir.name}__{invocation_index:03d}_{executable.name}_{identity_hash}"
        )
        invocations.append({
            "id": invocation_id,
            "benchmark": bench_dir.name,
            "invocation_index": invocation_index,
            "binary": str(executable),
            "args": shlex.join(arguments),
            "argv": arguments,
            "cwd": str(effective_cwd),
            "stdin": stdin_path,
            "isolate_cwd": True,
            "environment": environment,
            "memory_size": memory_size,
        })

    if not invocations:
        fail(f"no executable invocations found in {run_dir / 'speccmds.cmd'}")
    return invocations


def write_manifest(path, suite, spec_root, entries):
    path.parent.mkdir(parents=True, exist_ok=True)
    manifest = {
        "schema_version": 1,
        "suite": suite,
        "generated_at": datetime.now(timezone.utc).isoformat(),
        "spec_root": str(spec_root),
        "entries": entries,
    }
    path.write_text(json.dumps(manifest, indent=2, sort_keys=True) + "\n")


def write_design_spaces(args, manifest_path, entries):
    by_benchmark = defaultdict(list)
    for entry in entries:
        by_benchmark[entry["benchmark"]].append(entry)
    args.config_dir.mkdir(parents=True, exist_ok=True)

    for benchmark, benchmark_entries in sorted(by_benchmark.items()):
        benchmark_items = "\n".join(
            f'        <item name="{html.escape(entry["id"], quote=True)}"/>'
            for entry in benchmark_entries
        )
        benchmark_safe = safe_name(benchmark).replace(".", "_")
        for method_key, (gem5_method, algorithm, metaheuristic_json) in METHODS.items():
            xml = XML_TEMPLATE.format(
                gem5_bin=html.escape(str(args.gem5_bin), quote=True),
                manifest=html.escape(str(manifest_path), quote=True),
                max_sim_minutes=args.timeout_seconds // 60 + 10,
                timeout_seconds=args.timeout_seconds,
                warmup_insts=args.warmup_insts,
                maxinsts=args.maxinsts,
                gem5_method=gem5_method,
                memory_size=html.escape(
                    benchmark_entries[0].get("memory_size", args.memory_size),
                    quote=True,
                ),
                benchmark_items=benchmark_items,
                algorithm=algorithm,
                metaheuristic_json=metaheuristic_json,
                launch_budget_parameter=(
                    f'        <parameter name="gem5_launch_budget" value="{args.launch_budget}"/>'
                    if args.launch_budget is not None else ""
                ),
                extended_parameters=EXTENDED_PARAMETERS if args.extended_architecture else "",
            )
            output = args.config_dir / f"gem5_o3_dse_{method_key}_{benchmark_safe}.xml"
            output.write_text(xml)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--spec-root", required=True, type=Path)
    selection = parser.add_mutually_exclusive_group(required=True)
    selection.add_argument("--benchmarks", nargs="+")
    selection.add_argument("--all-speed", action="store_true")
    selection.add_argument(
        "--all",
        dest="all_benchmarks",
        action="store_true",
        help="all scored speed and rate benchmarks (excludes 998/999 specrand)",
    )
    parser.add_argument("--suite")
    parser.add_argument("--gem5-bin", required=True, type=Path)
    parser.add_argument("--manifest-out", type=Path, default=DEFAULT_MANIFEST)
    parser.add_argument("--config-dir", type=Path, default=DEFAULT_CONFIG_DIR)
    parser.add_argument("--warmup-insts", type=int, default=10_000_000)
    parser.add_argument("--maxinsts", type=int, default=50_000_000)
    parser.add_argument("--timeout-seconds", type=int, default=7200)
    parser.add_argument("--memory-size", default="16GB")
    parser.add_argument("--launch-budget", type=int,
                        help="cap physical gem5 launches across all clients in each FADSE run")
    parser.add_argument("--extended-architecture", action="store_true",
                        help="also vary enabled L3, branch prediction, and prefetching")
    args = parser.parse_args()
    if args.launch_budget is not None and args.launch_budget <= 0:
        parser.error("--launch-budget must be positive")

    args.spec_root = args.spec_root.resolve()
    args.gem5_bin = args.gem5_bin.resolve()
    args.manifest_out = args.manifest_out.resolve()
    args.config_dir = args.config_dir.resolve()
    if not args.gem5_bin.is_file():
        fail(f"gem5 executable not found: {args.gem5_bin}")

    suite = args.suite or infer_suite(args.spec_root)
    benchmarks = discover_benchmarks(
        args.spec_root,
        args.benchmarks,
        args.all_speed,
        args.all_benchmarks,
    )
    entries = []
    for benchmark in benchmarks:
        memory_size = "64GB" if benchmark == "854.graph500_s" else args.memory_size
        benchmark_entries = parse_invocations(
            args.spec_root / "benchspec" / "CPU" / benchmark,
            memory_size=memory_size,
        )
        entries.extend(benchmark_entries)
        print(f"{benchmark}: {len(benchmark_entries)} invocation(s)")

    write_manifest(args.manifest_out, suite, args.spec_root, entries)
    write_design_spaces(args, args.manifest_out, entries)
    print(
        f"Generated {len(entries)} invocation(s) for {len(benchmarks)} benchmark(s).\n"
        f"Manifest: {args.manifest_out}\n"
        f"FADSE configs: {args.config_dir}"
    )


if __name__ == "__main__":
    main()
