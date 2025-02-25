package input.adapters.parser.gap;

import input.adapters.extractor.gap.GAPJsonDataExtractor;
import input.adapters.extractor.gap.GAPXmlDataExtractor;
import input.adapters.parser.JsonInputParser;
import input.adapters.parser.XmlInputParser;
import input.ports.extractor.BenchmarkExtractor;
import input.ports.InputParser;
import input.ports.extractor.MetaheuristicExtractor;
import input.ports.parser.MicroArchInputParser;

import java.util.List;
import java.util.Map;

public class GAPInputParser extends MicroArchInputParser {

    public GAPInputParser(String filePath) {
        super(filePath);
    }

    @Override
    protected Object createExtractor(InputParser parser) {
        if (parser instanceof XmlInputParser) {
            return new GAPXmlDataExtractor((XmlInputParser) parser); // Extractor specific GAP
        } else if (parser instanceof JsonInputParser) {
            return new GAPJsonDataExtractor((JsonInputParser) parser);
        }
        throw new IllegalArgumentException("Error when creating GAP input data extractor");
    }

    public Map<String, String> getMetaheuristicData() {
        return ((MetaheuristicExtractor) dataExtractor).parseMetaheuristic();
    }

    public List<String> getBenchmarkList() {
        return ((BenchmarkExtractor) dataExtractor).parseBenchmarksList();
    }
}