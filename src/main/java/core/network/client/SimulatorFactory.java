package core.network.client;

import core.network.client.gap.GAPSimulator;
import input.model.InputData;
import input.model.setup.CommonSetupParameters;

import java.util.Objects;
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

        // If we reach here, simulator type is not supported
        String errorMsg = String.format(
            "Unknown simulator type: '%s'. Supported types: GAP",
            simulatorName
        );
        LOGGER.log(Level.SEVERE, errorMsg);
        throw new IllegalArgumentException(errorMsg);
    }
}