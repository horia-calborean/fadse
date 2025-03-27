package input.adapters.extractor.gap;

import core.model.paths.PathUtils;
import input.adapters.document.XmlInputDocument;
import input.adapters.extractor.XmlDataExtractor;
import input.adapters.parameter.problem.gap.GapParameterFactory;
import input.adapters.parameter.setup.GapSetupParameters;
import input.model.NumberParser;
import input.ports.extractor.common.BenchmarkExtractor;
import input.ports.extractor.common.MetaheuristicExtractor;
import input.ports.extractor.fadse.ClientsExtractor;
import input.ports.extractor.fadse.DatabaseExtractor;
import input.ports.extractor.gap.*;
import input.ports.parameter.problem.ProblemParameter;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GAPXmlDataExtractor extends XmlDataExtractor implements MetaheuristicExtractor, BenchmarkExtractor, DatabaseExtractor, TypeExtractor, NameExtractor, GapConfigExtractor, OutputPathExtractor, ClientsExtractor, GapParametersExtractor {
    public GAPXmlDataExtractor(XmlInputDocument xmlDoc) {
        super(xmlDoc);
    }

    @Override
    public Map<String, String> extractGapConfigParameters() {
        String tagName = GapSetupParameters.GAP_CONFIG.getName();
        NodeList simulatorTag = xmlDocument.getElementsByTagName(tagName);
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
    public ProblemParameter<?>[] extractParameters() throws Exception {
        String tagName = GapSetupParameters.GAP_PARAMETERS.getName();

        NodeList parametersXmlNode = ((Element) xmlDocument.getElementsByTagName(tagName).item(0)).getElementsByTagName("parameter");

        int noOfParameters = parametersXmlNode.getLength();

        ProblemParameter<?>[] problemParameters = new ProblemParameter<?>[noOfParameters];


            for (int parameterIndex = 0; parameterIndex < noOfParameters; parameterIndex++) {
                Node xmlParameterNode = parametersXmlNode.item(parameterIndex);
                NamedNodeMap attributes = xmlParameterNode.getAttributes();

                if (attributes.getNamedItem("type") == null) {
                    throw new Exception("type was not specified for the parameter at index " + parameterIndex);
                }

                String typeName = attributes.getNamedItem("type").getNodeValue();

                String numberStr;

                if (attributes.getNamedItem("min") == null) {
                    throw new Exception("min was not specified for the parameter at index " + parameterIndex);
                }

                numberStr = attributes.getNamedItem("min").getNodeValue();
                Number min = NumberParser.parse(numberStr);

                if (attributes.getNamedItem("max") == null) {
                    throw new Exception("max was not specified for the parameter at index " + parameterIndex);
                }

                numberStr = attributes.getNamedItem("max").getNodeValue();
                Number max = NumberParser.parse(numberStr);

                ProblemParameter<?> parameter = GapParameterFactory.createParameter(typeName, min, max);

                String name = "";
                String description = "";

                if (attributes.getNamedItem("name") != null) {
                    name = attributes.getNamedItem("name").getNodeValue();
                }

                if (attributes.getNamedItem("description") != null) {
                    description = attributes.getNamedItem("description").getNodeValue();
                }

                parameter.setName(name);
                parameter.setDescription(description);

                // TODO - Is it necessary to set step for Integer and exp for Exponential ?

                problemParameters[parameterIndex] = parameter;
            }

        return problemParameters;
    }

    @Override
    public List<String> extractBenchmarksList() {
        String tagName = GapSetupParameters.BENCHMARKS.getName();
        NodeList benchmarksTag = xmlDocument.getElementsByTagName(tagName);

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
        String tagName = GapSetupParameters.METAHEURISTIC.getName();
        NodeList metaheuristicNode = xmlDocument.getElementsByTagName(tagName);
        NamedNodeMap attributes = metaheuristicNode.item(0).getAttributes();

        Map<String, String> data = new HashMap<>();

        String name = attributes.getNamedItem("name").getNodeValue();
        String path = attributes.getNamedItem("config_path").getNodeValue();

        data.put("name", name);

        if (Paths.get(path).isAbsolute()) {
            data.put("config_path", path);
        } else {
            data.put("config_path", PathUtils.getAlgorithmFullFilePath(path));
        }

        return data;
    }

    @Override
    public Map<String, String> parseDbConnectionData() {
        String tagName = GapSetupParameters.DATABASE.getName();
        NodeList databaseNode = xmlDocument.getElementsByTagName(tagName);

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
        String tagName = GapSetupParameters.GAP_CONFIG.getName();
        NodeList simulatorTag = xmlDocument.getElementsByTagName(tagName);
        NamedNodeMap simulatorAttributes = simulatorTag.item(0).getAttributes();

        return simulatorAttributes.getNamedItem("type").getNodeValue();
    }

    @Override
    public String extractName() {
        String tagName = GapSetupParameters.GAP_CONFIG.getName();
        NodeList simulatorTag = xmlDocument.getElementsByTagName(tagName);
        NamedNodeMap simulatorAttributes = simulatorTag.item(0).getAttributes();

        return simulatorAttributes.getNamedItem("name").getNodeValue();
    }

    @Override
    public String extractOutputPath() {
        String tagName = GapSetupParameters.OUTPUT_PATH.getName();
        NodeList outputNode = xmlDocument.getElementsByTagName(tagName);
        NamedNodeMap outputAttributes = outputNode.item(0).getAttributes();
        return outputAttributes.getNamedItem("path").getNodeValue();
    }

    @Override
    public String extractClientsFilePath() {
        String tagName = GapSetupParameters.FADSE_CLIENTS_FILE_PATH.getName();
        NodeList simulatorTag = xmlDocument.getElementsByTagName(tagName);
        NamedNodeMap simulatorAttributes = simulatorTag.item(0).getAttributes();

        String fileName = simulatorAttributes.getNamedItem("fileName").getNodeValue();
        return PathUtils.getFadseClientsFullFilePath(fileName);
    }
}