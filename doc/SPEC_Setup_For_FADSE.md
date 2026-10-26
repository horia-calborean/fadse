# SPEC CPU 2026 workloads with FADSE and gem5

This workflow uses licensed SPEC CPU 2026 sources, binaries and inputs from a
local installation. Do not commit the suite, generated binaries, reference
inputs, manifests or machine-local XML files.

The commands below use variables instead of machine-specific paths. Set them
for the current shell before starting:

```bash
export WORKSPACE="$(pwd)"
export FADSE_ROOT="$WORKSPACE/fadse-private"
export CPU2026_ISO="$WORKSPACE/cpu2026-1.0.1.iso"
export CPU2026_MOUNT="$WORKSPACE/cpu2026-media"
export SPEC_ROOT="$WORKSPACE/cpu2026"
export GEM5_BIN="$WORKSPACE/gem5/build/X86/gem5.opt"
export GCC_ROOT="$(dirname "$(dirname "$(command -v gcc)")")"
```

Adjust only the variable values when the workspace layout differs.

## Prerequisites

Install Java 25, Maven, Python 3, `file`, GCC and G++. Building the complete
CPU2026 suite also requires GFortran and the static C, C++, Fortran, pthread
and OpenMP runtime libraries supplied by the platform toolchain. Build gem5
first by following the [gem5 setup guide](Gem5_Setup_For_FADSE.md), then set
`GEM5_BIN` to the resulting X86 executable.

The static configuration in this repository is intended for a GCC toolchain
compatible with CPU2026's shipped `Example-gcc-linux-x86.cfg`. It deliberately
uses baseline `x86-64` code generation instead of `-march=native`, because the
host CPU and the simulated gem5 CPU may expose different ISA extensions.

## Install CPU2026 from the ISO

Run the installer into a writable directory outside the FADSE repository:

```bash
mkdir -p "$CPU2026_MOUNT" "$SPEC_ROOT"
sudo mount -o loop,ro "$CPU2026_ISO" "$CPU2026_MOUNT"
"$CPU2026_MOUNT/install.sh" -d "$SPEC_ROOT"
sudo umount "$CPU2026_MOUNT"
```

Confirm the installed version and load the suite environment:

```bash
sed -n '1p' "$SPEC_ROOT/version.txt"
cd "$SPEC_ROOT"
. ./shrc
```

After `shrc` is loaded, the suite sets `SPEC` to its installation directory.

## Create the static one-thread configuration

CPU2026 supplies the compiler definitions and benchmark-specific portability
rules in `Example-gcc-linux-x86.cfg`. The repository provides a small overlay
that retains those rules while enforcing static linking, baseline x86-64 code
generation, one copy and one thread:

```bash
cp "$FADSE_ROOT/configs/spec/fadse-gem5-static.cfg" "$SPEC/config/"
```

Every `runcpu` command using the overlay must provide `gcc_dir`, as shown in
the build commands below.

The overlay keeps the official `-pthread` and OpenMP compiler groups, so
single-thread execution does not remove libraries required by `801.xz_s` or
`854.graph500_s`. It also limits the example's Fortran DO CONCURRENT transform
to one worker.

Use `threads = 1` in the overlay and `--threads=1` on `runcpu` commands to
request single-thread execution. Do not add `preENV_OMP_NUM_THREADS`; `runcpu`
manages `OMP_NUM_THREADS` itself and rejects a conflicting `preENV` setting.

## Build and prepare the example workloads

The documented starter set is `801.xz_s`, `821.gcc_s` and
`854.graph500_s`. Build the base binaries and prepare reference working
directories without running the native benchmark suite:

```bash
SPEC_BENCHMARKS="801.xz_s 821.gcc_s 854.graph500_s"

runcpu --config=fadse-gem5-static --define gcc_dir="$GCC_ROOT" \
  --action=build --tune=base $SPEC_BENCHMARKS

runcpu --config=fadse-gem5-static --define gcc_dir="$GCC_ROOT" \
  --action=runsetup --tune=base --size=refspeed --threads=1 \
  $SPEC_BENCHMARKS
```

`runsetup` creates reference run directories and their `speccmds.cmd` files.
The workload generator reads those files rather than guessing command lines.

To prepare all scored workloads, build both suite families and prepare speed
and rate inputs separately:

```bash
runcpu --config=fadse-gem5-static --define gcc_dir="$GCC_ROOT" \
  --action=build --tune=base all

runcpu --config=fadse-gem5-static --define gcc_dir="$GCC_ROOT" \
  --action=runsetup --tune=base --size=refspeed --threads=1 \
  intspeed fpspeed

runcpu --config=fadse-gem5-static --define gcc_dir="$GCC_ROOT" \
  --action=runsetup --tune=base --size=refrate --copies=1 \
  intrate fprate
```

Before continuing, inspect the selected executables with `file`. The next step
also rejects every executable that is not reported as statically linked.

## Generate the manifest and FADSE configurations

Return to the FADSE repository and generate the starter set:

```bash
cd "$FADSE_ROOT"
python3 scripts/setup_spec_workloads.py \
  --spec-root "$SPEC" \
  --gem5-bin "$GEM5_BIN" \
  --benchmarks 801.xz_s 821.gcc_s 854.graph500_s \
  --launch-budget 300
```

Use `--all-speed` for every prepared speed workload or `--all` for prepared
speed and rate workloads. The generator writes:

```text
workloads/spec2026.local.json
configs/designSpace/local/spec2026/
```

The files are ignored by Git because they contain machine-local paths into the
licensed installation. Each XML benchmark item is a manifest invocation ID.
The connector resolves its binary, arguments, working directory, standard
input, environment and simulated memory size.

Each launch copies the invocation's run directory into a candidate-private
worktree so benchmark outputs cannot modify the shared SPEC installation. The
worktree is disposable and is removed after success, failure or timeout;
candidate statistics, logs and result files remain in the configured output
directory.

Defaults are 10 million warmup instructions, 50 million measured instructions
and a 7,200-second request timeout. Override them with `--warmup-insts`,
`--maxinsts` and `--timeout-seconds`. The configured memory is simulated memory,
not preallocated host RAM. It defaults to 16 GB; `854.graph500_s` receives
64 GB automatically.

## Validate one real invocation

Copy one generated XML to
`configs/designSpace/local/spec2026/single-invocation.xml`, keep one `<item>`
under `<benchmarks>`, and change `gem5_output_dir` to a fresh relative output
directory. Then run:

```bash
mvn -Dtest=core.network.client.gem5.Gem5ConfiguredObjectivesIntegrationTest \
  -Dgem5.designSpace=configs/designSpace/local/spec2026/single-invocation.xml \
  test
```

This invokes the real Python/gem5/Java path at the design variables' lower
bounds. Success requires `status` to be `ok`; the test independently recomputes
CPI, L2 MPKI and cost proxy and writes `objective_readback.json` beside the
candidate results.

The ordinary Java and Python test suites use fake simulator processes. They
verify connector behavior but do not establish that a CPU2026 executable is
compatible with gem5 syscall-emulation mode.

## Start FADSE

Build FADSE, choose one generated XML, and start the client and server from the
repository root in separate terminals:

```bash
cd "$FADSE_ROOT"
mvn -DskipTests package
export FADSE_JAR=target/fadse-0.0.1-jar-with-dependencies.jar
export SPEC_XML=configs/designSpace/local/spec2026/gem5_o3_dse_nsga2_801_xz_s.xml
```

Client terminal:

```bash
java -cp "$FADSE_JAR" core.application.ServerBoot client 4450
```

Server terminal:

```bash
java -cp "$FADSE_JAR" core.application.ServerBoot "$SPEC_XML"
```

For multiple workers, add each client port to
`configs/clients/clientsConfig.xml`. Every worker must resolve the simulator,
manifest, SPEC executables, inputs, candidate outputs and launch ledger through
equivalent shared paths. The filesystem must support POSIX `flock`.

Ensure `maximumTimeOfASimulation` exceeds `gem5_timeout_seconds` plus cleanup
time. One architecture evaluation launches every invocation listed for that
benchmark unless a compatible cached result exists. A launch budget smaller
than the number of required uncached invocations produces an incomplete suite
and worst objective values.

## Result semantics

FADSE minimizes CPI, positive L2 MPKI and the analytical cost proxy. These are
fixed gem5 instruction-window measurements, not official CPU2026 scores.

When an XML contains multiple manifest invocations, FADSE gives every successful
invocation equal weight when averaging each objective. It does not weight by
native runtime, simulated instruction count or the CPU2026 scoring formula. A
failed invocation rejects the complete architecture result; partial averages
are not accepted.

## Troubleshooting

- **Not statically linked:** confirm that the overlay is next to the official
  GCC example, pass `--define gcc_dir="$GCC_ROOT"`, rebuild, and inspect the
  resulting executable with `file`.
- **No reference command file:** run the matching `runsetup` command and use
  `refspeed` for `_s` benchmarks or `refrate` for `_r` benchmarks.
- **Program ends during warmup:** inspect `fadse_result.json`; reduce the warmup
  or select a longer invocation before starting an optimization campaign.
- **Timeout:** raise both `gem5_timeout_seconds` and
  `maximumTimeOfASimulation`, keeping the server timeout larger.
- **gem5 assertion or unsupported syscall:** inspect the candidate's gem5 log,
  `stats.txt` and `fadse_result.json`. Do not interpret worst objective values
  as measurements.
- **Stale result reused:** use a fresh output directory when validating. Normal
  optimization runs intentionally cache successful candidates by content and
  invocation identity.
- **Missing result on a worker:** verify that all workers use the repository
  root as their working directory and can resolve the same manifest, inputs,
  output directory and launch ledger.
