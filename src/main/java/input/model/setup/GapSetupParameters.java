package input.model.setup;

public enum GapSetupParameters implements SetupParameter {
    METAHEURISTIC("metaheuristic"),
    METAHEURISTIC_DATA(""),
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