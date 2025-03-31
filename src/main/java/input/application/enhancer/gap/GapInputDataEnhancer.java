package input.application.enhancer.gap;

import input.application.enhancer.InputDataEnhancer;
import input.model.InputData;
import input.model.setup.GapSetupParameters;

import java.util.List;

public class GapInputDataEnhancer implements InputDataEnhancer {
    @Override
    public void expandDataFromFiles(InputData inputData) {
        if (inputData.get(GapSetupParameters.FADSE_CLIENTS_FILE_PATH) != null) {
            String filePath = (String) inputData.get(GapSetupParameters.FADSE_CLIENTS_FILE_PATH);
        }
    }
}