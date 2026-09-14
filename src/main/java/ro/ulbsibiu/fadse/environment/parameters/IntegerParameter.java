package ro.ulbsibiu.fadse.environment.parameters;

import org.uma.jmetal.util.pseudorandom.JMetalRandom;

public class IntegerParameter extends Parameter
{
    private int step = 1;
    private int divideBy = 1;
    //private Int variable;
    private int value;       //Stores the value of the variable
    private int lowerBound;  //Stores the lower limit of the variable
    private int upperBound;  //Stores the upper limit of the variable

    public IntegerParameter(String name, String type, String description) {
        super(name, type, description);
        init(Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
    }

    public IntegerParameter(String name, String type, String description, int lower, int upper) {
        super(name, type, description);
        JMetalRandom random = JMetalRandom.getInstance();
        init(lower, upper, random.nextInt(lower, upper));
    }

    public IntegerParameter(String name, String type, String description, int lower, int upper, int value) {
        super(name, type, description);
        init(lower, upper, value);
    }
    private void init(int lowerBound, int upperBound, int value) {
        this.value = value;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    @Override
    public Object getValue() {
        return ((Double)((double)this.value*step)).intValue();
    }

    @Override
    public void setValue(Object value) {
        this.value = (int)value;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        //return super.clone();
        return new IntegerParameter(this.getName(), this.getType(), this.getDescription(), this.lowerBound, this.upperBound, this.value);
    }

    @Override
    public double getLowerBound() { return this.lowerBound; }

    @Override
    public double getUpperBound() { return this.upperBound; }

    @Override
    public void setLowerBound(double lowerBound) {  this.lowerBound = ((int)lowerBound)/step; }

    @Override
    public void setUpperBound(double upperBound) { this.upperBound = ((int)upperBound)/step; }

    @Override
    public String toString() {
        return "" + this.value;
    }

    @Override
    public int getStep() {
        return step;
    }

    @Override
    public void setStep(int step) {
        this.step = step;
    }

    @Override
    public void setDivideBy(int divideBy) { this.divideBy = divideBy; }

    @Override
    public int getDivideBy() {
        return divideBy;
    }
}
