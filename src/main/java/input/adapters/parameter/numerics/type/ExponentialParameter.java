package input.adapters.parameter.numerics.type;

import input.ports.parameter.problem.ProblemParameter;

public class ExponentialParameter extends ProblemParameter<Integer> {
    protected int exp;

    public ExponentialParameter(int lowerBound, int upperBound) {
        super(lowerBound, upperBound);
        exp = 2;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getExp() {
        return exp;
    }
}