package input.adapters.parameter.numerics.type;

import input.ports.parameter.problem.ProblemParameter;

public class IntegerParameter extends ProblemParameter<Integer> {

    public IntegerParameter(int lowerBound, int upperBound) {
        super(lowerBound, upperBound);
    }
}