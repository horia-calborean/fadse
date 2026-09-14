package ro.ulbsibiu.fadse.io.parameters.gap;

import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import ro.ulbsibiu.fadse.environment.parameters.*;
import ro.ulbsibiu.fadse.io.parameters.ParameterParser;

import java.util.LinkedList;

public class GapParameterParser extends ParameterParser {
    public GapParameterParser() {

    }

    @Override
    protected void buildClassMap() {
        parameterClassMap.put("integer", IntegerParameter.class);
        parameterClassMap.put("float", DoubleParameter.class);
        parameterClassMap.put("exp2", Exp2Parameter.class);
        parameterClassMap.put("constant", ConstantParameter.class);
        parameterClassMap.put("expression", ExpresionParameter.class);
        parameterClassMap.put("permutation", PermutationParameter.class);
        parameterClassMap.put("string", StringParameter.class);
        parameterClassMap.put("virtual", VirtualParameter.class);
        // parameterClassMap.put("checkpoint", CheckpointFileParameter.class); TODO - Do it!
    }

    private int getValue(String value, Parameter[] params) {
        int valueI = 0;
        if (value.startsWith("@")) {//
            for (Parameter param : params) {
                if (param.getName().equalsIgnoreCase(value.substring(1))) {
                    valueI = (Integer) param.getValue();
                }
            }
        } else {
            valueI = Integer.parseInt(value);
        }
        return valueI;
    }

    @Override
    public Parameter[] parseParameters(Document dseXmlDocument) {
        NodeList parameters = ((Element) dseXmlDocument.getElementsByTagName("parameters").item(0)).getElementsByTagName("parameter");
        Parameter[] params = new Parameter[parameters.getLength()];

        try {
            for (int i = 0; i < parameters.getLength(); i++) {
                Node parameter = parameters.item(i);
                NamedNodeMap attributes = parameter.getAttributes();
                String name = attributes.getNamedItem("name").getNodeValue();
                String type = attributes.getNamedItem("type").getNodeValue();
                String description = "";

                if (attributes.getNamedItem("description") != null) {
                    description = attributes.getNamedItem("description").getNodeValue();
                }

                Parameter p = createParameter(name, type, description);
                String minValue = attributes.getNamedItem("min").getNodeValue();
                String maxValue = attributes.getNamedItem("max").getNodeValue();

                switch (type) {
                    case "integer":
                        //IMPORTANT SET STEP BEFORE SET MAX AND MIN
                        int stepI = 1;//default step
                        if (attributes.getNamedItem("step") != null) {
                            String step = attributes.getNamedItem("step").getNodeValue();
                            stepI = getValue(step, params);
                        }
                        p.setStep(stepI);

                        p.setLowerBound(getValue(minValue, params));
                        p.setUpperBound(getValue(maxValue, params));

                        int divideBy = 1;
                        if (attributes.getNamedItem("divideBy") != null) {
                            String divide = attributes.getNamedItem("divideBy").getNodeValue();
                            divideBy = getValue(divide, params);
                        }
                        p.setDivideBy(divideBy); //not working
                        break;
                    case "string":
                        NodeList items = ((Element) parameter).getElementsByTagName("item");
                        LinkedList<String> values = new LinkedList<>();
                        for (int j = 0; j < items.getLength(); j++) {
                            values.add(items.item(j).getAttributes().getNamedItem("value").getNodeValue());
                        }
                        p.setValues(values);
                        break;
                    case "exp2":
                        p.setLowerBound(getValue(minValue, params));
                        p.setUpperBound(getValue(maxValue, params));
                        break;
                }

                if (p != null) {
                    params[i] = p;
                }
            }
        } catch (SAXParseException err) {
            System.out.println("** Parsing error" + ", line "
                    + err.getLineNumber() + ", uri " + err.getSystemId());
            System.out.println(" " + err.getMessage());

        } catch (SAXException e) {
            Exception x = e.getException();
            ((x == null) ? e : x).fillInStackTrace();

        } catch (Throwable t) {
            t.fillInStackTrace();
        }

        return params;
    }
}