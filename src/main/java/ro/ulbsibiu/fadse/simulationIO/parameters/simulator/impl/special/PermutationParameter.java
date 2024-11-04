package ro.ulbsibiu.fadse.simulationIO.parameters.simulator.impl.special;

import ro.ulbsibiu.fadse.simulationIO.parameters.simulator.SimulatorParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PermutationParameter extends SimulatorParameter {
    protected List<Integer> values;
    protected int size = 2;

    public PermutationParameter(String name) {
        super(name);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setValue(List<Integer> values){
        this.values = new ArrayList<>(values);
    }

    @Override
    public String toString() {
        return values.stream().map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    @Override
    public PermutationParameter clone() {
        PermutationParameter clone = new PermutationParameter(name);

        clone.setSize(size);
        clone.setValue(values);
        clone.setDescription(description);

        return clone;
    }
}