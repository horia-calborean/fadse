package ro.ulbsibiu.fadse.environment.parameters;

import jmetal.base.Variable;
import jmetal.base.variable.Int;
import jmetal.base.variable.Permutation;

public class PermutationParameter implements Parameter {

    private int size = 2;
    private String name;
    private String type;
    private String description;
    private Permutation parameter;

    public PermutationParameter(String name, String type, String description) {
        init(name, type, description, new Permutation(size));
    }

    public PermutationParameter(String name, String type, String description, Permutation parameter) {
       init(name, type, description, parameter);
    }
    private void init(String name, String type, String description, Permutation parameter) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.parameter = parameter;
        parameter.setName(name);
    }

    public Object getValue() {//TODO test it
        return parameter.toString();
    }

    public void setValue(Object value) {
        //TODO what to do???
    }

    @Override
    public String toString() {
        return "" + parameter.toString() + "";
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

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setVariable(Variable v) {
        parameter =  (Permutation) v;
    }

    public Variable getVariable() {
         return parameter;
    }

}
