package input.model.setup;

public enum CommonSetupParameters implements SetupParameter {
    BENCHMARKS("benchmarks"),
    OBJECTIVES("objectives"),
    TYPE("type"),
    FADSE_CLIENTS("fadse_clients"),
    PROBLEM_CONFIG("problem_config"),
    DATABASE("database"),
    DESIGN_VARIABLES("parameters");

    private final String name;

    CommonSetupParameters(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}