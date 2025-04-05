package input.adapters.document;

import input.ports.document.InputDocument;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesInputDocument implements InputDocument {
    protected final Properties propertiesDocument;

    public PropertiesInputDocument(String propertiesPath) {
        propertiesDocument = new Properties();

        try (FileInputStream fileInputStream = new FileInputStream(propertiesPath)) {
            propertiesDocument.load(fileInputStream);
        } catch (IOException e) {
            throw new RuntimeException("Error while loading properties file", e);
        }
    }

    @Override
    public Properties getDocument() {
        return propertiesDocument;
    }
}