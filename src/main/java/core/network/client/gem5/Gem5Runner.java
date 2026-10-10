package core.network.client.gem5;

import core.network.client.Simulator;
import core.network.client.SimulatorRunner;
import core.model.objectives.Objective;
import input.model.InputData;
import input.model.setup.CommonSetupParameters;
import input.ports.parameter.problem.ProblemParameter;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Gem5Runner extends SimulatorRunner {
    private static final Logger LOGGER = Logger.getLogger(Gem5Runner.class.getName());
    private Path requestJson;
    private Path responseProperties;

    public Gem5Runner(Simulator simulator) {
        super(simulator);
    }

    @Override
    protected String[] getCommandLine() {
        responseProperties = null;
        if (individual == null) {
            throw new IllegalStateException("Individual not set - call setIndividual() first");
        }

        Map<String, String> problemConfig = getProblemConfig(individual.getInputData());
        String executable = Gem5Config.require(problemConfig, Gem5Config.GEM5_EXECUTABLE);
        String configScript = Gem5Config.require(problemConfig, Gem5Config.GEM5_CONFIG_SCRIPT);

        if (Gem5Config.hasValue(problemConfig.get(Gem5Config.GEM5_RUNNER_SCRIPT))) {
            return getPythonRunnerCommandLine(problemConfig);
        }

        Path outputDir = prepareOutputDirectory(Gem5Config.require(problemConfig, Gem5Config.GEM5_OUTPUT_DIR));
        String statsFile = Gem5Config.valueOrDefault(
                problemConfig.get(Gem5Config.GEM5_STATS_FILE),
                Gem5Config.DEFAULT_STATS_FILE
        );

        Path statsPath = resolveStatsPath(outputDir, statsFile);
        try {
            Files.deleteIfExists(statsPath);
        } catch (IOException e) {
            throw new IllegalStateException("Could not delete stale gem5 stats file: " + statsPath, e);
        }
        simulator.setSimulatorOutputFile(statsPath.toString());

        List<String> command = new ArrayList<>();
        command.add(executable);
        command.add("--outdir=" + outputDir);
        command.add("--stats-file=" + statsFile);
        command.add(configScript);

        String benchmark = individual.getSelectedBenchmark();
        if (benchmark != null && !benchmark.trim().isEmpty()) {
            command.add("--cmd=" + benchmark);
        }

        addDesignVariableArguments(command, problemConfig);
        addSimulationWindowArguments(command, problemConfig);

        return command.toArray(new String[0]);
    }

    @Override
    protected long getProcessTimeoutMinutes() {
        // Python's request deadline includes lock admission and gem5 execution.
        // Leave time for cleanup/publication before the outer watchdog fires.
        try {
            Map<String, String> problemConfig = getProblemConfig(individual.getInputData());
            long timeoutSeconds = Long.parseLong(Gem5Config.valueOrDefault(
                    problemConfig.get(Gem5Config.GEM5_TIMEOUT_SECONDS),
                    Gem5Config.DEFAULT_TIMEOUT_SECONDS
            ));
            return Math.max(super.getProcessTimeoutMinutes(), (timeoutSeconds + 59) / 60 + 5);
        } catch (RuntimeException e) {
            return super.getProcessTimeoutMinutes();
        }
    }

    @Override
    protected void validateProcessExit(int exitCode) {
        if (responseProperties == null) {
            if (exitCode == 0) return;
            throw new IllegalStateException("gem5 request failed with exit status " + exitCode);
        }

        // Shared candidate files may belong to a different worker. Only the
        // unique snapshot published by this invocation is safe to parse.
        Properties response = new Properties();
        try (var reader = Files.newBufferedReader(responseProperties)) {
            response.load(reader);
            String status = response.getProperty("status", "").trim();
            if (status.isEmpty() || (exitCode == 0) != status.equals("ok")) {
                throw new IllegalStateException("gem5 response status does not match exit status " + exitCode);
            }
            @SuppressWarnings("unchecked")
            Map<String, Objective> objectives = (Map<String, Objective>) individual.getInputData()
                    .get(CommonSetupParameters.OBJECTIVES);
            for (Objective objective : objectives.values()) {
                double value = Double.parseDouble(response.getProperty(objective.getName(), ""));
                if (!Double.isFinite(value) || (exitCode != 0 && value != Double.MAX_VALUE)) {
                    throw new IllegalStateException("Invalid gem5 response objective: " + objective.getName());
                }
            }
            if (exitCode != 0 && ("failed_launch".equals(status) || "failed_cleanup".equals(status))) {
                throw new IllegalStateException("Retryable gem5 worker failure: " + status);
            }
            if (exitCode != 0) individual.setFeasible(false);
        } catch (IOException | IllegalArgumentException e) {
            throw new IllegalStateException("gem5 request has no valid owned response (exit status "
                    + exitCode + ")", e);
        }
    }

    private String[] getPythonRunnerCommandLine(Map<String, String> problemConfig) {
        Gem5Candidate candidate = Gem5CandidateFactory.create(
                problemConfig,
                currentParameters,
                individual.getSelectedBenchmark()
        );
        Path outputDir = candidate.getOutputDirectory();
        try {
            Files.createDirectories(outputDir);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create gem5 output directory: " + outputDir, e);
        }

        String requestId = "request-" + java.util.UUID.randomUUID();
        requestJson = outputDir.resolve(requestId + ".json");
        responseProperties = outputDir.resolve(requestId + ".properties");
        candidate.writeJson(requestJson, configuredObjectiveNames());
        simulator.setSimulatorOutputFile(responseProperties.toString());

        List<String> command = new ArrayList<>();
        command.add(Gem5Config.valueOrDefault(problemConfig.get(Gem5Config.GEM5_PYTHON), Gem5Config.DEFAULT_PYTHON));
        command.add(Gem5Config.require(problemConfig, Gem5Config.GEM5_RUNNER_SCRIPT));
        command.add("--candidate-json");
        command.add(requestJson.toString());
        command.add("--result-json");
        command.add(candidate.getResultJson().toString());
        command.add("--response-properties");
        command.add(responseProperties.toString());
        if (Gem5Config.hasValue(problemConfig.get(Gem5Config.GEM5_EVALUATED_CSV))) {
            command.add("--evaluated-csv");
            command.add(problemConfig.get(Gem5Config.GEM5_EVALUATED_CSV));
        }
        command.add("--repairs-csv");
        command.add(Gem5Config.valueOrDefault(
                problemConfig.get(Gem5Config.GEM5_REPAIRS_LOG),
                Gem5Config.DEFAULT_REPAIRS_LOG));
        return command.toArray(new String[0]);
    }

    private void addDesignVariableArguments(List<String> command, Map<String, String> problemConfig) {
        if (currentParameters == null) {
            return;
        }

        boolean fadseO3 = Gem5Config.isFadseO3(problemConfig);
        if (!fadseO3) {
            for (ProblemParameter<?> parameter : currentParameters) {
                if (parameter == null || parameter.getName() == null
                        || parameter.getName().trim().isEmpty() || parameter.getValue() == null) {
                    continue;
                }
                command.add("--" + parameter.getName().trim().replace('_', '-')
                        + "=" + String.valueOf(parameter.getValue()));
            }
            return;
        }

        Map<String, Integer> architecture = new java.util.LinkedHashMap<>();
        for (ProblemParameter<?> parameter : currentParameters) {
            if (parameter == null || parameter.getName() == null || parameter.getName().trim().isEmpty()) {
                continue;
            }
            Object value = parameter.getValue();
            if (value == null) {
                continue;
            }
            String name = parameter.getName().trim();
            if (value instanceof Number) {
                try {
                    architecture.put(name, new BigDecimal(value.toString()).intValueExact());
                } catch (ArithmeticException | NumberFormatException ex) {
                    throw new IllegalArgumentException(
                            "fadse-o3 design variable must be an integer in range: "
                                    + name + "=" + value, ex);
                }
            } else {
                throw new IllegalArgumentException(
                        "fadse-o3 design variable must be numeric: " + name);
            }
        }
        Gem5CandidateValidation validation = Gem5CandidateValidator.validate(architecture);
        if (!validation.getRepairs().isEmpty()) {
            String candidateId = Gem5CandidateId.forDirect(
                    architecture, individual.getSelectedBenchmark());
            Gem5CandidateRepairLogger.append(
                    Path.of(Gem5Config.valueOrDefault(
                            problemConfig.get(Gem5Config.GEM5_REPAIRS_LOG),
                            Gem5Config.DEFAULT_REPAIRS_LOG)),
                    problemConfig.getOrDefault(Gem5Config.GEM5_RUN_ID, ""),
                    candidateId,
                    architecture,
                    validation.getArchitecture(),
                    validation.getRepairs()
            );
        }
        validation.getArchitecture().forEach((name, value) ->
                command.add("--" + name.replace('_', '-') + "=" + value));
    }

    @SuppressWarnings("unchecked")
    private List<String> configuredObjectiveNames() {
        Map<String, Objective> objectives = (Map<String, Objective>) individual.getInputData()
                .get(CommonSetupParameters.OBJECTIVES);
        if (objectives == null) {
            return List.of();
        }
        return objectives.values().stream().map(Objective::getName).sorted().toList();
    }

    void cleanupRequestFiles() {
        deleteRequestFile(requestJson);
        deleteRequestFile(responseProperties);
        requestJson = null;
        responseProperties = null;
    }

    private static void deleteRequestFile(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not delete gem5 request file: " + path, e);
        }
    }

    private static void addSimulationWindowArguments(
            List<String> command,
            Map<String, String> problemConfig
    ) {
        if (Gem5Config.isFadseO3(problemConfig)) {
            command.add("--warmup-insts=" + Gem5Config.valueOrDefault(
                    problemConfig.get(Gem5Config.GEM5_WARMUP_INSTS), Gem5Config.DEFAULT_WARMUP_INSTS));
            command.add("--maxinsts=" + Gem5Config.valueOrDefault(
                    problemConfig.get(Gem5Config.GEM5_MAX_INSTS), Gem5Config.DEFAULT_MAX_INSTS));
            command.add("--cacheline-size=" + Gem5Config.valueOrDefault(
                    problemConfig.get(Gem5Config.GEM5_CACHELINE_SIZE), Gem5Config.DEFAULT_CACHELINE_SIZE));
            command.add("--mem-size=" + Gem5Config.validateMemorySize(
                    problemConfig.get(Gem5Config.GEM5_MEMORY_SIZE)));
            return;
        }

        addConfiguredArgument(command, "--warmup-insts=", problemConfig.get(Gem5Config.GEM5_WARMUP_INSTS));
        addConfiguredArgument(command, "--maxinsts=", problemConfig.get(Gem5Config.GEM5_MAX_INSTS));
        addConfiguredArgument(command, "--cacheline_size=", problemConfig.get(Gem5Config.GEM5_CACHELINE_SIZE));
    }

    private static void addConfiguredArgument(List<String> command, String prefix, String value) {
        if (Gem5Config.hasValue(value)) {
            command.add(prefix + value.trim());
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> getProblemConfig(InputData inputData) {
        if (inputData == null) {
            throw new IllegalStateException("Individual has no InputData");
        }
        Map<String, String> problemConfig = (Map<String, String>) inputData.get(CommonSetupParameters.PROBLEM_CONFIG);
        if (problemConfig == null) {
            throw new IllegalStateException("No problem_config found in InputData");
        }
        return problemConfig;
    }

    private Path prepareOutputDirectory(String outputDirectory) {
        String resolvedOutputDirectory = outputDirectory.replace(
                "#",
                System.currentTimeMillis() + "_" + individual.hashCode()
        );
        Path outputPath = Paths.get(resolvedOutputDirectory);
        try {
            Files.createDirectories(outputPath);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create gem5 output directory: " + outputPath, e);
        }
        return outputPath;
    }

    private static Path resolveStatsPath(Path outputDir, String statsFile) {
        Path statsPath = Paths.get(statsFile);
        if (statsPath.isAbsolute()) {
            return statsPath;
        }
        return outputDir.resolve(statsPath);
    }

}
