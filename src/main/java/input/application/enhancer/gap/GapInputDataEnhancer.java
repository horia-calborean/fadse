package input.application.enhancer.gap;

import core.model.clients.ListOfFadseClients;
import core.model.paths.PathUtils;
import input.adapters.document.InputDocumentFactory;
import input.adapters.document.JsonInputDocument;
import input.adapters.document.XmlInputDocument;
import input.adapters.extractor.clients.ClientsXmlDataExtractor;
import input.adapters.extractor.gap.GAPJsonDataExtractor;
import input.adapters.extractor.gap.GAPXmlDataExtractor;
import input.application.enhancer.InputDataEnhancer;
import input.model.InputData;
import input.model.setup.GapSetupParameters;
import input.ports.document.InputDocument;
import input.ports.extractor.fadse.ClientsExtractor;

import java.nio.file.Paths;
import java.util.List;

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
    }

    protected Object createExtractor(InputDocument inputDocument) {
        if (inputDocument instanceof XmlInputDocument) {
            return new ClientsXmlDataExtractor((XmlInputDocument) inputDocument);
        }
        throw new IllegalArgumentException("Error when creating GAP input data extractor");
    }
}