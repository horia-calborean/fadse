package input.model.setup;

public enum GapSetupParameters implements SetupParameter {
    GAP_CONFIG("gap_config"),
    GAP_PARAMETERS("gap_parameters"),
    BENCHMARKS("benchmarks"),
    METAHEURISTIC("metaheuristic"),
    DATABASE("database"),
    TYPE("type"),
    NAME("name"),
    OUTPUT_PATH("output"),
    FADSE_CLIENTS_FILE_PATH("clients");

    private final String name;

    GapSetupParameters(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}