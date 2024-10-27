package ro.ulbsibiu.fadse.environment.parameters;

import ro.ulbsibiu.fadse.utils.MathEvaluator;

public class ExpresionParameter extends SimulatorParameter<Integer> {
    protected String expression;
    protected MathEvaluator evaluator;

    public ExpresionParameter(String name, String expression) {
        super(name);
        this.expression = expression;
        evaluator = new MathEvaluator(expression);
    }

    @Override
    public Integer getValue() {
        return evaluator.getValue().intValue();
    }

    @Override
    public String toString() {
        return expression;
    }

    @Override
    public ExpresionParameter clone() throws CloneNotSupportedException {
        ExpresionParameter clone = new ExpresionParameter(name, expression);

        clone.setDescription(description);

        return clone;
    }

    public void addVariable(String name, Double value) {
        evaluator.addVariable(name, value);
    }

    public String getExpression() {
        return expression;
    }
}