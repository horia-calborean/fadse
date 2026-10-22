#!/usr/bin/env python3
"""Run one fixed gem5 candidate and verify FADSE objective outputs."""

import argparse
import json
import math
import subprocess
import sys
import uuid
from pathlib import Path


def candidate(args):
    outdir = Path(args.outdir).resolve()
    return {
        "candidate_id": "smoke",
        "method": "smoke",
        "benchmark": args.benchmark,
        "seed": 0,
        "architecture": {
            "num_cores": 1,
            "width": 4,
            "rob_entries": 128,
            "iq_entries": 64,
            "lsq_entries": 64,
            "l1i_kb": 32,
            "l1d_kb": 32,
            "l2_kb": 512,
            "l2_assoc": 8,
        },
        "gem5": {
            "gem5_binary": args.gem5_bin,
            "config_script": args.config,
            "benchmark_binary": args.benchmark,
            "benchmark_name": Path(args.benchmark).name,
            "benchmark_args": "",
            "output_root": str(outdir.parent),
            "output_dir": str(outdir),
            "timeout_seconds": args.timeout_seconds,
            "warmup_insts": args.warmup_insts,
            "maxinsts": args.maxinsts,
        },
        "outputs": {
            "output_dir": str(outdir),
            "result_properties": str(outdir / "fadse_result.properties"),
            "result_json": str(outdir / "fadse_result.json"),
        },
    }


def read_properties(path):
    values = {}
    with Path(path).open(encoding="utf-8") as handle:
        for line in handle:
            if "=" not in line:
                continue
            key, value = line.split("=", 1)
            values[key.strip()] = value.strip()
    return values


def require_positive(properties, key):
    value = float(properties.get(key, "nan"))
    if not math.isfinite(value) or value <= 0.0 or value == sys.float_info.max:
        raise SystemExit(f"{key} is not positive: {value}")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--gem5-bin", required=True)
    parser.add_argument("--config", required=True)
    parser.add_argument("--benchmark", required=True)
    parser.add_argument("--outdir", required=True)
    parser.add_argument("--timeout-seconds", type=int, default=3600)
    parser.add_argument("--warmup-insts", type=int, default=0)
    parser.add_argument("--maxinsts", type=int, default=1000000)
    args = parser.parse_args()

    outdir = Path(args.outdir).resolve()
    outdir.mkdir(parents=True, exist_ok=True)
    candidate_json = outdir / ("request-" + uuid.uuid4().hex + ".json")
    with candidate_json.open("w", encoding="utf-8") as handle:
        json.dump(candidate(args), handle, indent=2)
        handle.write("\n")

    result_json = outdir / "fadse_result.json"
    command = [
        sys.executable,
        str(Path(__file__).with_name("run_gem5_candidate.py")),
        "--candidate-json",
        str(candidate_json),
        "--result-json",
        str(result_json),
        "--force",
    ]
    subprocess.run(command, check=True)

    stats_path = outdir / "stats.txt"
    config_ini_path = outdir / "config.ini"
    properties_path = outdir / "fadse_result.properties"
    for path in [stats_path, config_ini_path, properties_path]:
        if not path.exists():
            raise SystemExit(f"Required smoke-test output is missing: {path}")

    properties = read_properties(properties_path)
    if properties.get("status") != "ok":
        raise SystemExit(f"gem5 smoke test failed: {properties.get('status')}")
    for key in ["cpi", "l2_mpki_pos", "cost_proxy"]:
        require_positive(properties, key)


if __name__ == "__main__":
    raise SystemExit(main())
