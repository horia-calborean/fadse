package input.application.enhancer;

import input.model.InputData;
import input.model.setup.SetupParameter;

public class InputDataFileEnhancer {
    public void enhance(InputData inputData) {
        for (SetupParameter param : inputData.getKeySet()) {
            InputDataEnhancer enhancer = InputDataEnhancerRegistry.getEnhancer(param);
            if (enhancer != null) {
                enhancer.enhance(inputData);
            }
        }
    }
}