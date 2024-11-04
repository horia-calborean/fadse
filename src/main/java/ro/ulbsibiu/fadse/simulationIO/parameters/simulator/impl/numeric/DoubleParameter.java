package ro.ulbsibiu.fadse.simulationIO.parameters.simulator.impl.numeric;

import simulation.parameter.NumericParameter;

public class DoubleParameter extends NumericParameter {
    protected Double lowerBound;
    protected Double upperBound;

    public DoubleParameter(String name, Double lowerBound, Double upperBound) {
        super(name);
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    @Override
    public DoubleParameter clone() {
        DoubleParameter clone = new DoubleParameter(name, lowerBound, upperBound);

        clone.setValue(value);
        clone.setDescription(description);

        return clone;
    }
}