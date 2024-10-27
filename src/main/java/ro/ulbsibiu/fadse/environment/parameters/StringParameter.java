package ro.ulbsibiu.fadse.environment.parameters;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class StringParameter extends SimulatorParameter<Integer> {
    protected List<String> values;

    public StringParameter(String name, List<String> values) {
        super(name);
        this.values = new LinkedList<>(values);
    }

    @Override
    public void setValue(Integer value) {
        int pos = -1;
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).equalsIgnoreCase(String.valueOf(value))) {
                pos = i;
            }
        }
        if (pos != -1) {
            this.value = pos;
        } else {
            throw new IllegalArgumentException(value + " is not in the legal values for this parameter");
        }
    }

    @Override
    public String toString() {
        return values.get(value);
    }

    @Override
    public StringParameter clone() throws CloneNotSupportedException {
        StringParameter clone = new StringParameter(name, values);

        clone.setDescription(description);

        return clone;
    }

    public List<String> getValues() {
        return new ArrayList<>(values);
    }

    public void setValues(LinkedList<String> values) {
        // TODO
        //parameter.setLowerBound(0);
        //parameter.setUpperBound(values.size()-1);
        this.values = values;
    }
}
