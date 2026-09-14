package core.network.client.gap;

import core.network.client.Simulator;
import input.model.InputData;
import input.model.setup.CommonSetupParameters;

import java.util.Map;

public class GAPSimulator extends Simulator {
    public GAPSimulator(InputData inputData) {
        super(inputData);
        Map<String, String> problemConfigParameters = (Map<String, String>) inputData.get(CommonSetupParameters.PROBLEM_CONFIG);
        simulatorOutputFile = problemConfigParameters.get("simulator_output_file");
        simulatorOutputParser = new GAPOutputParser(this);
        simulatorRunner = new GAPRunner(this);
    }
}