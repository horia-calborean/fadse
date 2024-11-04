package ro.ulbsibiu.fadse.simulationIO.parameters.simulator;

import java.io.Serializable;

public abstract class SimulatorParameter implements Cloneable, Serializable {
    protected String name;
    protected String description;

    public SimulatorParameter(String name) {
        this.name = name;
        description = "";
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public abstract Object clone();
}