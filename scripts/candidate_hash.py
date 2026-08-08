#!/usr/bin/env python3
"""Create deterministic candidate IDs for gem5 DSE candidates."""

import argparse
import hashlib
import json
import re
import os
from pathlib import Path

from generated_gem5_contract import (
    CACHE_FORMAT as GENERATED_CACHE_FORMAT,
    FINGERPRINT_FIELDS,
    SIMULATION_DEFAULTS,
)


CACHE_FORMAT = GENERATED_CACHE_FORMAT


def simulation_window(gem5):
    """Effective defaults shared by cache identity and the executed command."""
    warmup = gem5.get("warmup_insts")
    measured = gem5.get("maxinsts")
    return (int(SIMULATION_DEFAULTS["warmup_insts"]) if warmup is None else warmup,
            int(SIMULATION_DEFAULTS["maxinsts"]) if measured is None else measured)


def content_hash(value, python_sources=False):
    """Hash bytes, or a framed, sorted tree of relative paths and file hashes.

    Missing paths remain representable so candidate validation/launch failures
    retain their normal reporting. Unreadable files and symlink cycles fail.
    This format is mirrored by Gem5InputFingerprint in Java.
    """
    if not value:
        return "absent"
    path = Path(value)
    if not path.exists():
        return "missing"
    if path.is_file() and not python_sources:
        digest = hashlib.sha256()
        with path.open("rb") as handle:
            for block in iter(lambda: handle.read(65536), b""):
                digest.update(block)
        return digest.hexdigest()
    root = path.parent if python_sources else path
    entries = {}

    def visit(directory, ancestors):
        resolved = directory.resolve()
        if resolved in ancestors:
            raise ValueError(f"Symlink cycle in fingerprint input: {directory}")
        for child in directory.iterdir():
            if child.is_dir():
                if not python_sources or child.name != "__pycache__":
                    visit(child, ancestors | {resolved})
            elif not python_sources or child.suffix == ".py" or child == path:
                entries[child.relative_to(root).as_posix()] = content_hash(str(child))

    visit(root, set())
    digest = hashlib.sha256()
    # Java String ordering is UTF-16, including names outside the BMP.
    for name in sorted(entries, key=lambda item: item.encode("utf-16-be")):
        for field in (name, entries[name]):
            data = field.encode("utf-8")
            digest.update(str(len(data)).encode("ascii") + b":" + data)
    return digest.hexdigest()


def input_hashes(gem5, benchmark):
    supplied = gem5.get("input_hashes")
    if (isinstance(supplied, list) and len(supplied) == 5
            and all(isinstance(value, str) for value in supplied)):
        return supplied
    stdin = gem5.get("stdin", "")
    if stdin and not os.path.isabs(stdin):
        stdin = str(Path(gem5.get("run_cwd") or ".") / stdin)
    return [content_hash(gem5.get("gem5_binary", "")),
            content_hash(gem5.get("config_script", ""), python_sources=True),
            content_hash(gem5.get("benchmark_binary", benchmark)),
            "path_only" if gem5.get("run_cwd") else "absent", content_hash(stdin)]


def sanitize(value):
    text = "" if value is None else str(value)
    return re.sub(r"[^A-Za-z0-9._-]+", "_", text)[:48]


def candidate_id(candidate, budget_index=None):
    method = candidate.get("method", SIMULATION_DEFAULTS["method"])
    benchmark = candidate.get("benchmark", "benchmark")
    seed = candidate.get("seed", SIMULATION_DEFAULTS["seed"])
    budget = candidate.get("budget_index", budget_index if budget_index is not None
                           else SIMULATION_DEFAULTS["budget_index"])
    gem5 = candidate.get("gem5", {})
    architecture = candidate.get("architecture", {})
    warmup, measured = simulation_window(gem5)
    values = {
        "cache_format": CACHE_FORMAT,
        "method": method,
        "benchmark": benchmark,
        "seed": seed,
        "budget_index": budget,
        "gem5_binary": gem5.get("gem5_binary", ""),
        "config_script": gem5.get("config_script", ""),
        "timeout_seconds": gem5.get("timeout_seconds", SIMULATION_DEFAULTS["timeout_seconds"]),
        "warmup_insts": warmup,
        "maxinsts": measured,
        "cacheline_size": gem5.get("cacheline_size", SIMULATION_DEFAULTS["cacheline_size"]),
        "architecture": json.dumps(architecture, sort_keys=True),
        "benchmark_binary": gem5.get("benchmark_binary", candidate.get("benchmark", "")),
        "benchmark_args": gem5.get("benchmark_args", ""),
        "run_cwd": gem5.get("run_cwd", ""),
        "stdin": gem5.get("stdin", ""),
        "mem_size": gem5.get("mem_size", SIMULATION_DEFAULTS["mem_size"]),
        "isolate_cwd": str(bool(gem5.get("isolate_cwd", False))).lower(),
        "environment": json.dumps(gem5.get("environment", {}), sort_keys=True,
                                  separators=(",", ":"), ensure_ascii=False),
    }
    fields = [str(values[name]) for name in FINGERPRINT_FIELDS] + input_hashes(gem5, benchmark)
    fingerprint = "".join(f"{len(value.encode('utf-8'))}:{value}" for value in fields)
    digest = hashlib.sha256(fingerprint.encode("utf-8")).hexdigest()[:24]
    return f"{sanitize(method)}_{sanitize(benchmark) or 'benchmark'}_seed{sanitize(seed)}_budget{sanitize(budget)}_{digest}"


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--candidate-json", required=True)
    parser.add_argument("--budget-index")
    args = parser.parse_args()
    with Path(args.candidate_json).open(encoding="utf-8") as handle:
        candidate = json.load(handle)
    print(candidate_id(candidate, args.budget_index))


if __name__ == "__main__":
    raise SystemExit(main())
