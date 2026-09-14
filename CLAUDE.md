# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Branch context

This is the `fadse-jmetal-6.6-problem` branch: an in-progress rewrite of FADSE (Framework for
Automatic Design Space Exploration — see the root `README.md` and `doc/Working_with_FADSE.md` for
the project's overall goal) that replaces the old bundled jMetal fork with the real
`org.uma.jmetal` 6.6 Maven artifacts, and introduces a new ports-and-adapters (hexagonal)
architecture under `core/`, `input/`, `output/` — currently implemented only for the GAP
simulator/problem. Commit history on this branch (`git log --oneline`) is a sequence of "Working
on problem" / "fixed" commits — treat the new code as actively unstable WIP, not a finished
design: expect empty stub classes, hardcoded developer paths, and TODOs left for named
teammates (e.g. `// TODO - Andrei`, `// TODO ... -> George`).

The legacy code (`src/main/java/ro/ulbsibiu/fadse/**`, `src/main/java/jmetal/**`) is still present
and untouched — it has **not** been deleted, and `pom.xml`'s `maven-jar-plugin` /
`exec-maven-plugin` main class is still `ro.ulbsibiu.fadse.Boot`, the old entry point. The new
`core.application.ServerBoot` / `core.application.ClientBoot` classes are **not** wired up as the
packaged/exec main class yet — run them directly (IDE run config or `java -cp ...
core.application.ServerBoot`), they take no CLI args and hardcode their design-space config
filename (see below).

## Build

```bash
mvn -o package
```

Same offline-friendly setup as before (`lib/` supplies the Eclipse-only deps: `stepstep-gui`,
`stepstep-configuration-analysys`, `jfuzzylogic`, `ctg-xml`, `jgraphx`), but this branch adds real
Maven dependencies for jMetal 6.6 (`jmetal-core`, `jmetal-algorithm`, `jmetal-problem`) and Apache
POI, and bumps the compiler plugin to Java 14 (`<source>14</source>`/`<target>14</target>`,
previously targeting J2SE-1.5 on `master`). There's still no lint step and no JUnit test suite —
the root-level `Test*.java` classes under `ro.ulbsibiu.fadse` remain manual `main()`-driven
harnesses, not automated tests.

## Running (new architecture)

`core.application.ServerBoot.main` hardcodes its design-space file to
`configs/designSpace/gapdistsimin_andrei.xml` (via `PathUtils.getDseRelativePath`) — edit that
config, or edit `ServerBoot` itself, to point at a different one; there's no CLI arg parsing yet
(unlike the legacy `Boot`, which matched args by file extension). `core.application.ClientBoot`
takes an optional port as `args[1]` (mirrors legacy `BootClient`), starts an `IndividualReceiver`,
and runs the same INI-driven watchdog as before, now via a `ScheduledExecutorService` instead of a
sleep loop, reading `configs/fadseConfig.ini`.

There is no new Monitor entry point yet — the legacy Swing monitor
(`ro.ulbsibiu.fadse.tools.monitor.SwingMonitor`, launched via old `Boot monitor`) is still the only
one.

## New architecture (`core` / `input` / `output`)

This is a ports-and-adapters style split, mirrored across `core` (domain + orchestration) and
`input` (parsing configuration into a `InputData` bag), with GAP as the only wired-up
simulator/problem so far:

- **`input.model.InputData`** — a `Map<SetupParameter, Object>` (keys are enum-like constants from
  `input.model.setup.CommonSetupParameters` / `GapSetupParameters`) that replaces the old
  `Environment`/`InputDocument` pair as the single bag of parsed config passed around at runtime
  (design variables, objectives, benchmarks, DB connection info, metaheuristic settings, clients
  file path, output path, problem type/name).
- **`input.ports.collector.MicroArchInputCollector`** (abstract) / **`input.adapters.collector.gap.GAPInputCollector`**
  — reads a design-space XML/JSON (`input.adapters.document.XmlInputDocument` /
  `JsonInputDocument`, created via `InputDocumentFactory`) and uses a family of `*Extractor`
  interfaces (`input.ports.extractor.**`, implemented per-format in `input.adapters.extractor.**`,
  e.g. `GAPXmlDataExtractor`) to pull out each concern (benchmarks, objectives, metaheuristic
  properties, DB config, clients file, GAP-specific params) into the `InputData`. This replaces
  the old single `XMLInputReader`.
- **`core.problem.application.ProblemFactory`** — reflection-based factory that maps a simulator
  name (currently only `"GAP"`) to a `core.problem.adapters.*Problem` class (searched under
  `core.problem.adapters` and `org.uma.jmetal.problem.multiobjective.dtlz`), constructed with the
  `InputData`. `core.problem.ports.MicroArchitectureProblem` (extends jMetal 6.6's
  `AbstractDoubleProblem`) is the shared base: it builds variable bounds from
  `CommonSetupParameters.DESIGN_VARIABLES`, and its `evaluate()` builds a `FadseIndividual` per
  benchmark, dispatches it via `core.network.ClientsRepository.performSimulation(...)`, and
  averages the returned objectives across benchmarks.
- **`core.algorithm.adapters.WrappedEvolutionaryAlgorithm`** — wraps a jMetal 6.6
  `AbstractEvolutionaryAlgorithm` (built via jMetal's own builders, e.g. `NSGAIIBuilder`, in
  `core.application.AlgorithmRunner`) using reflection over its declared methods (so it can
  override `run()` to insert `clientsRepository.join()` calls between generations, waiting for
  distributed simulation results before continuing the loop) — this is the bridge between jMetal's
  synchronous algorithm loop and FADSE's asynchronous network simulation model. Note it currently
  hardcodes a Windows debug output path (`D:\Downloads\fadse.xlsx`, via `output.application.CsvUtils.writeExcel`)
  on every run — a known rough edge, not an intentional feature.
- **`core.network`** — reworked version of the old
  `ro.ulbsibiu.fadse.extended.problems.simulators.network` layer: `ClientsRepository` (singleton,
  same crash-detection/redistribution/results-averaging logic as before) hands individuals to
  `MessageSender`, tracks in-flight work via `SimulationStatus`, and receives results through
  `ResultsReceiver`; `core.network.client.IndividualReceiver` is the client-side counterpart
  (started by `ClientBoot`) that runs the actual simulator via `core.network.client.SimulatorFactory`
  / `Simulator`. GAP-specific runner/parser/dustman classes live under `core.network.client.gap`
  (ported from `ro.ulbsibiu.fadse.extended.problems.simulators.gap`).
- **Two `AlgorithmRunner` classes exist** — `core.application.AlgorithmRunner` (the real one,
  invoked by `ServerBoot`, builds and runs the jMetal algorithm) and
  `core.algorithm.application.AlgorithmRunner` (an empty stub — don't confuse the two).

When extending this new architecture to another simulator (M-SIM, UniMap, etc., all still only
implemented on the legacy side), the GAP path is the template: add a `*Problem` under
`core.problem.adapters`, a matching `*InputCollector`/`*DataExtractor` set under `input.adapters`,
register the problem name in `ProblemFactory.getClassName(...)`, and add the simulator-specific
runner/parser under `core.network.client.<simulator>`.

## Legacy architecture (`ro.ulbsibiu.fadse`, `jmetal`)

Still present and buildable; see `doc/Working_with_FADSE.md` for the connector-writing workflow
(`SimulatorBase` subclass + `performSimulation(...)`) and the design-space XML format
(`configs/designSpace/*.xml`, DTD at `configs/xmlinput.dtd`). `Boot` → `Environment`/`InputDocument`
(via `XMLInputReader`) → `AlgorithmRunner` → jMetal `Algorithm`/`Problem` via a `SimulatorWrapper`
around a `SimulatorBase` subclass; simulator connectors live under
`ro.ulbsibiu.fadse.extended.problems.simulators.*` (one subpackage per tool: `gap`, `m5`, `msim2`,
`msim3`, `Multi2Sim`, `sniper`, `simplegem5`, `unimap`, `tem`, `continental`, plus `FalseSimulator`
for testing without a real simulator). `ro.ulbsibiu.fadse.persistence` handles MySQL-backed result
reuse; `ro.ulbsibiu.fadse.extended.qualityIndicator` implements DSE quality metrics (hypervolume,
coverage, etc. — `doc/Compute_Metrics_with_FADSE.md`). Treat this as the reference implementation
for behavior the new architecture hasn't ported yet, not as code to extend for new work on this
branch.

## Shared config files (unchanged)

`configs/fadseConfig.ini` (server/client/monitor ports, watchdog timeout), `configs/neighbor/*.xml`
(distributed client list — the legacy path; the new architecture reads its clients file path from
`GapSetupParameters.FADSE_CLIENTS_FILE_PATH` inside the design-space XML instead), and
`configs/metaheuristicConfig/*.properties` (per-algorithm tuning) are all still used by both the
legacy and new code paths.
