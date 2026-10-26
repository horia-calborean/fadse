# gem5 Configs

`o3_fadse_se.py` is the syscall-emulation O3 configuration used by the
FADSE gem5 connector. It fixes `num_cores=1` and accepts the FADSE design
variables as command-line options:

```bash
--binary --benchmark-name --benchmark-args
--width --rob-entries --iq-entries --lsq-entries
--l1i-kb --l1d-kb --l2-kb --l2-assoc --cacheline-size
--mem-size --warmup-insts --maxinsts
```

Optional architecture flags are `--l3-kb`, `--l3-assoc`, `--bp-global-size`,
`--btb-entries`, `--ras-size`, `--prefetcher`, and `--prefetch-degree`.
L3 size zero disables that level. Prefetcher codes are 0 (none), 1 (stride),
and 2 (tagged). The connector normalizes a zero or omitted degree to one when
a prefetcher is enabled; invoking the gem5 configuration directly with an
enabled prefetcher and a nonpositive degree is rejected.

The DDR3 model retains the `DDR3_1600_8x8` device organization and derives its
rank count from `--mem-size`, so the modeled DRAM capacity matches the address
range. Consequently, the requested capacity must be a power-of-two multiple of
the 4 GiB rank capacity (for example 4, 8, 16, 32 or 64 GiB). On Linux, gem5
maps the guest memory with `MAP_NORESERVE`
so large simulated ranges do not require equivalent host swap reservation. The
size-dependent cache-latency table supplies analytical estimates rather than
physically calibrated timings.

The authoritative design-space contract is
`configs/gem5/architecture-contract.json`. It owns parameter domains, repair
rules, cost weights, simulation defaults, and candidate-fingerprint field
order for both Java and Python. After editing it, regenerate and verify the
language adapters:

```bash
python3 scripts/generate_gem5_contract.py
python3 scripts/generate_gem5_contract.py --check
```

Do not edit `GeneratedGem5Contract.java` or either generated
`generated_gem5_contract.py` copy directly. The generator emits one Python
adapter for the runner and one for the standalone gem5 configuration. Changing
`cache_format` intentionally invalidates old candidate cache entries.

`simulation_window.py` detects guest failures and incomplete warmup and writes
measurement status through the O3 harness. Failed or incomplete simulations
are rejected. See the [setup and run guide](../doc/Gem5_Setup_For_FADSE.md).

Run a FADSE example from the setup guide after changing this configuration.
Check `stats.txt`, `config.ini`, and `fadse_result.json` in the candidate output
directory; successful candidates have status `ok` and positive objectives.
