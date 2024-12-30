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

    private String name;
    private String type;
    private String description;
    //private Real parameter;
    private double value_;
    private double lowerBound_;
    private double upperBound_;

    public DoubleParameterClass(String name, String type, String description) {
        init(name, type, description, Double.MIN_VALUE, Double.MAX_VALUE, 0);
    }

    public DoubleParameterClass(String name, String type, String description, double lower, double upper) {
       init(name, type, description, lower, upper, PseudoRandom.randDouble()*(upper-lower)+lower);
    }

    public DoubleParameterClass(String name, String type, String description, double lowerBound, double upperBound, double value) {
        init(name, type, description, lowerBound, upperBound, value);
    }
    private void init(String name, String type, String description, double lowerBound, double upperBound, double value){
        this.name = name;
        this.type = type;
        this.description = description;
        this.value_ = value;
        this.lowerBound_ = lowerBound;
        this.upperBound_ = upperBound;
        this.setName(name);
    }
    
    @Override
    public Object getValue() {return value_; }

    @Override
    public void setValue(Object value) { value_ = (Double) value; }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        //return super.clone();
        return new DoubleParameterClass(this.name, this.type, this.description, this.value_, this.lowerBound_, this.upperBound_);
    }

    @Override
    public double getLowerBound() { return lowerBound_; }

    @Override
    public void setLowerBound(double lowerBound) { lowerBound_ = lowerBound; }

    @Override
    public double getUpperBound() { return upperBound_; }

    @Override
    public void setUpperBound(double upperBound) { upperBound_ = upperBound; }

    public String toString() { return "" + value_ + ""; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

}
