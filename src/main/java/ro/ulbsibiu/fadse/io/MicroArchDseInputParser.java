package ro.ulbsibiu.fadse.io;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ro.ulbsibiu.fadse.environment.Objective;
import ro.ulbsibiu.fadse.io.parser.*;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class MicroArchDseInputParser extends DseInputParser
        implements SimulatorNameParser, SimulationParametersParser, BenchmarksParser, MetaheuristicParser, SystemMetricParser, OutputPathParser {
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

    @Override
    public Map<String, Objective> parseObjectives() {
        NodeList systemMetrics = ((Element) dseXmlDocument.getElementsByTagName("system_metrics").item(0)).getElementsByTagName("system_metric");
        Map<String, Objective> objectives = new HashMap<>();
        for (int i = 0; i < systemMetrics.getLength(); i++) {
            Node metric = systemMetrics.item(i);
            NamedNodeMap attributes = metric.getAttributes();
            String name = attributes.getNamedItem("name").getNodeValue();
            String type = attributes.getNamedItem("type").getNodeValue();
            String unit = "";
            if (attributes.getNamedItem("unit") != null) {
                unit = attributes.getNamedItem("unit").getNodeValue();
            }
            String desired = "small";//default small if not specified
            if (attributes.getNamedItem("desired") != null) {
                desired = attributes.getNamedItem("desired").getNodeValue();
            }
            String description = "";
            if (attributes.getNamedItem("description") != null) {
                description = attributes.getNamedItem("description").getNodeValue();
            }
            Objective obj = new Objective(name, type, unit, description, !desired.equalsIgnoreCase("small"));
            objectives.put(name, obj);
        }

        return objectives;
    }

    @Override
    public String parseOutputPath() {
        NodeList outputNode = dseXmlDocument.getElementsByTagName("output");
        NamedNodeMap outputAttributes = outputNode.item(0).getAttributes();
        return outputAttributes.getNamedItem("output_path").getNodeValue();
    }
}