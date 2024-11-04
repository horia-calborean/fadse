package simulation.parameter;

import ro.ulbsibiu.fadse.simulationIO.parameters.simulator.SimulatorParameter;

public abstract class NumericParameter extends SimulatorParameter {
    protected Number value;
    protected Number lowerBound;
    protected Number upperBound;

    public NumericParameter(String name) {
        super(name);
    }

    public Number getValue(){
        return value;
    }

    public void setValue(Number value){
        this.value = value;
    }

    public Number getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(Number lowerBound) {
        this.lowerBound = lowerBound;
    }

    public Number getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(Number upperBound) {
        this.upperBound = upperBound;
    }
}