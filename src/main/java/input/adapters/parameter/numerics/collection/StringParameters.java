package input.adapters.parameter.numerics.collection;

import input.adapters.parameter.problem.ParametersList;
import input.adapters.parameter.strings.StringParameter;
import input.ports.parameter.problem.ProblemParameter;

import java.util.Map;

public class StringParameters extends ParametersList<String> {

    @Override
    protected Map<String, Class<? extends ProblemParameter<String>>> buildMap() {
        return Map.of(
                "text", StringParameter.class
        );
    }
}