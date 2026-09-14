package input.adapters.extractor.gap;

import input.adapters.document.JsonInputDocument;
import input.adapters.extractor.JsonDataExtractor;
import input.ports.extractor.common.BenchmarkExtractor;
import input.ports.extractor.common.MetaheuristicExtractor;

import java.util.List;
import java.util.Map;

public class GAPJsonDataExtractor extends JsonDataExtractor implements MetaheuristicExtractor, BenchmarkExtractor {
    public GAPJsonDataExtractor(JsonInputDocument jsonDoc) {
        super(jsonDoc);
    }

    @Override
    public List<String> extractBenchmarksList() {
        return null;
    }

    @Override
    public Map<String, String> parseMetaheuristic() {
        return null;
    }
}