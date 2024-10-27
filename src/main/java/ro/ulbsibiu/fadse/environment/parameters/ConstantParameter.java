package ro.ulbsibiu.fadse.environment.parameters;

public class ConstantParameter extends SimulatorParameter<Double> {
    public ConstantParameter(String name) {
        super(name);
    }

    @Override
    public ConstantParameter clone() throws CloneNotSupportedException {
        ConstantParameter clone = new ConstantParameter(name);

        clone.setValue(value);
        clone.setDescription(description);

        return clone;
    }
}