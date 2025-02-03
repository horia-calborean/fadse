package ro.ulbsibiu.fadse.io;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import ro.ulbsibiu.fadse.io.parser.*;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class MicroArchDseInputParser extends DseInputParser implements SimulatorNameParser, SimulationParametersParser, BenchmarksParser, MetaheuristicParser {
    public MicroArchDseInputParser(String dseXmlPath) {
        super(dseXmlPath);
    }

    @Override
    public String parseName() {
        NodeList simulatorTag = dseXmlDocument.getElementsByTagName("simulator");
        NamedNodeMap simulatorAttributes = simulatorTag.item(0).getAttributes();

        return simulatorAttributes.getNamedItem("name").getNodeValue();
    }

    @Override
    public String parseType() {
        NodeList simulatorTag = dseXmlDocument.getElementsByTagName("simulator");
        NamedNodeMap simulatorAttributes = simulatorTag.item(0).getAttributes();

        return simulatorAttributes.getNamedItem("type").getNodeValue();
    }

    @Override
    public Map<String, String> parseSimulationParameters() {
        NodeList simulatorTag = dseXmlDocument.getElementsByTagName("simulator");
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

    @Override
    public List<String> parseBenchmarksList() {
        NodeList benchmarksTag = dseXmlDocument.getElementsByTagName("benchmarks");

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

    @Override
    public Map<String, String> parseMetaheuristic() {
        NodeList metaheuristicNode = dseXmlDocument.getElementsByTagName("metaheuristic");
        NamedNodeMap attributes = metaheuristicNode.item(0).getAttributes();

        Map<String, String> data = new HashMap<>();

        String name = attributes.getNamedItem("name").getNodeValue();
        String path = attributes.getNamedItem("config_path").getNodeValue();

        data.put("name", name);

        if (Paths.get(path).isAbsolute()) {
            data.put("config_path", path);
        } else {
            data.put("config_path", getMetaheuristicAbsolutePath() + path);
        }

        return data;
    }

    public void parseSystemMetricsTag() {

    }

    public void parseOutputTag() {

    }
}