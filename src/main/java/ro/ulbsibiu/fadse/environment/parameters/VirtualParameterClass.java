package ro.ulbsibiu.fadse.environment.parameters;

import ro.ulbsibiu.fadse.utils.MathEvaluator;

/*
 * Supports only Integer parameters
 */
public class VirtualParameterClass extends ParameterClass {

    private Object value;
    private String expression;
    private MathEvaluator evaluator;

    /**
     * p1 and p2 have to be convertible to integer
     *
     */
    public VirtualParameterClass(String name, String description, String expression) {
        super(name, description);
        this.expression = expression;
        evaluator = new MathEvaluator(expression);
    }

    @Override
    public Object getValue() {
        return evaluator.getValue().intValue();
    }

    @Override
    public void setValue(Object value) {
        this.value = (Integer) value;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        //return super.clone();
        return new VirtualParameterClass(this.getName(), this.getDescription(), this.expression);
    }

    @Override
    public String toString() {
        return "" + expression + "";
    }

    public void addVariable(String name, Double value) {
        evaluator.addVariable(name, value);
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }
}
