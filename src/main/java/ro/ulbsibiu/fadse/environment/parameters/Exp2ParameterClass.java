package ro.ulbsibiu.fadse.environment.parameters;

import jmetal.util.PseudoRandom;

public class Exp2ParameterClass extends ParameterClass {

    private String name;
    private String type;
    private String description;
    //private Int variable;
    private int value_;
    private int lowerBound_;
    private int upperBound_;

    public Exp2ParameterClass(String name, String type, String description) {
        init(name, type, description, 1, 6, PseudoRandom.randInt(1, 6));
    }

    public Exp2ParameterClass(String name, String type, String description, int lower, int upper) {
        init(name, type, description, lower, upper, PseudoRandom.randInt(lower, upper));
    }

    public Exp2ParameterClass(String name, String type, String description, int lower, int upper, int value) {
        init(name, type, description, lower, upper, value);
    }

    private void init(String name, String type, String description, int lower, int upper, int value) {
        this.name = name;
        this.type = type;
        this.description = description;
        lowerBound_ = lower;
        upperBound_ = upper;
        value_ = value;
        this.setName(name);
    }

    @Override
    public Object getValue() {
        return ((Double)(Math.pow(2,value_))).intValue();
    }

    @Override
    public void setValue(Object value) { value_ = (Integer) value; }

    @Override
    public Object clone() throws CloneNotSupportedException {
        //return super.clone();
        return new Exp2ParameterClass(this.name, this.type, this.description, this.lowerBound_, this.upperBound_, this.value_);
    }

    @Override
    public double getLowerBound() { return lowerBound_; }

    @Override
    public double getUpperBound() { return upperBound_; }

    @Override
    public void setLowerBound(double lowerBound) {  lowerBound_ = (int)lowerBound; }

    @Override
    public void setUpperBound(double upperBound) { upperBound_ = (int)upperBound; }

    public String toString() { return "" + value_ + ""; }

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

    /**
     * Calculate base 2 logarithm
     *
     * @param x value to take log of
     *
     * @return base 2 logarithm.
     */
    private double log2( double x )
        {
        // Math.log is base e, natural log, ln
        return Math.log( x ) / Math.log( 2 );
        }


}
