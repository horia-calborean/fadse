package ro.ulbsibiu.fadse.environment.parameters;

import jmetal.base.Variable;
import jmetal.base.variable.Int;

public class Exp2Parameter implements Parameter {

    private String name;
    private String type;
    private String description;
    private Int variable;

    public Exp2Parameter(String name, String type, String description) {
        init(name, type, description, new Int(1, 6));
    }

    public Exp2Parameter(String name, String type, String description, Int variable) {
        init(name, type, description, variable);
    }

    private void init(String name, String type, String description, Int variable) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.variable = variable;
        variable.setName(name);
    }

    public Object getValue() {
        return ((Double)(Math.pow(2,variable.getValue()))).intValue();
    }

    public void setValue(Object value) {
        this.variable.setValue((Integer) value);
    }

    @Override
    public String toString() {
        return "" + variable.getValue() + "";
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMaxValue() {
        return (int) Math.pow(2,variable.getUpperBound());
    }

    public void setMaxValue(int maxValue) {
        this.variable.setUpperBound(log2(maxValue));
    }

    public int getMinValue() {
        return (int) Math.pow(2,variable.getLowerBound());
    }

    public void setMinValue(int minValue) {
        this.variable.setLowerBound(log2(minValue));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setVariable(Variable v) {
        variable = (Int) v;
    }

    public Variable getVariable() {
        return variable;
    }
    /**
     * Calculate base 2 logarithm
     *
     * @param x value to take log of
     *
     * @return base 2 logarithm.
     */
    private double log2( double x )
        {
        // Math.log is base e, natural log, ln
        return Math.log( x ) / Math.log( 2 );
        }


}
