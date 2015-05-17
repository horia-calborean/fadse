package ro.ulbsibiu.fadse.environment.parameters;

import jmetal.base.Variable;
import jmetal.base.variable.Int;

public class IntegerParameter implements Parameter {

    private int step = 1;
    private String name;
    private String type;
    private String description;
    private Int variable;
    private int divideBy = 1;

    public IntegerParameter(String name, String type, String description) {
        init(name, type, description, new Int(0,1));
    }

    public IntegerParameter(String name, String type, String description, Int parameter) {
       init(name, type, description, parameter);
    }
    private void init(String name, String type, String description, Int parameter) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.variable = parameter;
        parameter.setName(name);
    }

    public Object getValue() {
        int value = 0;
//        if (step == 1) {
//            value = (int) parameter.getValue();
//        } else if (parameter.getValue() == parameter.getLowerBound()) {
//            value = (int) parameter.getLowerBound();
//        } else if (parameter.getValue() == 0) {
//            value = 0;
//        } else {
//            value = (int) (parameter.getValue() * step - (step - 1));
//        }
//        int n = (int) ((parameter.getValue() - parameter.getLowerBound()) / step);
//        value = (int) (parameter.getLowerBound() + (n)*step);
//        return value;
        return ((Double)(variable.getValue()*step)).intValue();
    }

    public void setValue(Object value) {
        this.variable.setValue((Integer) value);
    }

    @Override
    public String toString() {
        return "" + variable.getValue() + "";
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

    public int getMaxValue() {
        return (int) variable.getUpperBound();
    }

    public void setMaxValue(int maxValue) {
        this.variable.setUpperBound(maxValue/step);
    }

    public int getMinValue() {
        return (int) variable.getLowerBound();
    }

    public void setMinValue(int minValue) {
        this.variable.setLowerBound(minValue/step);
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

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public void setVariable(Variable v) {
        variable = (Int) v;
    }

    public Variable getVariable() {
         return variable;
    }

    public void setDivideBy(int divideBy) {
        this.divideBy = divideBy;
    }

    public int getDivideBy() {
         return divideBy;
    }
}
