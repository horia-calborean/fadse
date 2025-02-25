package input.ports.collector;

import input.model.InputData;
import input.ports.InputDocumentFactory;
import input.ports.document.InputDocument;

public abstract class MicroArchInputCollector {
    protected final InputDocument inputDocument;
    protected final Object dataExtractor;

    public MicroArchInputCollector(String filePath) {
        inputDocument = InputDocumentFactory.createParser(filePath);
        dataExtractor = createExtractor(inputDocument);
    }

    protected abstract Object createExtractor(InputDocument parser);

    public abstract InputData collectInputData();
}