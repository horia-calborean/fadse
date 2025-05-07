package core.model.objectives;

import java.io.Serializable;

public class Objective implements Cloneable, Serializable {
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

    public double getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isBetter(Objective objective) {
        if (!isMinimized) {
            return this.value > objective.getValue();
        } else {
            return this.value < objective.getValue();
        }
    }

    public boolean isMinimized(){
        return isMinimized;
    }

    @Override
    public Objective clone() {
        Objective cloned = new Objective(this.name, this.type, this.isMinimized);
        cloned.unit = this.unit;
        cloned.description = this.description;
        cloned.value = this.value;
        return cloned;
    }
}