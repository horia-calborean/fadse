# Workload Records

`workloads/workloads.yaml` records benchmark identifiers, executable paths, arguments, and input descriptions used for gem5 runs.

Keep proprietary SPEC CPU binaries, inputs, and generated data outside the repository. Commit only workload metadata that is safe to share, or use local paths that other users can replace.

The included `smoke_true` entry is for parser and runner checks only. Replace or extend it with the licensed or open benchmark set used for actual experiments.

`gapbs.example.json` is an executable connector manifest for BFS, PageRank and
connected components. Build the static binaries under `workloads/gapbs/` and
run FADSE from the repository root. The matching example is
`configs/designSpace/gem5_o3_dse_nsga2_gapbs_all.xml`.

Use `scripts/setup_spec_workloads.py` to generate machine-local manifests from
prepared SPEC run directories. See [the SPEC guide](../doc/SPEC_Setup_For_FADSE.md).
