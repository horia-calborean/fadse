package ro.ulbsibiu.fadse.environment.parameters;

import java.util.List;
import java.util.stream.Collectors;

public class PermutationParameter extends SimulatorParameter<List<Integer>> {
    protected int size = 2;

    public PermutationParameter(String name) {
        super(name);
    }

    @Override
    public String toString() {
        return value.stream().map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    @Override
    public PermutationParameter clone() throws CloneNotSupportedException {
        PermutationParameter clone = new PermutationParameter(name);

        clone.setSize(size);
        clone.setValue(value);
        clone.setDescription(description);

        return clone;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}