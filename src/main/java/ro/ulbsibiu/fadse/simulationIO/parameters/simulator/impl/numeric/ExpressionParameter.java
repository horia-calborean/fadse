package ro.ulbsibiu.fadse.simulationIO.parameters.simulator.impl.numeric;

import ro.ulbsibiu.fadse.utils.MathEvaluator;
import simulation.parameter.NumericParameter;

public class ExpressionParameter extends NumericParameter {
    protected String expression;
    protected MathEvaluator evaluator;

    public ExpressionParameter(String name, String expression) {
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
    public ExpressionParameter clone() {
        ExpressionParameter clone = new ExpressionParameter(name, expression);

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