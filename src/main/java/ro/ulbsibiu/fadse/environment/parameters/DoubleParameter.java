package ro.ulbsibiu.fadse.environment.parameters;

import org.uma.jmetal.util.pseudorandom.JMetalRandom;

public class DoubleParameter extends Parameter {

    //private Real parameter;
    private double value;
    private double lowerBound;
    private double upperBound;

    public DoubleParameter(String name, String type, String description) {
        super(name, type, description);
        init(Double.MIN_VALUE, Double.MAX_VALUE, 0);
    }

    public DoubleParameter(String name, String type, String description, double lower, double upper) {
        super(name, type, description);
        JMetalRandom random = JMetalRandom.getInstance();
        init(lower, upper, random.nextDouble()*(upper-lower)+lower);
    }

    public DoubleParameter(String name, String type, String description, double lower, double upper, double value) {
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
    public void setValue(Object value) { this.value = (Double) value; }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        //return super.clone();
        return new DoubleParameter(this.getName(), this.getType(), this.getDescription(), this.lowerBound, this.upperBound, this.value);
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
    public String toString() { return "" + value; }

}
