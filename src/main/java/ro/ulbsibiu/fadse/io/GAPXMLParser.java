package ro.ulbsibiu.fadse.io;

import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import ro.ulbsibiu.fadse.environment.document.InputDocument;
import ro.ulbsibiu.fadse.environment.parameters.*;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class GAPXMLParser implements XMLInputReaderInterface{

    private enum GAPParameterType {IntegerParameterClass, DoubleParameterClass, Exp2ParameterClass, ConstantParameterClass,
        ExpressionParameterClass, PermutationParameterClass, StringParameterClass, VirtualParameterClass, CheckpointFileParameter};
    private final HashMap<String, Class<? extends ParameterClass>> parameterClassMap =  new HashMap<String, Class<? extends ParameterClass>>();

    public GAPXMLParser(){
        parameterClassMap.put("integer", IntegerParameterClass.class);
        parameterClassMap.put("float", DoubleParameterClass.class);//
        parameterClassMap.put("exp2", Exp2ParameterClass.class);
        parameterClassMap.put("constant", ConstantParameterClass.class);//
        parameterClassMap.put("expression", ExpresionParameterClass.class);//
        parameterClassMap.put("permutation", PermutationParameterClass.class);//
        parameterClassMap.put("string", StringParameterClass.class);
        parameterClassMap.put("virtual", VirtualParameterClass.class);//
        // parameterClassMap.put("checkpoint", CheckpointFileParameter.class);// not working
    }

    // Create a parameter object dynamically
    private ParameterClass createParameter(String name, String type, String description) throws Exception {
        Class<? extends ParameterClass> clazz = parameterClassMap.get(type);
        if (clazz != null) {
            return clazz.getDeclaredConstructor(String.class, String.class, String.class).newInstance(name, type, description);
        }
        throw new IllegalArgumentException("Unknown parameter type: " + type);
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
    public InputDocument parse(String xmlFilePath) {
        try {
            InputDocument inputDoc = new InputDocument();
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new File(xmlFilePath));

            // normalize text representation
            doc.getDocumentElement().normalize();
            //PARAMETERS

            NodeList parameters = ((Element) doc.getElementsByTagName("parameters").item(0)).getElementsByTagName("parameter");
            ParameterClass[] params = new ParameterClass[parameters.getLength()];
            for (int i = 0; i < parameters.getLength(); i++) {
                Node parameter = parameters.item(i);
                NamedNodeMap attributes = parameter.getAttributes();
                String name = attributes.getNamedItem("name").getNodeValue();
                String type = attributes.getNamedItem("type").getNodeValue();
                String description = "";
                //Parameter p = null;
                if (attributes.getNamedItem("description") != null) {
                    description = attributes.getNamedItem("description").getNodeValue();
                }

                ParameterClass p = createParameter(name, type, description);

                switch(type){
                    case "integer":
                        //IMPORTANT SET STEP BEFORE SET MAX AND MIN
                        int stepI = 1;//default step
                        if (attributes.getNamedItem("step") != null) {
                            String step = attributes.getNamedItem("step").getNodeValue();
                            stepI = getValue(step, params);
                        }
                        p.setStep(stepI); //not working
                        break;
                    case "string":
                        break;

                }

                String minValue = attributes.getNamedItem("min").getNodeValue();
                String maxValue = attributes.getNamedItem("max").getNodeValue();

                p.setLowerBound(getValue(minValue, params));
                p.setUpperBound(getValue(maxValue, params));
                //p.getClass().getMethod("setUpperBound").invoke(p);

                int divideBy = 1;
                if (attributes.getNamedItem("divideBy") != null) {
                    String divide = attributes.getNamedItem("divideBy").getNodeValue();
                    divideBy = getValue(divide, params);
                }
                p.setDivideBy(divideBy); //not working

                if (p != null) {
                    params[i] = p;
                }

                inputDoc.setParameters(params); //not working, needs to be changed in InputDoc class
            }
            return inputDoc;
        } catch (SAXParseException err) {
            System.out.println("** Parsing error" + ", line "
                    + err.getLineNumber() + ", uri " + err.getSystemId());
            System.out.println(" " + err.getMessage());

        } catch (SAXException e) {
            Exception x = e.getException();
            ((x == null) ? e : x).printStackTrace();

        } catch (Throwable t) {
            t.printStackTrace();
        }
        //System.exit (0);
        return null;
    }

}
