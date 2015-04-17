package ro.ulbsibiu.fadse.environment;

import java.io.Serializable;

public class Objective implements Serializable {

    private double value;
    private boolean maximize = true;
    private String name;
    private String type;
    private String unit;
    private String description;

    public Objective(String name, String type, String unit, String description, boolean maximize) {
        this.name = name;
        this.type = type;
        this.unit = unit;
        this.description = description;
        value = 0;
        this.maximize = maximize;
    }

    public Objective(String name, String type, String unit, String description, double value, boolean maximize) {
        this.name = name;
        this.type = type;
        this.unit = unit;
        this.description = description;
        if (maximize) {
            this.value = 1 / value;//TODO test this
        } else {
            this.value = value;
        }
        this.maximize = maximize;
    }

    public double getValue() {
        return value;
    }
    public double getRealValue() {
        if (maximize) {
            return  1 / value;//TODO test this
        } else {
            return  value;
        }
    }

    public void setValue(double value) {
       if (maximize) {
            this.value = 1 / value;//TODO test this
        } else {
            this.value = value;
        }
    }

    public boolean isMaximize() {
        return maximize;
    }

    @Override
    public String toString() {
        return "" + value + "";
    }

    public boolean isBetter(Objective objective) {
        if (maximize) {
            return this.value > objective.getValue();
        } else {
            return this.value < objective.getValue();
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
