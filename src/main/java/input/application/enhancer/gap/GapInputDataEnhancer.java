package input.application.enhancer.gap;

import core.model.clients.ListOfFadseClients;
import core.model.paths.PathUtils;
import input.adapters.document.InputDocumentFactory;
import input.adapters.document.PropertiesInputDocument;
import input.adapters.document.XmlInputDocument;
import input.adapters.extractor.common.AlgorithmPropertiesDataExtractor;
import input.adapters.extractor.fadse.ClientsXmlDataExtractor;
import input.application.enhancer.InputDataEnhancer;
import input.model.InputData;
import input.model.setup.GapSetupParameters;
import input.ports.document.InputDocument;
import input.ports.extractor.fadse.ClientsExtractor;

import java.util.Map;

@SuppressWarnings("unchecked cast")
public class GapInputDataEnhancer implements InputDataEnhancer {
    @Override
    public void expandDataFromFiles(InputData inputData) {
        String filePath;
        InputDocument inputDocument;
        Object extractor;

        filePath = (String) inputData.get(GapSetupParameters.FADSE_CLIENTS_FILE_PATH);

        if (filePath != null) {
            filePath = PathUtils.getAbsolutePath(filePath);
            inputDocument = InputDocumentFactory.createDocument(filePath);
            extractor = createExtractor(inputDocument);
            ListOfFadseClients listOfClients = ((ClientsExtractor) extractor).extractClientsData();
            inputData.set(GapSetupParameters.FADSE_CLIENTS, listOfClients);
        }

        Map<String, String> metaheuristicInfo = (Map<String, String>) inputData.get(GapSetupParameters.METAHEURISTIC);
        filePath = metaheuristicInfo.get("config_path");

        if(filePath != null) {
            filePath = PathUtils.getAbsolutePath(filePath);
            inputDocument = InputDocumentFactory.createDocument(filePath);
            extractor = createExtractor(inputDocument);
            Map<String, Object> algorithmData = ((AlgorithmPropertiesDataExtractor)extractor).extractData();
            inputData.set(GapSetupParameters.METAHEURISTIC_DATA, algorithmData);
        }
    }

    protected Object createExtractor(InputDocument inputDocument) {
        if (inputDocument instanceof XmlInputDocument) {
            return new ClientsXmlDataExtractor((XmlInputDocument) inputDocument);
        } else if (inputDocument instanceof PropertiesInputDocument) {
            return new AlgorithmPropertiesDataExtractor((PropertiesInputDocument) inputDocument);
        }
        throw new IllegalArgumentException("Error when creating GAP input data extractor");
    }
}