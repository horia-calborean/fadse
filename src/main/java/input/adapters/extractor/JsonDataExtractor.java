package input.adapters.extractor;

import com.fasterxml.jackson.databind.JsonNode;
import input.adapters.parser.JsonInputParser;

public class JsonDataExtractor {
    protected final JsonNode jsonDocument;

    public JsonDataExtractor(JsonInputParser jsonParser) {
        jsonDocument = jsonParser.getDocument();
    }
}