package input.model.setup;

//TODO: move them to CommonSetupParameters if possible
public enum GapSetupParameters implements SetupParameter {
    METAHEURISTIC("metaheuristic"),
    METAHEURISTIC_DATA(""),
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