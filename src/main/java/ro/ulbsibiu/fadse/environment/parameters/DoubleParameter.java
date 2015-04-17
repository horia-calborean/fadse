/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ulbsibiu.fadse.environment.parameters;

import java.util.Random;

import ro.ulbsibiu.fadse.utils.Utils;
import jmetal.base.Variable;
import jmetal.base.variable.Real;

/**
 *
 * @author Horia Andrei Calborean <horia.calborean@ulbsibiu.ro>
 */
public class DoubleParameter implements Parameter {

    private String name;
    private String type;
    private String description;
    private Real parameter;

    public DoubleParameter(String name, String type, String description) {
       init(name, type, description, new Real(0, 1));
    }

    public DoubleParameter(String name, String type, String description, Real parameter) {
        init(name, type, description, parameter);
    }
    private void init(String name, String type, String description, Real parameter){
        this.name = name;
        this.type = type;
        this.description = description;
        this.parameter = parameter;
        parameter.setName(name);
    }
    

    public Object getValue() {
        return parameter.getValue();
    }

    public void setValue(Object value) {
        this.parameter.setValue((Double) value);
    }

    @Override
    public String toString() {
        return "" + parameter.getValue() + "";
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public String getName() {
        return name;
    }

    public double getMaxValue() {
        return parameter.getUpperBound();
    }

    public void setMaxValue(double maxValue) {
        parameter.setUpperBound(maxValue);
    }

    public double getMinValue() {
        return parameter.getLowerBound();
    }

    public void setMinValue(double minValue) {
        this.parameter.setLowerBound(minValue) ;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }
    

    public void setVariable(Variable v) {
        parameter = (Real) v;
    }

    public Variable getVariable() {
         return parameter;
    }
}
