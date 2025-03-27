package input.adapters.parameter.numerics.type;

import input.ports.parameter.problem.ProblemParameter;

public class DoubleParameter extends ProblemParameter<Double> {
    public DoubleParameter(double lowerBound, double upperBound) {
        super(lowerBound, upperBound);
    }
}