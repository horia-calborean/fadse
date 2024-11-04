package ro.ulbsibiu.fadse.simulationIO.parameters.simulator.impl.numeric;

import simulation.parameter.NumericParameter;

public class ConstantParameter extends NumericParameter {
    public ConstantParameter(String name) {
        super(name);
    }

    @Override
    public ConstantParameter clone() {
        ConstantParameter clone = new ConstantParameter(name);

        clone.setValue(value);
        clone.setDescription(description);

        return clone;
    }
}