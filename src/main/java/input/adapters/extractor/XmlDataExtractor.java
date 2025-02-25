package input.adapters.extractor;

import input.adapters.parser.XmlInputParser;
import org.w3c.dom.Document;

public class XmlDataExtractor {
    protected final Document xmlDocument;

    public XmlDataExtractor(XmlInputParser xmlParser) {
        xmlDocument = xmlParser.getDocument();
    }
}
