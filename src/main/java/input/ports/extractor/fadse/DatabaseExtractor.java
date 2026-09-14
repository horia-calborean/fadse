package input.ports.extractor.fadse;

import java.util.Map;

public interface DatabaseExtractor {
    Map<String, String> parseDbConnectionData();
}