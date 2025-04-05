package input.adapters.extractor;

import input.adapters.document.PropertiesInputDocument;

import java.util.Properties;

public class PropertiesDataExtractor {
    protected final Properties propertiesDocument;

    public PropertiesDataExtractor(PropertiesInputDocument propertiesDocument) {
        this.propertiesDocument = propertiesDocument.getDocument();
    }
}