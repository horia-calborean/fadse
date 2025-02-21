package input.adapters;

import input.data.InputData;
import input.ports.InputParser;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import ro.ulbsibiu.fadse.environment.Objective;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public abstract class XmlParser implements InputParser {
    protected final Document xmlDocument;

    protected XmlParser(String xmlPath) {
        DocumentBuilder documentBuilder;

        try {
            documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            xmlDocument = documentBuilder.parse(new File(xmlPath));
        } catch (ParserConfigurationException | SAXException | IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public Document getDocument(){
        return xmlDocument;
    }

    public abstract InputData collectInputData();

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
            // data.put("config_path", getMetaheuristicAbsolutePath() + path);
        }

        return data;
    }

    public Map<String, Objective> parseObjectives() {
        NodeList systemMetrics = ((Element) xmlDocument.getElementsByTagName("system_metrics").item(0)).getElementsByTagName("system_metric");
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

    public String parseOutputPath() {
        NodeList outputNode = xmlDocument.getElementsByTagName("output");
        NamedNodeMap outputAttributes = outputNode.item(0).getAttributes();
        return outputAttributes.getNamedItem("output_path").getNodeValue();
    }
}