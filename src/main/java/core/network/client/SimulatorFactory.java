package core.network.client;

import core.network.client.gap.GAPSimulator;
import core.network.client.gem5.Gem5Simulator;
import input.model.InputData;
import input.model.setup.CommonSetupParameters;

import java.util.Objects;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SimulatorFactory - Factory for creating simulator instances.
 *
 * Improvements:
 * - Null validation
 * - Better error messages
 * - Logging
 */
public class SimulatorFactory {
    private static final Logger LOGGER = Logger.getLogger(SimulatorFactory.class.getName());

    /**
     * Creates a simulator instance based on the configuration.
     *
     * @param inputData Configuration data (cannot be null)
     * @return Simulator instance
     * @throws NullPointerException if inputData is null
     * @throws IllegalArgumentException if simulator type is unknown or not configured
     */
    public static Simulator createSimulator(InputData inputData) {
        Objects.requireNonNull(inputData, "InputData cannot be null");

        String simulatorName = (String) inputData.get(CommonSetupParameters.NAME);

        if (simulatorName == null || simulatorName.trim().isEmpty()) {
            throw new IllegalArgumentException("Simulator name not configured in InputData");
        }

        LOGGER.log(Level.INFO, "Creating simulator: " + simulatorName);

        if ("GAP".equalsIgnoreCase(simulatorName)) {
            return new GAPSimulator(inputData);
        }
        if ("gem5".equalsIgnoreCase(simulatorName)) {
            return new Gem5Simulator(inputData);
        }

        // If we reach here, simulator type is not supported
        String errorMsg = String.format(
            "Unknown simulator type: '%s'. Supported types: GAP, gem5",
            simulatorName
        );
        LOGGER.log(Level.SEVERE, errorMsg);
        throw new IllegalArgumentException(errorMsg);
    }

    /** Apply connector-specific request placeholder policy before construction. */
    @SuppressWarnings("unchecked")
    public static void resolvePlaceholders(InputData inputData, String messageId) {
        Objects.requireNonNull(inputData, "InputData cannot be null");
        String simulatorName = (String) inputData.get(CommonSetupParameters.NAME);
        if ("gem5".equalsIgnoreCase(simulatorName)) {
            // Candidate IDs, not message IDs, own gem5 output directories.
            return;
        }
        Map<String, String> parameters = (Map<String, String>) inputData.get(
                CommonSetupParameters.PROBLEM_CONFIG);
        if (parameters == null) {
            return;
        }
        String uniqueId = System.currentTimeMillis() + "_" + messageId;
        for (String key : new ArrayList<>(parameters.keySet())) {
            String value = parameters.get(key);
            if (value != null && value.contains("#")) {
                parameters.put(key, value.replace("#", uniqueId));
            }
        }
    }
}
