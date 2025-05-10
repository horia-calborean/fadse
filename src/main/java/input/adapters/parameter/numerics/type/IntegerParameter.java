package input.adapters.parameter.numerics.type;

import input.ports.parameter.problem.ProblemParameter;

import java.io.Serializable;

public class IntegerParameter extends ProblemParameter<Integer> implements Serializable {

    protected int step;

    public IntegerParameter(int lowerBound, int upperBound) {
        super(lowerBound, upperBound);
        value = 0;
        step = 1;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getStep() {
        return step;
    }

    @Override
    public ProblemParameter<Integer> clone() {
        IntegerParameter copy = new IntegerParameter(this.lowerBound, this.upperBound);
        copy.setName(this.name);
        copy.setDescription(this.description);
        copy.setStep(step);
        copy.setValue(value);
        return copy;
    }

    @Override
    public void setValueFromDouble(double value) {
        int clampedValue = (int) Math.max(lowerBound, Math.min(upperBound, value));
        int stepsFromLower = Math.round((clampedValue - lowerBound) / (float) step);
        this.value = lowerBound + stepsFromLower * step;
    }
}