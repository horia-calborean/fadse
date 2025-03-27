package input.adapters.parameter.numerics.collection;

import input.adapters.parameter.numerics.NumericParameters;
import input.adapters.parameter.numerics.type.DoubleParameter;
import input.ports.parameter.problem.ProblemParameter;

import java.util.Map;

public class DoubleParameters extends NumericParameters<Double> {

    @Override
    protected Map<String, Class<? extends ProblemParameter<Double>>> buildMap() {
        return Map.of(
                "double", DoubleParameter.class
        );
    }
}