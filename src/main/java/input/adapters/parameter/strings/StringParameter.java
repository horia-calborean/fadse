package input.adapters.parameter.strings;

import input.ports.parameter.problem.ProblemParameter;

public class StringParameter extends ProblemParameter<String> {
    public StringParameter(String lowerBound, String upperBound) {
        super(lowerBound, upperBound);
    }
}