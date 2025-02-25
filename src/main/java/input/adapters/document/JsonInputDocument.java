package input.adapters.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import input.ports.document.InputDocument;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.File;
import java.io.IOException;

public class JsonInputDocument implements InputDocument {
    private final JsonNode jsonDocument;

    public JsonInputDocument(String jsonPath) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            jsonDocument = objectMapper.readTree(new File(jsonPath));
        } catch (IOException e) {
            throw new RuntimeException("Error while loading json file", e);
        }
    }

    @Override
    public JsonNode getDocument(){
        return jsonDocument;
    }
}