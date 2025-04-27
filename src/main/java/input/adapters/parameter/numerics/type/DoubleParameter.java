package input.adapters.parameter.numerics.type;

import input.ports.parameter.problem.ProblemParameter;

public class DoubleParameter extends ProblemParameter<Double> {
    public DoubleParameter(double lowerBound, double upperBound) {
        super(lowerBound, upperBound);
        value = 0.0;
    }

    @Override
    public ProblemParameter<Double> clone() {
        DoubleParameter copy = new DoubleParameter(this.lowerBound, this.upperBound);
        copy.setName(this.name);
        copy.setDescription(this.description);
        copy.setValue(value);
        return copy;
    }

    @Override
    public void setValueFromDouble(double value) {
        this.value = value;
    }
}