package input.adapters.extractor;

import input.adapters.document.XmlInputDocument;
import org.w3c.dom.Document;

public class XmlDataExtractor {
    protected final Document xmlDocument;

    public XmlDataExtractor(XmlInputDocument xmlDoc) {
        xmlDocument = xmlDoc.getDocument();
    }
}
