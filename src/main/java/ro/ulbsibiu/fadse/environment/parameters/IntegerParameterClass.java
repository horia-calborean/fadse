package ro.ulbsibiu.fadse.environment.parameters;

import jmetal.util.PseudoRandom;

public class IntegerParameterClass extends ParameterClass
{
    private int step = 1;
    private String name;
    private String type;
    private String description;
    private int divideBy = 1;
    //private Int variable;
    private int value_;       //Stores the value of the variable
    private int lowerBound_;  //Stores the lower limit of the variable
    private int upperBound_;  //Stores the upper limit of the variable

    public IntegerParameterClass(String name, String type, String description) {
        init(name, type, description, Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
    }

    public IntegerParameterClass(String name, String type, String description, int lower, int upper) {
        init(name, type, description, lower, upper, PseudoRandom.randInt(lower, upper));
    }

    public IntegerParameterClass(String name, String type, String description, int lower, int upper, int value) {
        init(name, type, description, lower, upper, value);
    }
    private void init(String name, String type, String description, int lowerBound, int upperBound, int value) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.value_ = value;
        this.lowerBound_ = lowerBound;
        this.upperBound_ = upperBound;
        this.setName(name);
    }

    @Override
    public Object getValue() {
        return ((Double)((double)value_*step)).intValue();
    }

    @Override
    public void setValue(Object value) {
        value_ = (int)value;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        //return super.clone();
        return new IntegerParameterClass(this.name, this.type, this.description, this.value_, this.lowerBound_, this.upperBound_);
    }

    @Override
    public double getLowerBound() { return lowerBound_; }

    @Override
    public double getUpperBound() { return upperBound_; }

    @Override
    public void setLowerBound(double lowerBound) {  lowerBound_ = ((int)lowerBound)/step; }

    @Override
    public void setUpperBound(double upperBound) { upperBound_ = ((int)upperBound)/step; }

    public String toString() {
        return "" + value_ + "";
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() { return name; }

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

    public void setDivideBy(int divideBy) { this.divideBy = divideBy; }

    public int getDivideBy() {
        return divideBy;
    }
}
