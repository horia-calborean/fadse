package ro.ulbsibiu.fadse.environment.parameters;

public class Exp2Parameter extends SimulatorParameter<Integer> {
    protected Integer initialLowerBound;
    protected Integer initialUpperBound;
    protected Integer lowerBound;
    protected Integer upperBound;

    public Exp2Parameter(String name, Integer lowerBound, Integer upperBound) {
        super(name);
        initialLowerBound = lowerBound;
        initialUpperBound = upperBound;
        this.lowerBound = (int) getLog2(lowerBound);
        this.upperBound = (int) getLog2(upperBound);
    }

    @Override
    public Exp2Parameter clone() throws CloneNotSupportedException {
        Exp2Parameter clone = new Exp2Parameter(name, initialLowerBound, initialUpperBound);

        clone.setValue(value);
        clone.setDescription(description);

        return clone;
    }

    protected double getLog2(double x) {
        return Math.log(x) / Math.log(2);
    }
}