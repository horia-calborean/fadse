package ro.ulbsibiu.fadse.environment.parameters;

public class DoubleParameter extends SimulatorParameter<Double> {
    protected Double lowerBound;
    protected Double upperBound;

    public DoubleParameter(String name, Double lowerBound, Double upperBound) {
        super(name);
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    @Override
    public DoubleParameter clone() throws CloneNotSupportedException {
        DoubleParameter clone = new DoubleParameter(name, lowerBound, upperBound);

        clone.setValue(value);
        clone.setDescription(description);

        return clone;
    }
}