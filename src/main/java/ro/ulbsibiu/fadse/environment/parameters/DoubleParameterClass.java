/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ulbsibiu.fadse.environment.parameters;

import jmetal.util.PseudoRandom;

/**
 *
 * @author Horia Andrei Calborean <horia.calborean@ulbsibiu.ro>
 */
public class DoubleParameterClass extends ParameterClass {

    //private Real parameter;
    private double value;
    private double lowerBound;
    private double upperBound;

    public DoubleParameterClass(String name, String type, String description) {
        super(name, type, description);
        init(Double.MIN_VALUE, Double.MAX_VALUE, 0);
    }

    public DoubleParameterClass(String name, String type, String description, double lower, double upper) {
        super(name, type, description);
        init(lower, upper, PseudoRandom.randDouble()*(upper-lower)+lower);
    }

    public DoubleParameterClass(String name, String type, String description, double lower, double upper, double value) {
        super(name, type, description);
        init(lower, upper, value);
    }
    private void init(double lower, double upper, double value){
        this.value = value;
        this.lowerBound = lower;
        this.upperBound = upper;
    }
    
    @Override
    public Object getValue() {return value; }

    @Override
    public void setValue(Object value) { value = (Double) value; }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        //return super.clone();
        return new DoubleParameterClass(this.getName(), this.getType(), this.getDescription(), this.lowerBound, this.upperBound, this.value);
    }

    @Override
    public double getLowerBound() { return this.lowerBound; }

    @Override
    public void setLowerBound(double lowerBound) { this.lowerBound = lowerBound; }

    @Override
    public double getUpperBound() { return this.upperBound; }

    @Override
    public void setUpperBound(double upperBound) { this.upperBound = upperBound; }

    @Override
    public String toString() { return "" + value + ""; }

}
