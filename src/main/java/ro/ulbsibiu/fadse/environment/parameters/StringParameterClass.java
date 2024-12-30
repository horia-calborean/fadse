package ro.ulbsibiu.fadse.environment.parameters;

import jmetal.util.PseudoRandom;
import java.util.LinkedList;
import java.util.List;

public class StringParameterClass extends ParameterClass {

    private List<String> values;
    private String name;
    private String type;
    private String description;
    //private Int parameter;
    private int value_;
    private int lowerBound_;
    private int upperBound_;

    public StringParameterClass(String name, String type, String description) {
        init(new LinkedList<String>(), name, type, description, Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
    }

    public StringParameterClass(List<String> values, String name, String type, String description, int lower, int upper) {
        init(values, name, type, description, lower, upper, PseudoRandom.randInt(lower, upper));
    }

    public StringParameterClass(List<String> values, String name, String type, String description, int lower, int upper, int value) {
        init(values, name, type, description, lower, upper, value);
    }

    private void init(List<String> values, String name, String type, String description, int lower, int upper, int value) {
        this.values = values;
        this.name = name;
        this.type = type;
        this.description = description;
        this.value_ = value;
        this.upperBound_ = upper;
        this.lowerBound_ = lower;
        this.setName(name);
    }

    @Override
    public Object getValue() {
//        System.out.println(" value: "+values.get((int) parameter.getValue()) );
        return values.get(value_);
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
            value_ = pos;
        } else {
            throw new IllegalArgumentException(value + " is not in the legal values for this parameter");
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        //return super.clone();
        return new StringParameterClass(this.values, this.name, this.type, this.description, this.lowerBound_, this.upperBound_, this.value_);
    }

    @Override
    public double getLowerBound() { return lowerBound_; }

    @Override
    public double getUpperBound() { return upperBound_; }

    @Override
    public void setLowerBound(double lowerBound) {  lowerBound_ = (int)lowerBound; }

    @Override
    public void setUpperBound(double upperBound) { upperBound_ = (int)upperBound; }

    public String toString() {
        return "" + values.get(value_) + "";
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
        lowerBound_ = 0;
        upperBound_ = values.size()-1;
        this.values = values;
    }
}
