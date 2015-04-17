package ro.ulbsibiu.fadse.environment.parameters;

import java.util.LinkedList;
import java.util.List;
import jmetal.base.Variable;
import jmetal.base.variable.Int;

public class StringParameter implements Parameter {

    private List<String> values;
    private String name;
    private String type;
    private String description;
    private Int parameter;

    public StringParameter(String name, String type, String description) {
        init(new LinkedList<String>(), name, type, description, new Int());
    }

    public StringParameter(List<String> values, String name, String type, String description, Int parameter) {
        init(values, name, type, description, parameter);
    }

    private void init(List<String> values, String name, String type, String description, Int parameter) {
        this.values = values;
        this.name = name;
        this.type = type;
        this.description = description;
        this.parameter = parameter;
        parameter.setName(name);
    }

    public Object getValue() {
//        System.out.println(" value: "+values.get((int) parameter.getValue()) );
        return values.get((int) parameter.getValue());
    }

    public void setValue(Object value) {
        int pos = -1;
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).equalsIgnoreCase((String) value)) {
                pos = i;
            }
        }
        if (pos != -1) {
            parameter.setValue(pos);
        } else {
            throw new IllegalArgumentException(value + " is not in the legal values for this parameter");
        }
    }

    @Override
    public String toString() {
        return "" + values.get((int) parameter.getValue()) + "";
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(LinkedList<String> values) {
        parameter.setLowerBound(0);
        parameter.setUpperBound(values.size()-1);
        this.values = values;
    }

    public void setVariable(Variable v) {
        parameter = (Int) v;
    }

    public Variable getVariable() {
        return parameter;
    }
}
