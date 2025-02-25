package input.adapters.parameter;

import input.ports.parameter.InputParameter;

public enum GAPInputParameter implements InputParameter {
    METAHEURISTIC("metaheuristic"),
    BENCHMARKS("benchmarks");

    private final String name;

    GAPInputParameter(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
