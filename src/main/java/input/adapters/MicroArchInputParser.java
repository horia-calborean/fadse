package input.adapters;

import input.ports.InputDataPort;
import input.ports.InputParser;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class MicroArchInputParser implements InputDataPort {
    protected final InputParser inputParser;

    public MicroArchInputParser(InputParser inputParser) {
        this.inputParser = inputParser;
    }

    public List<String> parseBenchmarksList() {
        NodeList benchmarksTag = xmlDocument.getElementsByTagName("benchmarks");

        List<String> benchmarksNames = new LinkedList<>();

        if (benchmarksTag != null && benchmarksTag.getLength() > 0) {
            NodeList itemTag = ((Element) benchmarksTag.item(0)).getElementsByTagName("item");

            for (int i = 0; i < itemTag.getLength(); i++) {
                String benchmarkName = itemTag.item(i).getAttributes().getNamedItem("name").getNodeValue();
                benchmarksNames.add(benchmarkName);
            }
        }

        return benchmarksNames;
    }

    public Map<String, String> parseSimulationParameters() {
        NodeList simulatorTag = xmlDocument.getElementsByTagName("simulator");
        NodeList simulatorParams = ((Element) simulatorTag.item(0)).getElementsByTagName("parameter");

        Map<String, String> parameters = new HashMap<>();

        for (int i = 0; i < simulatorParams.getLength(); i++) {
            NamedNodeMap parameter = simulatorParams.item(i).getAttributes();
            String name = parameter.getNamedItem("name").getNodeValue();
            String value = parameter.getNamedItem("value").getNodeValue();

            parameters.put(name, value);
        }

        return parameters;
    }
}