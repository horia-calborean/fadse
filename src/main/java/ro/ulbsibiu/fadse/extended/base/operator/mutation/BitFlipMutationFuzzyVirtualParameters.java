package ro.ulbsibiu.fadse.extended.base.operator.mutation;

import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.solution.Solution;

import java.util.Iterator;
import java.util.Random;

import org.uma.jmetal.solution.binarysolution.BinarySolution;
import org.uma.jmetal.solution.integersolution.impl.DefaultIntegerSolution;
import org.uma.jmetal.util.binarySet.BinarySet;
import org.uma.jmetal.util.errorchecking.Check;
import ro.ulbsibiu.fadse.simulationIO.SimulationIO;
import ro.ulbsibiu.fadse.simulationIO.parameters.simulator.impl.numeric.Exp2Parameter;
import ro.ulbsibiu.fadse.simulationIO.parameters.simulator.impl.numeric.IntegerParameter;
import ro.ulbsibiu.fadse.simulationIO.parameters.simulator.SimulatorParameter;
import ro.ulbsibiu.fadse.utils.Utils;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.rule.Variable;

public class BitFlipMutationFuzzyVirtualParameters<S extends BinarySolution> implements MutationOperator<S>  {
    private final double mutationProbability;

    protected static int STATS_CALLS = 0;
    protected static int STATS_APPLIED_FUZZY = 0;
    protected static int STATS_APPLIED = 0;
    protected static int STATS_IND_CHANGED = 0;
    protected static double STATS_CURRENT_PROB = 0;
    protected static boolean IND_CHANGED = false;
    protected static Class<DefaultIntegerSolution> INT_SOLUTION;

    public BitFlipMutationFuzzyVirtualParameters(double mutationProbability) {
        Check.probabilityIsValid(mutationProbability);
        this.mutationProbability = mutationProbability;

        INT_SOLUTION = DefaultIntegerSolution.class;
    }

    protected void doMutation(double probability, S solution, SimulationIO environment) throws JMException {
        try {
            // TODO - GO AND UPDATE getParametersAndVirtualParameters(..., ...)
            SimulatorParameter[] params = Utils.getParametersAndVirtualParameters(solution, environment);

            for (int i = 0; i < params.length; i++) {
                try {
                    String fuzzyInputFile = environment.getFuzzyInputFilePath();
                    FIS fis = FIS.load(fuzzyInputFile, true);
                    if (fis == null) {
                        throw new Exception("FCL file " + fuzzyInputFile + " was not found");
                    }
                    fillFIS(fis, params);
                    fis.evaluate();

                    Variable outputVariable = fis.getVariable("out" + params[i].getName());
                    if (outputVariable.isOutputVarable()) {
                        int COG = computeCOG(outputVariable, params[i]);

                        double prob = PseudoRandom.randDouble();
                        double fuzzyMutationProbability = computeProbabilityGaussian(outputVariable, probability);
                        STATS_CURRENT_PROB = fuzzyMutationProbability;
                        if (prob < fuzzyMutationProbability) {
                            params[i].setValue(COG);
                            solution.variables().set(i, (BinarySet) params[i].getValue());
                            STATS_APPLIED_FUZZY++;
                            IND_CHANGED = true;
                        } else {
                            throw new Exception("Apply the old mutation, this one was not selected");
                        }
                    } else {
                        throw new Exception("It is not an output variable in the FCL file");
                    }
                } catch (Exception e) {
                    if (i < solution.variables().size()) {
                        if (new Random().nextDouble() < probability) {
                            int lowerBound = (int) solution.variables().get(i).getLowerBound();
                            int upperBound = (int) solution.variables().get(i).getUpperBound();

                            int value = new Random().nextInt(upperBound - lowerBound + 1) + lowerBound;

                            solution.variables().set(i, (BinarySet) value);
                            STATS_APPLIED++;
                            IND_CHANGED = true;
                        }
                    }
                }
            }
        } catch (ClassCastException e1) {
            Configuration.logger_.severe("BitFlipMutation.doMutation: "
                    + "ClassCastException error" + e1.getMessage());
            Class<String> cls = java.lang.String.class;
            String name = cls.getName();
            throw new JMException("Exception in " + name + ".doMutation()");
        }
    }

    @Override
    public S execute(S object) {
        Solution solution = (Solution) object;

        if (solution.getClass() != INT_SOLUTION) {
            Configuration.logger_.severe("BitFlipMutation.execute: the solution "
                    + "is not of the right type. 'Int', but " + solution.getClass() + " is obtained");

            Class<String> cls = java.lang.String.class;
            String name = cls.getName();
            try {
                throw new JMException("Exception in " + name + ".execute()");
            } catch (JMException exception) {
                throw new RuntimeException(exception);
            }
        }

        Double probability = (Double) getParameter("probability");
        SimulationIO env = (SimulationIO) getParameter("environment");
        if (probability == null) {
            Configuration.logger_.severe("BitFlipMutation.execute: probability not "
                    + "specified");
            Class cls = java.lang.String.class;
            String name = cls.getName();
            try {
                throw new JMException("Exception in " + name + ".execute()");
            } catch (JMException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            doMutation(probability.doubleValue(), (S) solution, env);
        } catch (JMException e) {
            throw new RuntimeException(e);
        }
        STATS_CALLS++;
        if (IND_CHANGED) {
            STATS_IND_CHANGED++;
        }
        IND_CHANGED = false;

        return (S) solution;
    }

    @Override
    public double mutationProbability() {
        return 0;
    }

    private int computeCOG(Variable outputVariable, SimulatorParameter parameter) throws Exception {
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

    private void fillFIS(FIS fis, SimulatorParameter[] params) {
        //trying to set the input values for all the parameters
        for (SimulatorParameter p : params) {
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

    public double computeGauss(double probability) {
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
}