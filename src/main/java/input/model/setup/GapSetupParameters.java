package input.model.setup;

public enum GapSetupParameters implements SetupParameter {
    GAP_CONFIG("gap_config"),
    METAHEURISTIC("metaheuristic"),
    METAHEURISTIC_DATA(""),
    DATABASE("database"),
    TYPE("type"),
    NAME("name"),
    OUTPUT_PATH("output"),
    FADSE_CLIENTS_FILE_PATH("clients"),
    FADSE_CLIENTS("fadse_clients"),
    OBJECTIVES("gap_objectives");

    private final String name;

    GapSetupParameters(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}