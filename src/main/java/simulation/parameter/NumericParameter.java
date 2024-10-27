package simulation.parameter;

import ro.ulbsibiu.fadse.environment.parameters.SimulatorParameter;

public abstract class NumericParameter extends SimulatorParameter {
    protected T value;
    protected T lowerBound;
    protected T upperBound;

    public NumericParameter(String name) {
        super(name);
    }

    public T getValue(){
        return value;
    }

    public void setValue(T value){
        this.value = value;
    }

    public T getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(T lowerBound) {
        this.lowerBound = lowerBound;
    }

    public T getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(T upperBound) {
        this.upperBound = upperBound;
    }
}