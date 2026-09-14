package input.adapters.parameter.numerics.collection;

import input.adapters.parameter.numerics.NumericParameters;
import input.adapters.parameter.numerics.type.ExponentialParameter;
import input.adapters.parameter.numerics.type.IntegerParameter;
import input.ports.parameter.problem.ProblemParameter;

import java.util.Map;

public class IntegerParameters extends NumericParameters<Integer> {

    @Override
    protected Map<String, Class<? extends ProblemParameter<Integer>>> buildMap() {
        return Map.of(
                "integer", IntegerParameter.class,
                "exp2", ExponentialParameter.class
        );
    }
}