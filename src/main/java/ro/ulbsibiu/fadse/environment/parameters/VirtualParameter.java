package ro.ulbsibiu.fadse.environment.parameters;

import ro.ulbsibiu.fadse.utils.MathEvaluator;

public class VirtualParameter extends SimulatorParameter<Integer> {
    protected String expression;
    protected MathEvaluator evaluator;

    public VirtualParameter(String name, String expression) {
        super(name);
        this.expression = expression;
        evaluator = new MathEvaluator(expression);
    }

    @Override
    public VirtualParameter clone() throws CloneNotSupportedException {
        VirtualParameter clone = new VirtualParameter(name, expression);

        clone.setDescription(description);

        return clone;
    }

    public void addVariable(String name, Double value) {
        evaluator.addVariable(name, value);
    }
}