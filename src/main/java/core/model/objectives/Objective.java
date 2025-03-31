package core.model.objectives;

public class Objective {
    protected String name;
    protected String type;
    protected boolean isMinimized;
    protected String unit;
    protected String description;
    protected double value;

    public Objective(String name, String type, boolean isMinimized) {
        this.name = name;
        this.type = type;
        this.isMinimized = isMinimized;
        unit = "";
        description = "";
        value = 0;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}