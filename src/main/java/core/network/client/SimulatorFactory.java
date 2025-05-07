package core.network.client;

import core.network.client.gap.GAPSimulator;
import input.model.InputData;
import input.model.setup.CommonSetupParameters;

public class SimulatorFactory {
    public static Simulator createSimulator(InputData inputData) {
        String simulatorName = (String) inputData.get(CommonSetupParameters.NAME);

        if ("GAP".equalsIgnoreCase(simulatorName)) {
            return new GAPSimulator(inputData);
        }

        throw new IllegalArgumentException("Unknown simulator type: " + simulatorName);
    }
}