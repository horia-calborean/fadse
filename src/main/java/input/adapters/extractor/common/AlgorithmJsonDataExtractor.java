package input.adapters.extractor.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import input.adapters.document.JsonInputDocument;
import input.ports.extractor.common.AlgorithmFieldsExtractor;

import java.io.IOException;
import java.util.Map;

public class AlgorithmJsonDataExtractor implements AlgorithmFieldsExtractor {
    private final JsonInputDocument jsonInputDocument;
    private final ObjectMapper mapper = new ObjectMapper();

    public AlgorithmJsonDataExtractor(JsonInputDocument jsonInputDocument) {
        this.jsonInputDocument = jsonInputDocument;
    }

    @Override
    public Map<String, Object> extractData() {
        JsonNode root = jsonInputDocument.getDocument();
        return mapper.convertValue(root, new TypeReference<Map<String, Object>>() {});
    }
}
