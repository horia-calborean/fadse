package ro.ulbsibiu.fadse.simulationIO.parameters.simulator.impl.special;

import ro.ulbsibiu.fadse.simulationIO.parameters.simulator.SimulatorParameter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class StringParameter extends SimulatorParameter {
    protected List<String> values;
    protected Integer index;
    protected Integer lowerBound;
    protected Integer upperBound;

    public StringParameter(String name, List<String> values) {
        super(name);
        this.values = new ArrayList<>(values);
        lowerBound = 0;
        upperBound = values.size() - 1;
    }

    public void setValue(Integer value) {
        int pos = -1;
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).equalsIgnoreCase(String.valueOf(value))) {
                pos = i;
            }
        }
        if (pos != -1) {
            index = pos;
        } else {
            throw new IllegalArgumentException(value + " is not in the legal values for this parameter");
        }
    }

    public String getValue(){
        return values.get(index);
    }

    @Override
    public String toString() {
        return values.get(index);
    }

    @Override
    public StringParameter clone() {
        StringParameter clone = new StringParameter(name, values);

        clone.setDescription(description);

        return clone;
    }

    public List<String> getValues() {
        return new ArrayList<>(values);
    }

    public void setValues(LinkedList<String> values) {
        this.values = values;
        lowerBound = 0;
        upperBound = values.size() - 1;
    }
}