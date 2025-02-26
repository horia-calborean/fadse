package input.adapters.extractor.gap;

import core.model.paths.PathUtils;
import input.adapters.document.XmlInputDocument;
import input.adapters.extractor.XmlDataExtractor;
import input.ports.extractor.*;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GAPXmlDataExtractor extends XmlDataExtractor implements MetaheuristicExtractor, BenchmarkExtractor, DatabaseExtractor, TypeExtractor, NameExtractor, SimulationParametersExtractor, OutputPathExtractor, ClientsExtractor {
    public GAPXmlDataExtractor(XmlInputDocument xmlDoc) {
        super(xmlDoc);
    }

    @Override
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

    @Override
    public Map<String, String> parseMetaheuristic() {
        NodeList metaheuristicNode = xmlDocument.getElementsByTagName("metaheuristic");
        NamedNodeMap attributes = metaheuristicNode.item(0).getAttributes();

        Map<String, String> data = new HashMap<>();

        String name = attributes.getNamedItem("name").getNodeValue();
        String path = attributes.getNamedItem("config_path").getNodeValue();

        data.put("name", name);

        if (Paths.get(path).isAbsolute()) {
            data.put("config_path", path);
        } else {
            data.put("config_path", PathUtils.getAlgorithmFileFullPath(path));
        }

        return data;
    }

    @Override
    public Map<String, String> parseDbConnectionData() {
        NodeList databaseNode = xmlDocument.getElementsByTagName("database");

        NamedNodeMap attributes = databaseNode.item(0).getAttributes();

        Map<String, String> data = new HashMap<>();

        String ip = attributes.getNamedItem("ip").getNodeValue();
        String port = attributes.getNamedItem("port").getNodeValue();
        String name = attributes.getNamedItem("name").getNodeValue();
        String user = attributes.getNamedItem("user").getNodeValue();
        String password = attributes.getNamedItem("password").getNodeValue();

        data.put("ip", ip);
        data.put("port", port);
        data.put("name", name);
        data.put("user", user);
        data.put("password", password);

        return data;
    }

    @Override
    public String extractType() {
        NodeList simulatorTag = xmlDocument.getElementsByTagName("simulator");
        NamedNodeMap simulatorAttributes = simulatorTag.item(0).getAttributes();

        return simulatorAttributes.getNamedItem("type").getNodeValue();
    }

    @Override
    public String extractName() {
        NodeList simulatorTag = xmlDocument.getElementsByTagName("simulator");
        NamedNodeMap simulatorAttributes = simulatorTag.item(0).getAttributes();

        return simulatorAttributes.getNamedItem("name").getNodeValue();
    }

    @Override
    public Map<String, String> extractParameters() {
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

    @Override
    public String extractOutputPath() {
        NodeList outputNode = xmlDocument.getElementsByTagName("output");
        NamedNodeMap outputAttributes = outputNode.item(0).getAttributes();
        return outputAttributes.getNamedItem("output_path").getNodeValue();
    }

    @Override
    public String extractClientsFilePath() {
        NodeList simulatorTag = xmlDocument.getElementsByTagName("simulator");
        NamedNodeMap simulatorAttributes = simulatorTag.item(0).getAttributes();

        return simulatorAttributes.getNamedItem("name").getNodeValue();
    }
}