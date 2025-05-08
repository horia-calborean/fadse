package input.adapters.parameter.numerics.type;

import input.ports.parameter.problem.ProblemParameter;

import java.io.Serializable;

public class ExponentialParameter extends ProblemParameter<Integer> implements Serializable {
    protected int exp;

    public ExponentialParameter(int lowerBound, int upperBound) {
        super(lowerBound, upperBound);
        exp = 2;
        value = 0;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getExp() {
        return exp;
    }

    @Override
    public ProblemParameter<Integer> clone() {
        ExponentialParameter copy = new ExponentialParameter(this.lowerBound, this.upperBound);
        copy.setExp(this.exp);
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