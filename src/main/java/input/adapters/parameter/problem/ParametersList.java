package input.adapters.parameter.problem;

import input.ports.parameter.problem.ProblemParameter;

import java.util.Map;

public abstract class ParametersList<T> {
    protected Map<String, Class<? extends ProblemParameter<T>>> parametersMap = null;

    public ParametersList() {
        parametersMap = buildMap();
    }

    protected abstract Map<String, Class<? extends ProblemParameter<T>>> buildMap();

    public Class<? extends ProblemParameter<T>> getTypeOf(String typeName){
        return parametersMap.get(typeName);
    }
}