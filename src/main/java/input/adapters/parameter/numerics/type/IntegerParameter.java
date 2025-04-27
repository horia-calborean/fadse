package input.adapters.parameter.numerics.type;

import input.ports.parameter.problem.ProblemParameter;

public class IntegerParameter extends ProblemParameter<Integer> {

    public IntegerParameter(int lowerBound, int upperBound) {
        super(lowerBound, upperBound);
        value = 0;
    }

    @Override
    public ProblemParameter<Integer> clone() {
        IntegerParameter copy = new IntegerParameter(this.lowerBound, this.upperBound);
        copy.setName(this.name);
        copy.setDescription(this.description);
        copy.setValue(value);
        return copy;
    }

    @Override
    public void setValueFromDouble(double value) {
        this.value = (int) Math.round(value);
    }
}