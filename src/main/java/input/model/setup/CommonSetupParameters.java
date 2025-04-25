package input.model.setup;

public enum CommonSetupParameters implements SetupParameter {
    BENCHMARKS("benchmarks"),
    PARAMETERS("parameters");

    private final String name;

    CommonSetupParameters(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}