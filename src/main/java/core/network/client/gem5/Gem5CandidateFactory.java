package core.network.client.gem5;

import input.ports.parameter.problem.ProblemParameter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Gem5CandidateFactory {
    private Gem5CandidateFactory() {
    }

    public static Gem5Candidate create(
            Map<String, String> problemConfig,
            ProblemParameter<?>[] parameters,
            String selectedBenchmark
    ) {
        return create(problemConfig, extractArchitecture(parameters), selectedBenchmark);
    }

    public static Gem5Candidate create(Map<String, String> problemConfig,
            Map<String, Integer> values, String selectedBenchmark) {
        String method = Gem5Config.valueOrDefault(problemConfig.get(Gem5Config.GEM5_METHOD), Gem5Config.DEFAULT_METHOD);
        String seed = Gem5Config.valueOrDefault(problemConfig.get(Gem5Config.GEM5_SEED), Gem5Config.DEFAULT_SEED);
        String budgetIndex = Gem5Config.valueOrDefault(
                problemConfig.get(Gem5Config.GEM5_BUDGET_INDEX),
                Gem5Config.DEFAULT_BUDGET_INDEX
        );
        String selectedBenchmarkValue = Gem5Config.valueOrDefault(selectedBenchmark, "");
        String benchmarkBinary = Gem5Config.valueOrDefault(
                problemConfig.get(Gem5Config.GEM5_BENCHMARK_BINARY),
                selectedBenchmarkValue
        );
        String benchmarkName = Gem5Config.valueOrDefault(
                problemConfig.get(Gem5Config.GEM5_BENCHMARK_NAME),
                selectedBenchmarkValue
        );
        String benchmarkArguments = Gem5Config.valueOrDefault(
                problemConfig.get(Gem5Config.GEM5_BENCHMARK_ARGS),
                ""
        );
        String runCwd = Gem5Config.valueOrDefault(problemConfig.get(Gem5Config.GEM5_RUN_CWD), "");
        String standardInput = Gem5Config.valueOrDefault(problemConfig.get(Gem5Config.GEM5_STDIN), "");
        String workloadMemorySize = "";
        Map<String, String> environment = new LinkedHashMap<>();
        boolean isolateCwd = false;

        String workloadManifest = Gem5Config.valueOrDefault(
                problemConfig.get(Gem5Config.GEM5_WORKLOAD_MANIFEST),
                ""
        );
        if (!workloadManifest.isEmpty()) {
            Gem5WorkloadManifest.Entry workload = Gem5WorkloadManifest.resolve(
                    workloadManifest,
                    selectedBenchmarkValue
            );
            benchmarkBinary = workload.getBinary();
            benchmarkName = workload.getBenchmark() + "__" + workload.getId();
            benchmarkArguments = workload.getArguments();
            runCwd = workload.getCwd();
            standardInput = workload.getStdin();
            workloadMemorySize = workload.getMemorySize();
            isolateCwd = workload.isIsolateCwd();
            environment.putAll(workload.getEnvironment());
        }

        Map<String, Integer> architecture = new LinkedHashMap<>(values);
        Map<String, Integer> originalArchitecture = new LinkedHashMap<>(architecture);

        Gem5CandidateValidation validation = Gem5CandidateValidator.validate(architecture);
        String executable = Gem5Config.require(problemConfig, Gem5Config.GEM5_EXECUTABLE);
        String configScript = Gem5Config.require(problemConfig, Gem5Config.GEM5_CONFIG_SCRIPT);
        String timeoutSeconds = Gem5Config.valueOrDefault(
                problemConfig.get(Gem5Config.GEM5_TIMEOUT_SECONDS),
                Gem5Config.DEFAULT_TIMEOUT_SECONDS
        );
        String warmupInsts = Gem5Config.valueOrDefault(
                problemConfig.get(Gem5Config.GEM5_WARMUP_INSTS),
                Gem5Config.DEFAULT_WARMUP_INSTS
        );
        String maxInsts = Gem5Config.valueOrDefault(
                problemConfig.get(Gem5Config.GEM5_MAX_INSTS),
                Gem5Config.DEFAULT_MAX_INSTS
        );
        String cachelineSize = Gem5Config.valueOrDefault(
                problemConfig.get(Gem5Config.GEM5_CACHELINE_SIZE),
                Gem5Config.DEFAULT_CACHELINE_SIZE
        );
        String memorySize = Gem5Config.valueOrDefault(
                workloadMemorySize,
                Gem5Config.valueOrDefault(
                        problemConfig.get(Gem5Config.GEM5_MEMORY_SIZE),
                        Gem5Config.DEFAULT_MEMORY_SIZE
                )
        );
        Map<String, String> fingerprintFields = new LinkedHashMap<>();
        fingerprintFields.put("method", method);
        fingerprintFields.put("benchmark", benchmarkName);
        fingerprintFields.put("seed", seed);
        fingerprintFields.put("budget_index", budgetIndex);
        fingerprintFields.put("gem5_binary", executable);
        fingerprintFields.put("config_script", configScript);
        fingerprintFields.put("timeout_seconds", timeoutSeconds);
        fingerprintFields.put("warmup_insts", warmupInsts);
        fingerprintFields.put("maxinsts", maxInsts);
        fingerprintFields.put("cacheline_size", cachelineSize);
        fingerprintFields.put("benchmark_binary", benchmarkBinary);
        fingerprintFields.put("benchmark_args", benchmarkArguments);
        fingerprintFields.put("run_cwd", runCwd);
        fingerprintFields.put("stdin", standardInput);
        fingerprintFields.put("mem_size", memorySize);
        fingerprintFields.put("isolate_cwd", Boolean.toString(isolateCwd));
        String runId = problemConfig.getOrDefault(Gem5Config.GEM5_RUN_ID, "");
        String[] inputHashes = Gem5InputFingerprint.hashes(
                runId, executable, configScript, benchmarkBinary, runCwd, standardInput);
        String candidateId = Gem5CandidateId.of(new Gem5CandidateId.Input(
                fingerprintFields,
                validation.getArchitecture(),
                environment
        ), inputHashes);

        Path outputDirectory = resolveOutputDirectory(
                Gem5Config.valueOrDefault(problemConfig.get(Gem5Config.GEM5_OUTPUT_DIR), "m5out"),
                candidateId
        );
        Path resultProperties = resolveOutputPath(
                outputDirectory,
                Gem5Config.valueOrDefault(problemConfig.get(Gem5Config.GEM5_RESULT_PROPERTIES), Gem5Config.DEFAULT_RESULT_PROPERTIES)
        );
        Path resultJson = resolveOutputPath(
                outputDirectory,
                Gem5Config.valueOrDefault(problemConfig.get(Gem5Config.GEM5_RESULT_JSON), Gem5Config.DEFAULT_RESULT_JSON)
        );

        Map<String, Object> gem5 = new LinkedHashMap<>();
        gem5.put("gem5_binary", executable);
        gem5.put("config_script", configScript);
        gem5.put("benchmark_binary", benchmarkBinary);
        gem5.put("benchmark_name", benchmarkName);
        gem5.put("benchmark_args", benchmarkArguments);
        if (!runCwd.isEmpty()) {
            gem5.put("run_cwd", runCwd);
        }
        if (!standardInput.isEmpty()) {
            gem5.put("stdin", standardInput);
        }
        if (isolateCwd) {
            gem5.put("isolate_cwd", true);
        }
        if (!environment.isEmpty()) {
            gem5.put("environment", environment);
        }
        gem5.put("output_root", outputDirectory.getParent() == null
                ? outputDirectory.toString()
                : outputDirectory.getParent().toString());
        gem5.put("output_dir", outputDirectory.toString());
        gem5.put("timeout_seconds", Gem5Config.parseIntegerOrString(timeoutSeconds));
        gem5.put("warmup_insts", Gem5Config.parseIntegerOrString(warmupInsts));
        gem5.put("maxinsts", Gem5Config.parseIntegerOrString(maxInsts));
        gem5.put("cacheline_size", Gem5Config.parseIntegerOrString(cachelineSize));
        gem5.put("mem_size", memorySize);
        gem5.put("input_hashes", java.util.List.of(inputHashes));
        gem5.put("run_id", runId);
        gem5.put("budget_index", budgetIndex);
        if (problemConfig.containsKey(Gem5Config.GEM5_BUDGET_FILE)) {
            gem5.put("budget_file", problemConfig.get(Gem5Config.GEM5_BUDGET_FILE));
        }

        return new Gem5Candidate(
                candidateId,
                method,
                benchmarkName,
                seed,
                outputDirectory,
                resultProperties,
                resultJson,
                originalArchitecture,
                validation.getArchitecture(),
                validation.getRepairs(),
                validation.getCostProxy(),
                gem5
        );
    }

    private static Map<String, Integer> extractArchitecture(ProblemParameter<?>[] parameters) {
        Map<String, Integer> architecture = new LinkedHashMap<>();
        if (parameters == null) {
            return architecture;
        }

        for (ProblemParameter<?> parameter : parameters) {
            if (parameter == null || parameter.getName() == null || parameter.getName().trim().isEmpty()) {
                continue;
            }
            Object value = parameter.getValue();
            if (value instanceof Number) {
                architecture.put(parameter.getName().trim(), ((Number) value).intValue());
            }
        }
        return architecture;
    }

    private static Path resolveOutputDirectory(String configuredOutputDirectory, String candidateId) {
        String outputDirectory = configuredOutputDirectory.trim();
        if (outputDirectory.contains("#")) {
            return Paths.get(outputDirectory.replace("#", candidateId));
        }
        return Paths.get(outputDirectory).resolve(candidateId);
    }

    private static Path resolveOutputPath(Path outputDirectory, String configuredFile) {
        Path configuredPath = Paths.get(configuredFile);
        if (configuredPath.isAbsolute()) {
            return configuredPath;
        }
        return outputDirectory.resolve(configuredPath);
    }

}
