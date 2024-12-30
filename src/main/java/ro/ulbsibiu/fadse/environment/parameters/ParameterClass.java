package ro.ulbsibiu.fadse.environment.parameters;


import java.io.Serializable;

import jmetal.util.Configuration;
import jmetal.util.JMException;

public abstract class ParameterClass implements Cloneable,Serializable {

//******** F R O M   P A R A M E T E R ********
    public abstract Object getValue();

    public abstract void setValue(Object value);

    public abstract Object clone() throws CloneNotSupportedException;
//******** E N D   F R O M   P A R A M E T E R ********

    //******** F R O M   V A R I A B L E ********

    /**
     * Gets the lower bound value of a variable. As not all
     * objects belonging to a subclass of <code>Variable</code> have a lower bound,
     * a call to this method is considered a fatal error by default,
     * and the program is terminated.
     * Those classes requiring this method must redefine it.
     */
    public double getLowerBound() throws JMException {
        Class cls = java.lang.String.class;
        String name = cls.getName();
        Configuration.logger_.severe("Class " + name +
                " does not implement method getLowerBound()");
        throw new JMException("Exception in " + name + ".getLowerBound()") ;
    } // getLowerBound

    /**
     * Gets the upper bound value of a variable. As not all
     * objects belonging to a subclass of <code>Variable</code> have an upper
     * bound, a call to this method is considered a fatal error by default, and the
     * program is terminated. Those classes requiring this method mustredefine it.
     */
    public double getUpperBound() throws JMException {
        Class cls = java.lang.String.class;
        String name = cls.getName();
        Configuration.logger_.severe("Class " + name +
                " does not implement method getUpperBound()");
        throw new JMException("Exception in " + name + ".getUpperBound()") ;
    } // getUpperBound

    /**
     * Sets the lower bound for a variable. As not all objects beloging to a
     * subclass of <code>Variable</code> have a lower bound, a call to this method
     * is considered a fatal error by defaultm and the program is terminated.
     * Those classes requiring this method must to redefine it.
     */
    public void setLowerBound(double lowerBound) throws JMException {
        Class cls = java.lang.String.class;
        String name = cls.getName();
        Configuration.logger_.severe("Class " + name +
                " does not implement method setLowerBound()");
        throw new JMException("Exception in " + name + ".setLowerBound()") ;
    } // setLowerBound

    /**
     * Sets the upper bound for a variable. As not all objects belongig to a
     * subclass of <code>Variable</code> have an upper bound, a call to this method
     * is considered a fatal error by default, and the program is terminated.
     * Those classes requiring this method must redefine it.
     */
    public void setUpperBound(double upperBound) throws JMException {
        Class cls = java.lang.String.class;
        String name = cls.getName();
        Configuration.logger_.severe("Class " + name +
                " does not implement method setUpperBound()");
        throw new JMException("Exception in " + name + ".setUpperBound()") ;
    } // setUpperBound

    /**
     * Gets the type of the variable. The types are defined in class Problem.
     * @return The type of the variable
     */
    public Class getVariableType() {
        return this.getClass() ;
    } // getVariableType

    // ******** E N D   F R O M   V A R I A B L E ********


}
