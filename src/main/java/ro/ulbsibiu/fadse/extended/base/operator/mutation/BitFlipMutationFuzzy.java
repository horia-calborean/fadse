/**
 * BitFlipMutationFuzzy.java
 * @author Juan J. Durillo
 * @author Antonio J. Nebro
 * @version 1.1
 */
package ro.ulbsibiu.fadse.extended.base.operator.mutation;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import jmetal.base.Solution;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;
import jmetal.base.operator.mutation.Mutation;

import java.util.Iterator;

import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.environment.parameters.Exp2Parameter;
import ro.ulbsibiu.fadse.environment.parameters.IntegerParameter;
import ro.ulbsibiu.fadse.environment.parameters.Parameter;
import ro.ulbsibiu.fadse.utils.Utils;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.rule.Variable;

/**
 * This class implements a bit flip mutation operator.
 * NOTE: the operator is applied to binary or integer solutions, considering the
 * whole solution as a single variable.
 */
public class BitFlipMutationFuzzy extends Mutation {
    public static int STATS_CALLS = 0;
    private static int STATS_APPLIED_FUZZY = 0;
    private static int STATS_APPLIED = 0;
    private static int STATS_IND_CHANGED = 0;
    private static double STATS_CURRENT_PROB = 0;
    private static boolean IND_CHANGED = false;
    /**
     * INT_SOLUTION represents class jmetal.base.solutionType.IntSolutionType
     */
    private static Class INT_SOLUTION;

    /**
     * Constructor
     * Creates a new instance of the Bit Flip mutation operator
     */
    public BitFlipMutationFuzzy() {
        try {
            INT_SOLUTION = Class.forName("jmetal.base.solutionType.IntSolutionType");
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } // catch
    } // BitFlipMutationFuzzy

    /**
     * Constructor
     * Creates a new instance of the Bit Flip mutation operator
     */
    public BitFlipMutationFuzzy(Properties properties) {
        this();
    } // BitFlipMutationFuzzy

    /**
     * Perform the mutation operation
     * @param probability Mutation probability
     * @param solution The solution to mutate
     * @throws JMException
     */
    public void doMutation(double probability, Solution solution, Environment env) throws JMException {
        try {
            Parameter[] params = Utils.getParametersAndVitualParameters(solution, env);
            // Integer representation
            for (int i = 0; i < params.length; i++) {
                //i have set the value, now I have to transform it
                try {
                    String fuzzyInputFile = env.getFuzzyInputFile();
                    FIS fis = FIS.load(fuzzyInputFile, true);//TODO take from xml
                    if (fis == null) {
                        throw new Exception("FCL file " + fuzzyInputFile + " was not found");
                    }
                    fillFIS(fis, params);
                    fis.evaluate();
                    //trying to get the output for this variable. if it thrwos an exception (the current varaible is not defined in the fcl file we turn to original bit flip mutation - see catch)
                    Variable outputVariable = fis.getVariable("out" + params[i].getName());
                    if (outputVariable.isOutputVarable()) {
                        int COG = computeCOG(outputVariable, params[i]);
                        //Should we apply the mutation according to fuzzy info?
                        double prob = PseudoRandom.randDouble();
                        double fuzzyMutationProbability = computeProbabilityGaussian(outputVariable, probability);
                        STATS_CURRENT_PROB = fuzzyMutationProbability;
                        if (prob < fuzzyMutationProbability) {
                            params[i].setValue(COG);
                            solution.getDecisionVariables()[i].setValue((double) params[i].getVariable().getValue());
                            STATS_APPLIED_FUZZY++;
                            IND_CHANGED = true;
                        } else {
                            throw new Exception("Apply the old mutation, this one was not selected");
                        }
                    } else {
                        throw new Exception("It is not an output variable in the FCL file");
                    }
                } catch (Exception e) {
//                    Logger.getLogger(BitFlipMutationFuzzy.class.getName()).log(Level.INFO, "Something went rong for(" + e.getMessage() + "): " + params[i].getName() + ". Switching to old bit flip mutation");
                    if (PseudoRandom.randDouble() < probability) {
                        int value = (int) (PseudoRandom.randInt(
                                (int) solution.getDecisionVariables()[i].getUpperBound(),
                                (int) solution.getDecisionVariables()[i].getLowerBound()));
                        solution.getDecisionVariables()[i].setValue(value);
                        STATS_APPLIED++;
                        IND_CHANGED = true;
                    }
                }

            }
        } catch (ClassCastException e1) {
            Configuration.logger_.severe("BitFlipMutation.doMutation: "
                    + "ClassCastException error" + e1.getMessage());
            Class cls = java.lang.String.class;
            String name = cls.getName();
            throw new JMException("Exception in " + name + ".doMutation()");
        }
    } // doMutation

    /**
     * Executes the operation
     * @param object An object containing a solution to mutate
     * @return An object containing the mutated solution
     * @throws JMException 
     */
    public Object execute(Object object) throws JMException {
//        System.out.println("BitFlipMutationFuzzy called");
        Solution solution = (Solution) object;

        if (solution.getType().getClass() != INT_SOLUTION) {
            Configuration.logger_.severe("BitFlipMutation.execute: the solution "
                    + "is not of the right type. 'Int', but " + solution.getType() + " is obtained");

            Class cls = java.lang.String.class;
            String name = cls.getName();
            throw new JMException("Exception in " + name + ".execute()");
        } // if 

        Double probability = (Double) getParameter("probability");
        Environment env = (Environment) getParameter("environment");
        if (probability == null) {
            Configuration.logger_.severe("BitFlipMutation.execute: probability not "
                    + "specified");
            Class cls = java.lang.String.class;
            String name = cls.getName();
            throw new JMException("Exception in " + name + ".execute()");
        }

        doMutation(probability.doubleValue(), solution, env);
        STATS_CALLS++;
        if (IND_CHANGED) {
            STATS_IND_CHANGED++;
        }
        IND_CHANGED = false;

        System.out.println("CALLS: " + STATS_CALLS + " APPLIED BIT FLIP: " + STATS_APPLIED + " APPLIED FUZZY: " + STATS_APPLIED_FUZZY + " CHANGED INDIVIDUALS: " + STATS_IND_CHANGED + " Curent prob: " + STATS_CURRENT_PROB);

        return solution;
    } // execute

    private int computeCOG(Variable outputVariable, Parameter parameter) throws Exception {
        double COG_temp = outputVariable.defuzzify();
        //at this point we know that there is an output defined in the fcl file for this parameter
        if (COG_temp == -1) {
            throw new Exception("Incomplete system of rules. No rule defined in the interval");
        }
        // System.out.println("Fuzzy mutation can be applied - COG (before normalization): " + COG_temp);
        int COG = 0;
        //converting the output from the fuzzy rules to FADSE internal representation (integers)
        if (parameter instanceof IntegerParameter) {
            COG = (int) Math.round(COG_temp / ((IntegerParameter) parameter).getStep());
        } else if (parameter instanceof Exp2Parameter) {
            // Math.log is base e, natural log, ln
            COG = (int) (Math.log(COG_temp) / Math.log(2));
        } else {
            throw new Exception("NOT supported type of parameter");
        }
        // System.out.println("Normalised COG: " + COG);
        //COG is an int value now
        return COG;
    }

    private void fillFIS(FIS fis, Parameter[] params) {
        //trying to set the input values for all the parameters
        for (Parameter p : params) {
            try {
                double val = (new Double((Integer) p.getValue())).doubleValue();
                fis.setVariable(p.getName(), val);
            } catch (java.lang.RuntimeException e) {
//                            Logger.getLogger(BitFlipMutationFuzzy.class.getName()).log(Level.INFO, "TODO REMOVE MESSAGE Could not set FIS variable: " + p.getName() + " " + p.getValue());
//                            e.printStackTrace();
            }
        }
    }

    private double obtainMaxMemebership(Variable outputVariable) {
        double maxMemebership = 0;
        for (Iterator<String> it = outputVariable.iteratorLinguisticTermNames(); it.hasNext();) {
            double membership = outputVariable.getMembership(it.next());
            if (membership > maxMemebership) {
                maxMemebership = membership;
            }
        }
        return maxMemebership;
    }
    public static int COUNT = 500;
    public static int x = 0;
    public static double MAX_PROBABILITY = 0.8;

    private double computeProbabilityLinear(Variable outputVariable, double probability) {
        //look through the memebership values on all the memebership functions and find the maximum.
        //this maxMembership will give us an inf on hou much should we trust this value (we can use it or not)
        double maxMemebership = obtainMaxMemebership(outputVariable);
        //we will compute the equation for the line that passes through point [0,max(MAX_PROBABILITY,probability)] and point [COUNT, probability]
        //for example if MAX_PROBABILITY is 0.8 and probability (set in the properties file is 0.1)
        //and COUNT is 500 (=after 500 individuals sent to mutation we want teh fuzzy mutation to happen less often)
        //we will compute the equation of the line that passes
        //through points [0,0.8] and [500,0.1]
        // y = a*x+b        if x<=COUNT
        //   = probability  otherwise
        double b = Math.max(0.8, probability);
        double a = (probability - b) / COUNT;
        double y = 0;
        if (x <= COUNT) {
            y = a * x + b;
        } else {
            y = probability;
        }
        x = x + 1;
        //we multiply the y value with teh maxMembership
        return y * maxMemebership;
    }

    private double computeProbabilityGaussian(Variable outputVariable, double probability) {
        //look through the memebership values on all the memebership functions and find the maximum.
        //this maxMembership will give us an inf on hou much should we trust this value (we can use it or not)
        double maxMemebership = obtainMaxMemebership(outputVariable);
        //y_gauss = a*e^(-(((x-b)^2)/2*c))
        //y = a*e^(-(((x-b)^2)/2*c)) + probability //translation
        //a = 1-probability
        //b = 0
        //c = 150 //=>when x is approx 500, y is approximative = probabiliy
        double y = computeGauss(probability);
//        System.out.println("MAX_Memebership: " + maxMemebership);
//        System.out.println("Gauss output" + y);
        return y * maxMemebership * 0.8;
    }
    public double computeGauss(double probability){
        double a = 1 - probability;
        double b = 0;
        double c = 50;
        double temp = Math.pow((x - b), 2); //(x-b)^2
        temp = temp / (2 * c);
        temp = -temp;
        double y = a * Math.pow(Math.E, temp) + probability;
        x = x + 1;
        return y;
    }
    public static void main(String[] args) {
        BitFlipMutationFuzzy mutation = new BitFlipMutationFuzzy();
        for (int i = 0; i < 500; i++) {
            System.out.print(mutation.computeGauss(0.10)+",");
        }
    }
} // BitFlipMutationFuzzy

