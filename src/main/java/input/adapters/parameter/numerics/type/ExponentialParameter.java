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
        int closest = -1;
        double minDiff = Double.MAX_VALUE;
        int power = 0;

        while (true) {
            double candidateDouble = Math.pow(exp, power);
            int candidate = (int) Math.round(candidateDouble);

            if (candidate > upperBound) {
                break;
            }

            if (candidate >= lowerBound) {
                double diff = Math.abs(candidate - value);
                if (diff < minDiff) {
                    minDiff = diff;
                    closest = candidate;
                }
            }

            power++;
        }

        this.value = closest;
    }
}