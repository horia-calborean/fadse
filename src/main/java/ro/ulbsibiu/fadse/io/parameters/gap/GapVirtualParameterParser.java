package ro.ulbsibiu.fadse.io.parameters.gap;

import org.w3c.dom.*;
import ro.ulbsibiu.fadse.environment.parameters.*;
import ro.ulbsibiu.fadse.io.parameters.ParameterParser;

public class GapVirtualParameterParser extends ParameterParser {
    public GapVirtualParameterParser() {

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

    @Override
    public Parameter[] parseParameters(Document dseXmlDocument) {
        System.out.println("EXTRACTING THE VIRTUAL PARAMS");
        Parameter[] virtualParams = new Parameter[0];

        try {
            NodeList virtualParameters = ((Element) dseXmlDocument.getElementsByTagName("virtual_parameters").item(0)).getElementsByTagName("parameter");
            virtualParams = new Parameter[virtualParameters.getLength()];

            for (int i = 0; i < virtualParameters.getLength(); i++) {
                Node parameter = virtualParameters.item(i);
                NamedNodeMap attributes = parameter.getAttributes();
                String name = attributes.getNamedItem("name").getNodeValue();
                String description = "";
                Parameter p = createParameter(name, "virtual", description);
                if (p != null) {
                    virtualParams[i] = p;
                }
            }

            System.out.println("FOUND: " + virtualParams.length);

        } catch (Exception e) {
            System.out.println("Problem at the virtual parameters (not fatal if you are not using them): " + e.getMessage());
        }

        return virtualParams;
    }
}