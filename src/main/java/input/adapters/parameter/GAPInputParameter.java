package input.adapters.parameter;

import input.ports.parameter.InputParameter;

public enum GAPInputParameter implements InputParameter {
    METAHEURISTIC("metaheuristic"),
    BENCHMARKS("benchmarks"),
    DATABASE("database"),
    TYPE("type"),
    NAME("name"),
    SIMULATION_PARAMETERS("simulation_parameters"),
    OUTPUT_PATH("output_path");

    private final String name;

    GAPInputParameter(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
