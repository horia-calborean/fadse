package input.application.enhancer.gap;

import input.application.enhancer.InputDataEnhancer;
import input.model.InputData;
import input.model.setup.GapSetupParameters;

import java.util.List;

public class GapInputDataEnhancer implements InputDataEnhancer {
    @Override
    public void enhance(InputData inputData) {
        if (inputData.get(GapSetupParameters.FADSE_CLIENTS_FILE_PATH) != null) {
            String filePath = (String) inputData.get(GapSetupParameters.FADSE_CLIENTS_FILE_PATH);
            parseClientsFile(filePath, inputData);
        }
    }

    private void parseClientsFile(String filePath, InputData inputData) {
        // Logica de parsare a fișierului și adăugare în inputData
        System.out.println("Parsing clients file: " + filePath);
        inputData.set(GapSetupParameters.GAP_PARAMETERS, List.of("param1", "param2"));
    }
}