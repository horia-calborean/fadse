package ro.ulbsibiu.fadse.simulationIO.xmlInput;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ro.ulbsibiu.fadse.simulationIO.parameters.simulator.SimulatorParameter;
import ro.ulbsibiu.fadse.simulationIO.parameters.simulator.impl.numeric.DoubleParameter;
import ro.ulbsibiu.fadse.simulationIO.parameters.simulator.impl.numeric.Exp2Parameter;
import ro.ulbsibiu.fadse.simulationIO.parameters.simulator.impl.numeric.IntegerParameter;
import ro.ulbsibiu.fadse.simulationIO.parameters.simulator.impl.special.PermutationParameter;
import ro.ulbsibiu.fadse.simulationIO.parameters.simulator.impl.special.StringParameter;
import ro.ulbsibiu.fadse.simulationIO.parameters.simulator.impl.special.VirtualParameter;
import simulation.parameter.NumericParameter;

import java.util.LinkedList;

public class XmlParameterReader {
    public XmlParameterReader() {
    }

    public NumericParameter createIntegerParameter(String name, String description, Node xmlParameterNode) {
        NamedNodeMap attributes = xmlParameterNode.getAttributes();

        int step;

        if (attributes.getNamedItem("step") != null) {
            String stepAsString = attributes.getNamedItem("step").getNodeValue();
            step = Integer.parseInt(stepAsString);
        }
        else{
            step = 1;
        }

        IntegerParameter integerParameter = new IntegerParameter(name, step);
        integerParameter.setDescription(description);

        String minValue = attributes.getNamedItem("min").getNodeValue();
        String maxValue = attributes.getNamedItem("max").getNodeValue();

        integerParameter.setLowerBound(Integer.parseInt(minValue));
        integerParameter.setUpperBound(Integer.parseInt(maxValue));

        int divideBy;

        if (attributes.getNamedItem("divideBy") != null) {
            String divideByAsString = attributes.getNamedItem("divideBy").getNodeValue();
            divideBy = Integer.parseInt(divideByAsString);
        }
        else{
            divideBy = 1;
        }

        integerParameter.setDivideBy(divideBy);

        return integerParameter;
    }

    public NumericParameter createBooleanParameter(String name, String description) {
        IntegerParameter booleanParameter = new IntegerParameter(name, 1);

        booleanParameter.setLowerBound(0);
        booleanParameter.setUpperBound(1);

        booleanParameter.setDescription(description);

        return booleanParameter;
    }

    public NumericParameter createExp2Parameter(String name, String description, Node xmlParameterNode) {
        NamedNodeMap attributes = xmlParameterNode.getAttributes();

        String minValue = attributes.getNamedItem("min").getNodeValue();
        String maxValue = attributes.getNamedItem("max").getNodeValue();

        int lowerBound = Integer.parseInt(minValue);
        int upperBound = Integer.parseInt(maxValue);

        Exp2Parameter exp2Parameter = new Exp2Parameter(name, lowerBound, upperBound);
        exp2Parameter.setDescription(description);

        return exp2Parameter;
    }

    public NumericParameter createDoubleParameter(String name, String description, Node xmlParameterNode) {
        NamedNodeMap attributes = xmlParameterNode.getAttributes();
        String minValue = attributes.getNamedItem("min").getNodeValue();
        String maxValue = attributes.getNamedItem("max").getNodeValue();

        double lowerBound = Integer.parseInt(minValue);
        double upperBound = Integer.parseInt(maxValue);

        DoubleParameter doubleParameter = new DoubleParameter(name, lowerBound, upperBound);
        doubleParameter.setDescription(description);

        return doubleParameter;
    }

    public SimulatorParameter createVirtualParameter(String name, String description, Node xmlParameterNode) {
        NamedNodeMap attributes = xmlParameterNode.getAttributes();

        String expression = attributes.getNamedItem("value").getNodeValue();
        VirtualParameter virtualParameter = new VirtualParameter(name, expression);
        virtualParameter.setDescription(description);

        return virtualParameter;
    }

    public SimulatorParameter createPermutationParameter(String name, String description, Node xmlParameterNode) {
        PermutationParameter permutationParameter = new PermutationParameter(name);

        NamedNodeMap attributes = xmlParameterNode.getAttributes();
        String size = attributes.getNamedItem("dimension").getNodeValue();
        int value = Integer.parseInt(size);
        permutationParameter.setSize(value);
        permutationParameter.setDescription(description);

        return permutationParameter;
    }

    public SimulatorParameter createStringParameter(String name, String description, Node xmlParameterNode) {


        NodeList items = ((Element) xmlParameterNode).getElementsByTagName("item");
        LinkedList<String> values = new LinkedList<>();

        for (int i = 0; i < items.getLength(); i++) {
            values.add(items.item(i).getAttributes().getNamedItem("value").getNodeValue());
        }

        StringParameter p = new StringParameter(name, values);
        p.setDescription(description);

        return p;
    }
}