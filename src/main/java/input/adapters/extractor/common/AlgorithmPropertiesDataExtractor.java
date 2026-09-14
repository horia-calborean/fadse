package input.adapters.extractor.common;

import input.adapters.document.PropertiesInputDocument;
import input.adapters.extractor.PropertiesDataExtractor;
import input.ports.extractor.common.AlgorithmFieldsExtractor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AlgorithmPropertiesDataExtractor extends PropertiesDataExtractor implements AlgorithmFieldsExtractor {
    public AlgorithmPropertiesDataExtractor(PropertiesInputDocument propertiesDoc) {
        super(propertiesDoc);
    }

    @Override
    public Map<String, Object> extractData() {
        Map<String, Object> extractedData = new HashMap<>();

        Set<Map.Entry<Object, Object>> fileEntries = propertiesDocument.entrySet();

        for (Map.Entry<Object, Object> entry : fileEntries) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();

            extractedData.put(key, value);
        }

        return extractedData;
    }
}