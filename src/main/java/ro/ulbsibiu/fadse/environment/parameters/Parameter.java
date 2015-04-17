package ro.ulbsibiu.fadse.environment.parameters;

import java.io.Serializable;
import jmetal.base.Variable;

public interface Parameter extends Cloneable,Serializable {

    public Object getValue();

    public void setValue(Object value);

    public Object clone() throws CloneNotSupportedException;

    public String getName();

    public void setVariable(Variable v);
    public Variable getVariable();
}
