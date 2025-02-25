package input.ports;

import input.adapters.document.JsonInputDocument;
import input.adapters.document.XmlInputDocument;
import input.ports.document.InputDocument;

public class InputDocumentFactory {
    public static InputDocument createParser(String filePath) {
        if (filePath.endsWith(".xml")) {
            return new XmlInputDocument(filePath);
        } else if (filePath.endsWith(".json")) {
            return new JsonInputDocument(filePath);
        }
        throw new IllegalArgumentException("Unknown file format " + filePath);
    }
}