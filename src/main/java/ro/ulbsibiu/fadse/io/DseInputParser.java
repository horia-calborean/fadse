package ro.ulbsibiu.fadse.io;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import ro.ulbsibiu.fadse.environment.document.InputDocument;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;

public abstract class DseInputParser {
    protected final Document dseXmlDocument;
    protected InputDocument simulationInput;

    public DseInputParser(String dseXmlPath) {
        DocumentBuilder documentBuilder;

        try {
            documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            dseXmlDocument = documentBuilder.parse(new File(dseXmlPath));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }

        dseXmlDocument.getDocumentElement().normalize();

        simulationInput = new InputDocument();
    }

    protected String getMetaheuristicAbsolutePath() {
        String separator = FileSystems.getDefault().getSeparator();

        return separator + "configs" +
                separator + "metaheuristicConfig" +
                separator;
    }

    protected abstract void collectSimulationInput();

    public InputDocument getSimulationInput() {
        return simulationInput;
    }
}