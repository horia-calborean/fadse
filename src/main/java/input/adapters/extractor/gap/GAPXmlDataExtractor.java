package input.adapters.extractor.gap;

import input.adapters.parser.XmlInputParser;
import input.adapters.extractor.XmlDataExtractor;
import input.ports.extractor.BenchmarkExtractor;
import input.ports.extractor.MetaheuristicExtractor;

import java.util.List;
import java.util.Map;

public class GAPXmlDataExtractor extends XmlDataExtractor implements MetaheuristicExtractor, BenchmarkExtractor {
    public GAPXmlDataExtractor(XmlInputParser xmlParser) {
        super(xmlParser);
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