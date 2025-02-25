package input.adapters.collector.gap;

import input.adapters.extractor.gap.GAPJsonDataExtractor;
import input.adapters.extractor.gap.GAPXmlDataExtractor;
import input.adapters.document.JsonInputDocument;
import input.adapters.document.XmlInputDocument;
import input.adapters.parameter.GAPInputParameter;
import input.model.InputData;
import input.ports.extractor.BenchmarkExtractor;
import input.ports.document.InputDocument;
import input.ports.extractor.MetaheuristicExtractor;
import input.ports.collector.MicroArchInputCollector;

import java.util.List;
import java.util.Map;

public class GAPInputCollector extends MicroArchInputCollector {

    public GAPInputCollector(String filePath) {
        super(filePath);
    }

    @Override
    protected Object createExtractor(InputDocument inputDocument) {
        if (inputDocument instanceof XmlInputDocument) {
            return new GAPXmlDataExtractor((XmlInputDocument) inputDocument);
        } else if (inputDocument instanceof JsonInputDocument) {
            return new GAPJsonDataExtractor((JsonInputDocument) inputDocument);
        }
        throw new IllegalArgumentException("Error when creating GAP input data extractor");
    }

    @Override
    public InputData collectInputData() {
        InputData inputData = new InputData();

        Map<String, String> metaheuristicData = getMetaheuristicData();
        inputData.set(GAPInputParameter.METAHEURISTIC, metaheuristicData);

        List<String> benchmarkData = getBenchmarkList();
        inputData.set(GAPInputParameter.BENCHMARKS, benchmarkData);

        return inputData;
    }

    protected Map<String, String> getMetaheuristicData() {
        return ((MetaheuristicExtractor) dataExtractor).parseMetaheuristic();
    }

    protected List<String> getBenchmarkList() {
        return ((BenchmarkExtractor) dataExtractor).parseBenchmarksList();
    }
}