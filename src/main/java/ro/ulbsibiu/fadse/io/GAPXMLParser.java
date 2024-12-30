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
    private HashMap<String, Class<?>> types =  new HashMap<String, Class<?>>(){{
        put("integer", IntegerParameterClass.class);
        put("float", DoubleParameterClass.class);//
        put("exp2", Exp2ParameterClass.class);
        put("constant", ConstantParameterClass.class);//
        put("expression", ExpresionParameterClass.class);//
        put("permutation", PermutationParameterClass.class);//
        put("string", StringParameterClass.class);
        put("virtual", VirtualParameterClass.class);//
        put("checkpoint", CheckpointFileParameter.class);//
    }};

    private Object createInstanceFromKey(String key, Object[] args) {
        try {
            // Fetch the Class from the map based on the key
            Class<?> clazz = types.get(key);
            if (clazz != null) {
                // Get the constructor that matches the argument types
                // The args array contains the argument values, so we need their types
                Class<?>[] paramTypes = new Class<?>[args.length];
                for (int i = 0; i < args.length; i++) {
                    paramTypes[i] = args[i].getClass();
                }

                // Get the constructor with the specified parameter types
                Constructor<?> constructor = clazz.getDeclaredConstructor(paramTypes);

                // Create an instance using the constructor and pass the arguments
                return constructor.newInstance(args);
            } else {
                System.out.println("No class found for key: " + key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
            Parameter[] params = new Parameter[parameters.getLength()];
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

                Object[] args = {name, type, description};
                Object p = createInstanceFromKey(type, args);

                switch(type){
                    case "integer":
                        break;
                    case "string":
                        break;

                }
                //IMPORTANT SET STEP BEFORE SET MAX AND MIN
                int stepI = 1;//default step
                if (attributes.getNamedItem("step") != null) {
                    String step = attributes.getNamedItem("step").getNodeValue();
                    stepI = getValue(step, params);
                }

                //p.setStep(stepI);
                // Example of invoking a method without parameters
                //p.getClass().getMethod("setStep").invoke(p);

                //setStep requires parameters, which complicates things


                p.getClass().getMethod("setStep").invoke(p);

                String minValue = attributes.getNamedItem("min").getNodeValue();
                String maxValue = attributes.getNamedItem("max").getNodeValue();

                //p.setLowerBound(getValue(minValue, params));
                p.getClass().getMethod("setLowerBound").invoke(p);

                //p.setUpperBound(getValue(maxValue, params));
                p.getClass().getMethod("setUpperBound").invoke(p);

                int divideBy = 1;
                if (attributes.getNamedItem("divideBy") != null) {
                    String divide = attributes.getNamedItem("divideBy").getNodeValue();
                    divideBy = getValue(divide, params);
                }
                //p.setDivideBy(divideBy);


//                if (type.equalsIgnoreCase("integer")) {
//                    p = createIntegerParameter(name, type, description, parameter, params);
//                } else if (type.equalsIgnoreCase("string")) {
//                    p = createStringParameter(name, type, description, parameter);
//                } else if (type.equalsIgnoreCase("exp2")) {
//                    p = createExp2Parameter(name, type, description, parameter, params);
//                } else if (type.equalsIgnoreCase("permutation")) {
//                    p = createPermutationParameter(name, type, description, parameter, params);
//                } else if (type.equalsIgnoreCase("boolean")) {//it is an Integer parameter with 0/1 min/max value
//                    p = createBooleanParameter(name, "boolean", description, parameter);
//                } else if (type.equalsIgnoreCase("on_off_mask")) {
//                    System.err.println("Unsuported parameter type: " + type);
//                } else if (type.equalsIgnoreCase("float")) {
//                    p = createFloatParameter(name, "float", description, parameter, params);
//                } else {
//                    System.err.println("Unsuported parameter type: " + type);
//                }
                if (p != null) {
                    params[i] = p;
                }

                inputDoc.setParameters(params);
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
