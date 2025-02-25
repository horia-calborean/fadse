package input.adapters.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import input.ports.InputParser;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.File;
import java.io.IOException;

public class JsonInputParser implements InputParser {
    private final JsonNode jsonDocument;

    public JsonInputParser(String jsonPath) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            jsonDocument = objectMapper.readTree(new File(jsonPath));
        } catch (IOException e) {
            throw new RuntimeException("Eroare la citirea JSON-ului", e);
        }
    }

    @Override
    public JsonNode getDocument(){
        return jsonDocument;
    }
}