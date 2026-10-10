package core.network.client.gem5;

import core.network.client.Simulator;
import input.model.InputData;
import input.model.setup.CommonSetupParameters;
import core.model.individual.FadseIndividual;
import core.model.objectives.Objective;

import java.util.Map;
import java.util.Objects;
import java.util.LinkedHashMap;
import java.nio.file.Path;
import java.util.logging.Logger;

public class Gem5Simulator extends Simulator {
    private static final Logger LOGGER = Logger.getLogger(Gem5Simulator.class.getName());

    public Gem5Simulator(InputData inputData) {
        super(inputData);

        Map<String, String> problemConfig = validateConfiguration(inputData);
        simulatorOutputFile = problemConfig.getOrDefault(
                Gem5Config.GEM5_STATS_FILE,
                Gem5Config.DEFAULT_STATS_FILE
        );
        simulatorOutputParser = new Gem5OutputParser(this);
        simulatorRunner = new Gem5Runner(this);
    }

    public static Map<String, String> validateConfiguration(InputData inputData) {
        Map<String, String> problemConfig = getProblemConfig(inputData);
        Gem5Config.require(problemConfig, Gem5Config.GEM5_EXECUTABLE);
        String configScript = Gem5Config.require(problemConfig, Gem5Config.GEM5_CONFIG_SCRIPT);
        Gem5Config.require(problemConfig, Gem5Config.GEM5_OUTPUT_DIR);
        boolean pythonBacked = Gem5Config.hasValue(
                problemConfig.get(Gem5Config.GEM5_RUNNER_SCRIPT));
        if (pythonBacked) {
            Gem5Config.validateMemorySize(problemConfig.get(Gem5Config.GEM5_MEMORY_SIZE));
            validatePythonObjectives(inputData);
        } else {
            String rawDialect = problemConfig.get(Gem5Config.GEM5_ARGUMENT_DIALECT);
            boolean bundledO3 = "o3_fadse_se.py".equals(
                    Path.of(configScript).getFileName().toString());
            String dialect = Gem5Config.valueOrDefault(
                    rawDialect,
                    bundledO3 ? Gem5Config.ARGUMENT_DIALECT_FADSE_O3 : "");
            if (Gem5Config.hasValue(dialect)
                    && !Gem5Config.ARGUMENT_DIALECT_FADSE_O3.equals(dialect)) {
                throw new IllegalArgumentException("Unsupported gem5 argument dialect: " + rawDialect);
            }
            if (Gem5Config.hasValue(dialect) && !dialect.equals(rawDialect)) {
                Map<String, String> normalized = new LinkedHashMap<>(problemConfig);
                normalized.put(Gem5Config.GEM5_ARGUMENT_DIALECT, dialect);
                inputData.set(CommonSetupParameters.PROBLEM_CONFIG, normalized);
                problemConfig = normalized;
            }
            if (!Gem5Config.hasValue(rawDialect) && bundledO3) {
                LOGGER.warning("Defaulting bundled O3 gem5 config to argument dialect 'fadse-o3'");
            }
            if (Gem5Config.ARGUMENT_DIALECT_FADSE_O3.equals(dialect)) {
                Gem5Config.validateMemorySize(problemConfig.get(Gem5Config.GEM5_MEMORY_SIZE));
            }
        }
        return problemConfig;
    }

    @Override
    public void performSimulation(FadseIndividual individual) {
        try {
            super.performSimulation(individual);
        } finally {
            ((Gem5Runner) simulatorRunner).cleanupRequestFiles();
        }
    }

    @SuppressWarnings("unchecked")
    private static void validatePythonObjectives(InputData inputData) {
        Map<String, Objective> objectives = (Map<String, Objective>) inputData.get(
                CommonSetupParameters.OBJECTIVES);
        if (objectives == null || objectives.isEmpty()) {
            throw new IllegalArgumentException("Python gem5 connector requires at least one objective");
        }
        var unsupported = objectives.values().stream()
                .map(Objective::getName)
                .filter(name -> !GeneratedGem5Contract.SUPPORTED_OBJECTIVES.contains(name))
                .sorted()
                .toList();
        if (!unsupported.isEmpty()) {
            throw new IllegalArgumentException(
                    "Unsupported Python gem5 objective(s): " + String.join(", ", unsupported));
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> getProblemConfig(InputData inputData) {
        Objects.requireNonNull(inputData, "InputData cannot be null");
        Map<String, String> problemConfig = (Map<String, String>) inputData.get(CommonSetupParameters.PROBLEM_CONFIG);
        if (problemConfig == null) {
            throw new IllegalArgumentException("Missing problem_config for gem5 simulator");
        }
        return problemConfig;
    }

}
