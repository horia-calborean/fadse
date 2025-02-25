package input.ports;

import input.adapters.parser.JsonInputParser;
import input.adapters.parser.XmlInputParser;

public class InputParserFactory {
    public static InputParser createParser(String filePath) {
        if (filePath.endsWith(".xml")) {
            return new XmlInputParser(filePath);
        } else if (filePath.endsWith(".json")) {
            return new JsonInputParser(filePath);
        }
        throw new IllegalArgumentException("Unknown file format " + filePath);
    }
}