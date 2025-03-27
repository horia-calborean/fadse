package input.adapters.parameter.problem.gap;

import input.adapters.parameter.numerics.type.DoubleParameter;
import input.adapters.parameter.numerics.type.ExponentialParameter;
import input.adapters.parameter.numerics.type.IntegerParameter;
import input.adapters.parameter.strings.StringParameter;
import input.ports.parameter.problem.ProblemParameter;

public class GapParameterFactory {

    @SuppressWarnings("unchecked")
    public static <T> ProblemParameter<T> createParameter(String typeName, T lowerBound, T upperBound) {
        switch (typeName) {
            case "integer":
                return (ProblemParameter<T>) new IntegerParameter((Integer) lowerBound, (Integer) upperBound);
            case "exp2":
                return (ProblemParameter<T>) new ExponentialParameter((Integer) lowerBound, (Integer) upperBound);
            case "double":
                return (ProblemParameter<T>) new DoubleParameter((Double) lowerBound, (Double) upperBound);
            case "string":
                return (ProblemParameter<T>) new StringParameter((String) lowerBound, (String) upperBound);
            default:
                throw new IllegalArgumentException("Unknown parameter type: " + typeName);
        }
    }
}