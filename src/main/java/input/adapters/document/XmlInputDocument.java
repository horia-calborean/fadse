package input.adapters.document;

import input.ports.document.InputDocument;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class XmlInputDocument implements InputDocument {
    protected final Document xmlDocument;

    public XmlInputDocument(String xmlPath) {
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
}