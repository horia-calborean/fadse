package input.adapters.document;

import input.ports.document.InputDocument;

public class InputDocumentFactory {
    public static InputDocument createDocument(String filePath) {
        if (filePath.endsWith(".xml")) {
            return new XmlInputDocument(filePath);
        } else if (filePath.endsWith(".json")) {
            return new JsonInputDocument(filePath);
        }
        throw new IllegalArgumentException("Unknown file format " + filePath);
    }
}