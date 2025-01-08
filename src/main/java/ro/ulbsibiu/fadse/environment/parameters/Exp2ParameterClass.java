package ro.ulbsibiu.fadse.environment.parameters;

import jmetal.util.PseudoRandom;

public class Exp2ParameterClass extends ParameterClass {

    //private Int variable;
    private int value;
    private int lowerBound;
    private int upperBound;

    public Exp2ParameterClass(String name, String type, String description) {
        super(name, type, description);
        init(1, 6, PseudoRandom.randInt(1, 6));
    }

    public Exp2ParameterClass(String name, String type, String description, int lower, int upper) {
        super(name, type, description);
        init(lower, upper, PseudoRandom.randInt(lower, upper));
    }

    public Exp2ParameterClass(String name, String type, String description, int lower, int upper, int value) {
        super(name, type, description);
        init(lower, upper, value);
    }

    private void init(int lower, int upper, int value) {
        this.lowerBound = lower;
        this.upperBound = upper;
        this.value = value;
    }

    @Override
    public Object getValue() {
        return ((Double)(Math.pow(2, this.value))).intValue();
    }

    @Override
    public void setValue(Object value) { this.value = (Integer) value; }

    @Override
    public Object clone() throws CloneNotSupportedException {
        //return super.clone();
        return new Exp2ParameterClass(this.getName(), this.getType(), this.getDescription(), this.lowerBound, this.upperBound, this.value);
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
    public String toString() { return "" + value + ""; }

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
