package input.adapters.extractor;

import com.fasterxml.jackson.databind.JsonNode;
import input.adapters.document.JsonInputDocument;

public class JsonDataExtractor {
    protected final JsonNode jsonDocument;

    public JsonDataExtractor(JsonInputDocument jsonDoc) {
        jsonDocument = jsonDoc.getDocument();
    }
}