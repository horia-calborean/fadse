package input.adapters.extractor.gap;

import core.model.paths.PathUtils;
import input.adapters.document.XmlInputDocument;
import input.adapters.extractor.XmlDataExtractor;
import input.ports.extractor.BenchmarkExtractor;
import input.ports.extractor.MetaheuristicExtractor;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GAPXmlDataExtractor extends XmlDataExtractor implements MetaheuristicExtractor, BenchmarkExtractor {
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
}