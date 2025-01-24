package ro.ulbsibiu.fadse.environment.parameters;

import jmetal.util.PseudoRandom;
import java.util.LinkedList;
import java.util.List;

public class StringParameter extends Parameter {

    private List<String> values;
    //private Int parameter;
    private int value;
    private int lowerBound;
    private int upperBound;

    public StringParameter(String name, String type, String description) {
        super(name, type, description);
        init(new LinkedList<String>(), Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
    }

    public StringParameter(List<String> values, String name, String type, String description, int lower, int upper) {
        super(name, type, description);
        init(values, lower, upper, PseudoRandom.randInt(lower, upper));
    }

    public StringParameter(List<String> values, String name, String type, String description, int lower, int upper, int value) {
        super(name, type, description);
        init(values, lower, upper, value);
    }

    private void init(List<String> values, int lower, int upper, int value) {
        this.values = values;
        this.value = value;
        this.upperBound = upper;
        this.lowerBound = lower;
    }

    @Override
    public Object getValue() {
//        System.out.println(" value: "+values.get((int) parameter.getValue()) );
        return values.get(value);
    }

    @Override
    public void setValue(Object value) {
        int pos = -1;
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).equalsIgnoreCase((String) value)) {
                pos = i;
            }
        }
        if (pos != -1) {
            this.value = pos;
        } else {
            throw new IllegalArgumentException(value + " is not in the legal values for this parameter");
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        //return super.clone();
        return new StringParameter(this.values, this.getName(), this.getType(), this.getDescription(), this.lowerBound, this.upperBound, this.value);
    }

    @Override
    public double getLowerBound() { return this.lowerBound; }

    @Override
    public double getUpperBound() { return this.upperBound; }

    @Override
    public void setLowerBound(double lowerBound) {  this.lowerBound = (int)lowerBound; }

    @Override
    public void setUpperBound(double upperBound) { this.upperBound = (int)upperBound; }

    @Override
    public String toString() { return "" + values.get(this.value) + ""; }

    @Override
    public List<String> getValues() {
        return values;
    }

    @Override
    public void setValues(LinkedList<String> values) {
        this.lowerBound = 0;
        this.upperBound = values.size()-1;
        this.values = values;
    }
}
