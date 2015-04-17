/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ulbsibiu.fadse.environment.rule;

import java.util.List;

import ro.ulbsibiu.fadse.environment.parameters.ConstantParameter;
import ro.ulbsibiu.fadse.environment.parameters.ExpresionParameter;
import ro.ulbsibiu.fadse.environment.parameters.Parameter;

/**
 *Supports only integer parameters for now
 * supports:
 * <greater>, <greater-equal>, <less>, <less-equal>, <equal>, <not-equal>,
 * do not know what to do with (or how to implement it): <expr>
 * @author Horia
 */
public class RelationRule implements Rule {

    String type;
    String description;
    String p1 = null;
    String p2 = null;
    ConstantParameter c1 = null;
    ConstantParameter c2 = null;
    ExpresionParameter e1 = null;
    ExpresionParameter e2 = null;

    public RelationRule(String type, String description, Parameter p1, Parameter p2) {
        this.type = type;
        this.description = description;
        if (p1 instanceof ConstantParameter) {//TODO think this better - the problem is that the constant parameter is likely to not have any name and also to not be included in the parameters list of the individual
            this.c1 = (ConstantParameter) p1;
        } else if (p1 instanceof ExpresionParameter) {//TODO think this better - the problem is that the constant parameter is likely to not have any name and also to not be included in the parameters list of the individual           
            this.e1 = (ExpresionParameter) p1;
        } else {
            this.p1 = p1.getName();
        }

        if (p2 instanceof ConstantParameter) {
            this.c2 = (ConstantParameter) p2;
        } else if (p2 instanceof ExpresionParameter) {
            this.e2 = (ExpresionParameter) p2;
        } else {
            this.p2 = p2.getName();
        }

    }

    public boolean validate(Parameter[] parameters) {
        Integer val1 = null;
        //TODO perform tests and throw errors if parameter not found
        if (p1 != null) {
            for (Parameter p : parameters) {
                if (p.getName().equalsIgnoreCase(p1)) {
                    val1 = (Integer) p.getValue();
                }
            }
        } else if (c1 != null) {
            val1 = (Integer) c1.getValue();
        } else if (e1 != null) {
            for (Parameter p : parameters) {
                try{
                    e1.addVariable(p.getName(), new Double((Integer) p.getValue()));
                } catch (Exception e){}
            }
            val1 = ((Integer) (e1.getValue()));
        }
        Integer val2 = null;
        if (p2 != null) {
            for (Parameter p : parameters) {
                if (p.getName().equalsIgnoreCase(p2)) {
                    val2 = (Integer) p.getValue();
                }
            }
        } else if (c1 != null) {
            val2 = (Integer) c1.getValue();
        } else if (c2 != null) {
            val2 = (Integer) c2.getValue();
        } else if (e2 != null) {
            for (Parameter p : parameters) {
                try{
                    e2.addVariable(p.getName(), new Double((Integer) p.getValue()));
                } catch (Exception e){}
            }
            val2 = ((Integer) (e2.getValue()));
        }
        boolean result = false;
        if (type.equalsIgnoreCase("greater")) {
            result = val1 > val2;
        } else if (type.equalsIgnoreCase("greater-equal")) {
            result = val1 >= val2;
        } else if (type.equalsIgnoreCase("less")) {
            result = val1 < val2;
        } else if (type.equalsIgnoreCase("less-equal")) {
            result = val1 <= val2;
        } else if (type.equalsIgnoreCase("equal")) {
            result = val1 == val2;
        } else if (type.equalsIgnoreCase("not-equal")) {
            result = val1 != val2;
        } else {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        return result;
    }

    @Override
    public String toString() {
        String result = "\nRelationRule: " + type + " " + description + " [";
        if (p1 != null) {
            result += p1;
        } else if (c1 != null) {
            result += c1.getValue();
        } else {
            result += "expr(" + e1.getExpression() + ")";
        }
        result += " <" + type + "> ";
        if (p2 != null) {
            result += p2;
        } else if (c2 != null) {
            result += "const[" + c2.getValue() + "]";
        } else {
            result += "expr(" + e2.getExpression() + ")";
        }
        result += "]";
        return result;

    }

    public ConstantParameter getC1() {
        return c1;
    }

    public void setC1(ConstantParameter c1) {
        this.c1 = c1;
    }

    public ConstantParameter getC2() {
        return c2;
    }

    public void setC2(ConstantParameter c2) {
        this.c2 = c2;
    }

    public ExpresionParameter getE1() {
        return e1;
    }

    public void setE1(ExpresionParameter e1) {
        this.e1 = e1;
    }

    public ExpresionParameter getE2() {
        return e2;
    }

    public void setE2(ExpresionParameter e2) {
        this.e2 = e2;
    }

    public String getP1() {
        return p1;
    }

    public void setP1(String p1) {
        this.p1 = p1;
    }

    public String getP2() {
        return p2;
    }

    public void setP2(String p2) {
        this.p2 = p2;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
}
