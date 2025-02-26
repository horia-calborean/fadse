package input.ports.extractor;

import java.util.Map;

public interface DatabaseExtractor {
    Map<String, String> parseDbConnectionData();
}