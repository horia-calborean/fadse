# Running FADSE with gem5

Use Java 25 (specified in `.java-version`), Maven, Python 3, and Linux. Run
FADSE commands from the repository root so relative configuration and script
paths resolve correctly.

## Build FADSE and gem5

```bash
mvn -DskipTests package
bash doc/scripts/linux/install-gem5-for-fadse.sh --install-deps
```

Omit `--install-deps` when the host already has the build dependencies. The
script defaults to gem5 `v25.1.0.1`, builds `build/X86/gem5.opt`, runs a
`/bin/true` smoke test, and generates the ignored local configuration
`configs/designSpace/gem5_local.xml`. The script prints the resulting executable
location; assign it to `GEM5_BIN` for the commands below.

Common build overrides are `--version`, `--isa`, `--variant`, `--jobs`, and
`--src-root`. Use `--no-smoke-test` for a non-X86 build or when `/bin/true`
is unsuitable. The script installs SCons in its own Python environment;
the FADSE Python runner needs only the standard library.

## Prepare a workload and local configuration

The included NSGA-II example uses a static, single-threaded GAPBS BFS binary:

```bash
git clone --depth 1 https://github.com/sbeamer/gapbs.git workloads/gapbs
make -C workloads/gapbs SERIAL=1 CXX_FLAGS="-std=c++11 -O3 -Wall -static" bfs pr cc
mkdir -p configs/designSpace/local results/fadse
cp configs/designSpace/gem5_o3_dse_nsga2.xml configs/designSpace/local/gem5_nsga2.xml
```

Set `gem5_executable` in the local XML to the installed executable. Other
benchmarks can replace the `<benchmarks>` binary path, `gem5_benchmark_name`,
and `gem5_benchmark_args`; use statically linked binaries for this configuration.
Keep binaries, inputs, and machine-specific paths outside tracked examples.

The example uses `configs/metaheuristicConfig/gem5_nsga2.json`, with population
size 20 and `maxEvaluations` 300. FADSE's existing NSGA-II stopping rule counts
evaluated solutions, including cached evaluations; it is not a physical gem5
launch quota. Adjust those settings for the intended run duration.

Examples for separate BFS, PageRank and connected-components workloads are
`gem5_o3_dse_nsga2_{bfs,pr,cc}.xml`. The `gem5_o3_dse_nsga2_gapbs_all.xml`
example evaluates all three through `workloads/gapbs.example.json`; each
workload keeps its own arguments, memory size and candidate identity.
`gem5_o3_dse_nsga2_extended.xml` also varies enabled L3 capacity, branch
prediction and prefetching. Omit the optional variables to retain the baseline
configuration. The extended example varies an enabled L3; set `l3_kb=0` in
a fixed candidate to disable that level.

For an installed SPEC CPU suite, use the [SPEC workload guide](SPEC_Setup_For_FADSE.md).

## Physical-launch budgets and run records

To cap actual simulator starts across all workers, add this to `problem_config`:

```xml
<parameter name="gem5_launch_budget" value="300"/>
<parameter name="gem5_run_root" value="results/runs"/>
```

The server creates a unique run ID and an absolute `launches.json` path under
the run root before dispatching evaluations. All workers must see that same
path. The Python connector admits launches under the shared ledger lock.
Cache hits, invalid candidates and failed executable starts consume no quota;
started simulations consume quota even if they fail or time out. Each workload
in a suite consumes one launch. Requests above the limit fail with
`budget_exhausted`; incomplete suites are rejected.

The run directory contains `evaluated_configurations.csv` with workload result
records, run IDs, cache-hit flags and the observed launch count. Result receipts
are also written under each candidate's `runs/<run-id>.json`. Set
`gem5_evaluated_csv` to choose another CSV path. CSV appends use POSIX locks
shared by Java and Python; candidate and quota locks use `flock`. If an existing
CSV has an older header, it is preserved beside the configured file with a
`.schema-mismatch-<id>.bak` suffix before a new file is started.

The quota is a ceiling. NSGA-II still stops according to `maxEvaluations`, so
cache hits can make it finish below the physical-launch cap.

## Start the client and server

In separate terminals, from the repository root:

```bash
java -cp target/fadse-0.0.1-jar-with-dependencies.jar core.application.ServerBoot client 4450
```

```bash
java -cp target/fadse-0.0.1-jar-with-dependencies.jar core.application.ServerBoot configs/designSpace/local/gem5_nsga2.xml
```

The default client configuration includes localhost port 4450. Each additional
worker needs a distinct port listed in `configs/clients/clientsConfig.xml`.
Run the controller and workers against the same shared filesystem, with
interprocess `flock` support for the Python-backed connector.

`maximumTimeOfASimulation` is the server's timeout in minutes. Set it above the
expected simulation duration. `gem5_timeout_seconds` bounds the Python
request's lock wait, preparation, and simulation together. The Java watchdog
allows extra time for child-process cleanup.

## Configuration and results

The Python-backed example sets these connector parameters:

```xml
<parameter name="gem5_executable" value="GEM5_BIN"/>
<parameter name="gem5_config_script" value="gem5_configs/o3_fadse_se.py"/>
<parameter name="gem5_argument_dialect" value="fadse-o3"/>
<parameter name="gem5_runner_script" value="scripts/run_gem5_candidate.py"/>
<parameter name="gem5_output_dir" value="results/gem5"/>
<parameter name="gem5_timeout_seconds" value="3600"/>
<parameter name="gem5_warmup_insts" value="10000000"/>
<parameter name="gem5_maxinsts" value="50000000"/>
<parameter name="gem5_memory_size" value="16GB"/>
```

Replace the `GEM5_BIN` marker with the executable location printed by the
installer. XML values do not expand shell variables.

The Python-backed connector accepts `cpi`, `l2_mpki_pos`, `cost_proxy`, `ipc`,
`sim_seconds`, `sim_ticks`, `host_seconds`, `cycles`,
`committed_instructions` and `sim_insts` as objective names, plus the
`simTicks`, `simSeconds`, `hostSeconds` and `simInsts` aliases. Raw
`l2_misses` and `l2_mpki` metrics are not objectives because zero is a valid
measurement; use the strictly positive `l2_mpki_pos` objective instead.
Unsupported objective names are rejected during server startup, before work is
dispatched. If a configured metric is absent from an otherwise successful
stats dump, the runner publishes `failed_missing_objective` and names the
missing metric in `fadse_result.json`.
This list is generated into both connectors from
`configs/gem5/architecture-contract.json`.

Use `desired="large"` to maximize IPC. Result files retain the positive raw
measurement; FADSE negates maximizing objectives when forming jMetal scores.
The `host_seconds` and `hostSeconds` objectives use gem5's own `hostSeconds`
statistic. The top-level `host_seconds` JSON field separately records elapsed
wrapper time, including workload preparation and cleanup.

`gem5_maxinsts` limits the measured window after warmup. This O3 configuration
is single-core; accepted architecture options are documented in
[gem5 configuration options](../gem5_configs/README.md).

FADSE writes a unique request JSON. The Python runner locks the candidate,
validates its architecture, runs gem5, and publishes `fadse_result.properties`
and `fadse_result.json`. The example minimizes `cpi`, `l2_mpki_pos`, and the
analytical `cost_proxy`; the latter is not measured power or area. Successful
results have status `ok`. Missing statistics, invalid counters, guest failures,
incomplete warmup, and simulator errors are rejected. Failed workloads must
not contribute a partial suite average.

Candidate directories under `results/gem5/` also contain `stats.txt`,
`config.ini`, `gem5.stdout.log`, and `gem5.stderr.log`. The runner streams
simulator output to these files and stores only bounded diagnostic tails in
`fadse_result.json`. FADSE's existing output remains under `results/fadse/`.
Successful candidates are cached using architecture, invocation settings, and
content hashes of the simulator/workload binaries, configuration helpers, and
explicit stdin. The working-directory path is included, but the mutable tree is
not re-hashed. Expensive input hashes are reused only within one server run;
keep inputs immutable during execution. Shared
libraries and external Python packages are not included in the fingerprint.
Cache latencies are analytical estimates rather than physically calibrated
timings.

For the direct connector, omit `gem5_runner_script`, point `gem5_config_script`
to gem5's `configs/deprecated/example/se.py` or a compatible script, and use
objective names matching `stats.txt` exactly, such as `simTicks` and `simInsts`.
Design-variable names must correspond to that script's arguments. The direct
connector uses the legacy `--cacheline_size` spelling; the Python O3 config
uses `--cacheline-size`. Set `gem5_argument_dialect=fadse-o3` for the bundled O3
script when it is renamed; the original `o3_fadse_se.py` filename selects this
dialect automatically with a warning for compatibility with older XML files.
The dialect also enables architecture repair on the direct path, and those
repairs are recorded in `gem5_repairs_log`. Repair rows include `dedupe_key`,
`run_id`, `candidate_id`, the original and repaired configurations, and the
repair reason. Repeated command construction for the same run and candidate
does not duplicate a repair row; an older CSV schema is rotated before the new
schema is written. Client-local launch and filesystem failures are reported as
retryable so the server can release the client slot and redispatch the
individual without waiting for the simulation timeout. Other direct connectors
preserve fractional numeric design-variable values. The `fadse-o3` dialect
instead requires exactly integral values and rejects fractional, non-finite or
out-of-range numbers rather than silently truncating them. Stale direct-run
statistics are removed before launch. The bundled O3 configuration receives
`gem5_memory_size` on both connector paths. Its fixed DDR3 organization accepts
only a positive power-of-two number of 4 GiB ranks (for example 4, 8, 16, 32,
or 64 GiB); invalid values are rejected before gem5 starts. Legacy direct
connectors do not consume this setting and retain their own memory semantics.
For the Python connector, all configured workload-manifest `memory_size` values
are validated during server run initialization rather than when the
corresponding candidate starts.

The Python runner terminates the complete private process group even when the
gem5 leader exits before a helper process. A failure to remove an isolated
workdir is retained as `cleanup_error` and logged, but it does not invalidate a
successful measurement or force another gem5 launch.

## Verification

```bash
mvn test
python3 -B -m unittest discover -s scripts/tests -v
```

These tests cover Java/Python quota propagation, duplicate requests, atomic
publication and CSV appends, process cleanup, input fingerprints, architecture
costs, complete statistics dumps and SPEC invocation isolation. They use fake
simulator processes by default. To check the real O3 connector:

Set `GEM5_BIN` and `BENCHMARK_BIN` to the local executables, then run:

```bash
python3 scripts/smoke_test_gem5.py \
  --gem5-bin "$GEM5_BIN" --config gem5_configs/o3_fadse_se.py \
  --benchmark "$BENCHMARK_BIN" --outdir results/smoke
GEM5_EXECUTABLE="$GEM5_BIN" python3 -B -m unittest discover -s scripts/tests -v
mvn -Dtest=core.network.client.gem5.Gem5ConfiguredObjectivesIntegrationTest \
  -Dgem5.designSpace=configs/designSpace/local/single-workload.xml test
```

The smoke helper uses no warmup by default, suitable for a short binary.
Real-gem5 tests skip unless explicitly configured. The Java objective readback
test requires a fresh output directory and one workload in the XML, either a
binary path or a manifest entry ID.
