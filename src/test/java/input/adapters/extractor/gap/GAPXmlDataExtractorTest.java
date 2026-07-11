package input.adapters.extractor.gap;

import input.adapters.document.XmlInputDocument;
import input.adapters.parameter.numerics.type.IntegerParameter;
import input.ports.parameter.problem.ProblemParameter;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class GAPXmlDataExtractorTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void extractsIntegerParameterStepFromXml() throws Exception {
        File xmlFile = writeXml(
                "<simulator>"
                        + "<parameters>"
                        + "<parameter name=\"width\" type=\"integer\" min=\"2\" max=\"8\" step=\"2\"/>"
                        + "</parameters>"
                        + "</simulator>"
        );
        GAPXmlDataExtractor extractor = new GAPXmlDataExtractor(new XmlInputDocument(xmlFile.getAbsolutePath()));

        ProblemParameter<?>[] parameters = extractor.extractParameters();

        assertEquals(1, parameters.length);
        assertTrue(parameters[0] instanceof IntegerParameter);
        assertEquals(2, ((IntegerParameter) parameters[0]).getStep());
    }

    @Test
    public void rejectsZeroNegativeAndFractionalIntegerSteps() throws Exception {
        for (String step : new String[] { "0", "-1", "0.5" }) {
            File xmlFile = writeXmlWithName(
                    "gap-" + step.replace('-', 'n').replace('.', '_') + ".xml",
                    "<simulator><parameters>"
                            + "<parameter name=\"width\" type=\"integer\" min=\"2\" max=\"8\" step=\""
                            + step + "\"/>"
                            + "</parameters></simulator>"
            );
            GAPXmlDataExtractor extractor = new GAPXmlDataExtractor(
                    new XmlInputDocument(xmlFile.getAbsolutePath())
            );
            assertThrows(IllegalArgumentException.class, extractor::extractParameters);
        }
    }

    @Test
    public void snappedIntegerValuesNeverExceedIncompatibleUpperBound() throws Exception {
        File xmlFile = writeXml(
                "<simulator><parameters>"
                        + "<parameter name=\"width\" type=\"integer\" min=\"1\" max=\"20\" step=\"4\"/>"
                        + "</parameters></simulator>"
        );
        IntegerParameter parameter = (IntegerParameter) new GAPXmlDataExtractor(
                new XmlInputDocument(xmlFile.getAbsolutePath())
        ).extractParameters()[0];

        parameter.setValueFromDouble(20);

        assertEquals(Integer.valueOf(17), parameter.getValue());
    }

    private File writeXml(String contents) throws IOException {
        return writeXmlWithName("gap.xml", contents);
    }

    private File writeXmlWithName(String name, String contents) throws IOException {
        File xmlFile = temporaryFolder.newFile(name);
        try (FileWriter writer = new FileWriter(xmlFile)) {
            writer.write(contents);
        }
        return xmlFile;
    }
}
