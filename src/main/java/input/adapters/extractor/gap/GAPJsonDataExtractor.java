package input.adapters.extractor.gap;

import input.adapters.parser.JsonInputParser;
import input.adapters.extractor.JsonDataExtractor;
import input.ports.extractor.BenchmarkExtractor;
import input.ports.extractor.MetaheuristicExtractor;

import java.util.List;
import java.util.Map;

public class GAPJsonDataExtractor extends JsonDataExtractor implements MetaheuristicExtractor, BenchmarkExtractor {
    public GAPJsonDataExtractor(JsonInputParser jsonParser) {
        super(jsonParser);
    }

    @Override
    public List<String> parseBenchmarksList() {
        return null;
    }

    @Override
    public Map<String, String> parseMetaheuristic() {
        return null;
    }
}