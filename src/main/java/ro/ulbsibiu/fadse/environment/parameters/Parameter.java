package ro.ulbsibiu.fadse.environment.parameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uma.jmetal.util.errorchecking.JMetalException;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public abstract class Parameter implements Cloneable,Serializable {
    private String name;
    private String type;
    private String description;
    private static final Logger logger = LoggerFactory.getLogger(Parameter.class);

    public Parameter(String name, String type, String description){
        this.name = name;
        this.type = type;
        this.description = description;
    }

    public Parameter(String name, String description){
        this.name = name;
        this.description = description;
    }

    public Parameter(String description){
        this.description = description;
    }

    public String getName(){
        return this.name;
    }

    public String getType(){
        return this.type;
    }

    public String getDescription(){
        return this.description;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setDescription(String description){
        this.description = description;
    }

//******** F R O M   P A R A M E T E R   I N T E R F A C E ********
    public abstract Object getValue();

    public abstract void setValue(Object value);

    public abstract Object clone() throws CloneNotSupportedException;

    public abstract String toString();
//******** E N D   F R O M   P A R A M E T E R   I N T E R F A C E ********

    //******** F R O M   V A R I A B L E ********

    /**
     * Gets the lower bound value of a variable. As not all
     * objects belonging to a subclass of <code>Variable</code> have a lower bound,
     * a call to this method is considered a fatal error by default,
     * and the program is terminated.
     * Those classes requiring this method must redefine it.
     */
    public double getLowerBound() throws JMetalException {
        Class<String> cls = java.lang.String.class;
        String name = cls.getName();
        logger.info("Class " + name +
                " does not implement method getLowerBound()");
        throw new JMetalException("Exception in " + name + ".getLowerBound()") ;
    } // getLowerBound

    /**
     * Gets the upper bound value of a variable. As not all
     * objects belonging to a subclass of <code>Variable</code> have an upper
     * bound, a call to this method is considered a fatal error by default, and the
     * program is terminated. Those classes requiring this method must redefine it.
     */
    public double getUpperBound() throws JMetalException {
        Class<String> cls = java.lang.String.class;
        String name = cls.getName();
        logger.info("Class " + name +
                " does not implement method getUpperBound()");
        throw new JMetalException("Exception in " + name + ".getUpperBound()") ;
    } // getUpperBound

    /**
     * Sets the lower bound for a variable. As not all objects belonging to a
     * subclass of <code>Variable</code> have a lower bound, a call to this method
     * is considered a fatal error by default and the program is terminated.
     * Those classes requiring this method must redefine it.
     */
    public void setLowerBound(double lowerBound) throws JMetalException {
        Class<String> cls = java.lang.String.class;
        String name = cls.getName();
        logger.info("Class " + name +
                " does not implement method setLowerBound()");
        throw new JMetalException("Exception in " + name + ".setLowerBound()") ;
    } // setLowerBound

    /**
     * Sets the upper bound for a variable. As not all objects belonging to a
     * subclass of <code>Variable</code> have an upper bound, a call to this method
     * is considered a fatal error by default, and the program is terminated.
     * Those classes requiring this method must redefine it.
     */
    public void setUpperBound(double upperBound) throws JMetalException {
        Class<String> cls = java.lang.String.class;
        String name = cls.getName();
        logger.info("Class " + name +
                " does not implement method setUpperBound()");
        throw new JMetalException("Exception in " + name + ".setUpperBound()") ;
    } // setUpperBound

    /**
     * Gets the type of the variable. The types are defined in class Problem.
     * @return The type of the variable
     */
    public Class<?> getVariableType() {
        return this.getClass() ;
    } // getVariableType

    // ******** E N D   F R O M   V A R I A B L E ********

    public void setStep(int step) throws JMetalException {
        Class<String> cls = java.lang.String.class;
        String name = cls.getName();
        logger.info("Class " + name +
                " does not implement method setStep()");
        throw new JMetalException("Exception in " + name + ".setStep()") ;
    }

    public int getStep() throws JMetalException {
        Class<String> cls = java.lang.String.class;
        String name = cls.getName();
        logger.info("Class " + name +
                " does not implement method getStep()");
        throw new JMetalException("Exception in " + name + ".getStep()") ;
    }

    public void setDivideBy(int divideBy) throws JMetalException {
        Class<String> cls = java.lang.String.class;
        String name = cls.getName();
        logger.info("Class " + name +
                " does not implement method setDivideBy()");
        throw new JMetalException("Exception in " + name + ".setDivideBy()") ;
    }

    public int getDivideBy() throws JMetalException {
        Class<String> cls = java.lang.String.class;
        String name = cls.getName();
        logger.info("Class " + name +
                " does not implement method getDivideBy()");
        throw new JMetalException("Exception in " + name + ".getDivideBy()") ;
    }

    public void setValues(LinkedList<String> values) throws JMetalException {
        Class<String> cls = java.lang.String.class;
        String name = cls.getName();
        logger.info("Class " + name +
                " does not implement method setValues()");
        throw new JMetalException("Exception in " + name + ".setValues()") ;
    }

    public List<String> getValues() throws JMetalException {
        Class<String> cls = java.lang.String.class;
        String name = cls.getName();
        logger.info("Class " + name +
                " does not implement method getValues()");
        throw new JMetalException("Exception in " + name + ".getValues()") ;
    }
}