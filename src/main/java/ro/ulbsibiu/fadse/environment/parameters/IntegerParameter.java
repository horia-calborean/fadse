package ro.ulbsibiu.fadse.environment.parameters;

import simulation.parameter.NumericParameter;

public class IntegerParameter extends NumericParameter<Integer> {
    protected int step;
    protected int divideBy ;
    protected Integer initialLowerBound;
    protected Integer initialUpperBound;

    public IntegerParameter(String name, int step) {
        super(name);
        this.step = step;
        divideBy = 1;
    }

    @Override
    public void setLowerBound(Integer lowerBound){
        initialLowerBound = lowerBound;
        this.lowerBound = lowerBound / step;
    }

    @Override
    public void setUpperBound(Integer upperBound){
        initialUpperBound = upperBound;
        this.upperBound = upperBound / step;
    }

    @Override
    public Integer getValue() {
        return (value * step);
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