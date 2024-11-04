package ro.ulbsibiu.fadse.simulationIO.parameters.simulator.impl.numeric;

import simulation.parameter.NumericParameter;

public class IntegerParameter extends NumericParameter {
    protected Integer step;
    protected Integer divideBy ;
    protected Integer initialLowerBound;
    protected Integer initialUpperBound;

    public IntegerParameter(String name, int step) {
        super(name);
        this.step = step;
        divideBy = 1;
    }

    @Override
    public void setLowerBound(Number lowerBound){
        initialLowerBound = (Integer) lowerBound;
        this.lowerBound = ((Integer) lowerBound) / step;
    }

    @Override
    public void setUpperBound(Number upperBound){
        initialUpperBound = (Integer) upperBound;
        this.upperBound = ((Integer) upperBound) / step;
    }

    @Override
    public Number getValue() {
        return (((Integer)value)* step);
    }

    @Override
    public IntegerParameter clone(){
        IntegerParameter clone = new IntegerParameter(name, step);

        clone.setValue(value);
        clone.setDescription(description);
        clone.setLowerBound(lowerBound);
        clone.setUpperBound(upperBound);
        clone.setDivideBy(divideBy);

        return clone;
    }

    public Integer getStep() {
        return step;
    }

    public void setDivideBy(Integer divideBy) {
        this.divideBy = divideBy;
    }

    public Integer getDivideBy() {
        return divideBy;
    }
}