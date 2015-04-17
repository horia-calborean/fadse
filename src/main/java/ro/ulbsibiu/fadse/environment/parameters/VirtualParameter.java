package ro.ulbsibiu.fadse.environment.parameters;

import ro.ulbsibiu.fadse.utils.MathEvaluator;
import jmetal.base.Variable;

/*
 * Supports only Integer parameters
 */
public class VirtualParameter implements Parameter {

    private Object value;
    private String expression;
    private String name;
    private String description;
    private MathEvaluator evaluator;

    /**
     * p1 and p2 have to be convertible to integer
     *
     */
    public VirtualParameter(String name, String expression, String description) {
        this.description = description;
        this.expression = expression;
        this.name = name;
        evaluator = new MathEvaluator(expression);
    }

    public Object getValue() {
        return evaluator.getValue().intValue();
    }

    public void setValue(Object value) {
        this.value = (Integer) value;
    }

    @Override
    public String toString() {
        return "" + expression + "";
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public String getDescription() {
        return description;
    }

    public void addVariable(String name, Double value) {
        evaluator.addVariable(name, value);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setVariable(Variable v) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Variable getVariable() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }
}
