package ro.ulbsibiu.fadse.utils;

//
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.environment.Objective;
import ro.ulbsibiu.fadse.environment.parameters.ExpresionParameter;
import ro.ulbsibiu.fadse.environment.parameters.Parameter;
import ro.ulbsibiu.fadse.environment.parameters.VirtualParameter;
import ro.ulbsibiu.fadse.extended.problems.simulators.network.Message;
import ro.ulbsibiu.fadse.extended.problems.simulators.network.server.status.SimulationStatus;
import jmetal.base.Solution;
import jmetal.base.SolutionSet;
import jmetal.base.Variable;
import jmetal.util.JMException;

public class Utils {

    private Random r;

    public Random getRandom() {
        if (r == null) {
            r = new Random();
        }
        return r;
    }

    public String generateCSV(SolutionSet s) {
        String csvOutput = "";
        for (int i = 0; i < s.size(); i++) {
            String csvLine = "";
            Solution solution = s.get(i);
            for (Variable v : solution.getDecisionVariables()) {
                try {
                    csvLine += v.getValue() + ",";
                } catch (JMException ex) {
                    Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
                    csvLine += "unknown" + ",";
                }
            }
            for (int j = 0; j < solution.numberOfObjectives(); j++) {
                double objVal = solution.getObjective(j);
                csvLine += Double.toString(objVal) + ",";
            }
            csvLine = csvLine.substring(0, csvLine.length() - 1);
            csvLine += System.getProperty("line.separator");
            csvOutput += csvLine;
        }
        return csvOutput;
    }

    public String generateCSVHeadder(Environment environment) {
        String headder = "";
        for (Parameter p : environment.getInputDocument().getParameters()) {
            headder += p.getName() + ",";
        }
        for (Objective o : environment.getInputDocument().getObjectives().values()) {
            headder += o.getName() + ",";
        }
        headder = headder.substring(0, headder.length() - 1);
        headder += System.getProperty("line.separator");
        return headder;
    }

    /**
     *
     * @param simulationStatus
     * @return a new SolutionSet containing solutions with filled objectives
     */
    public SolutionSet insertObjectivesValuesIntoSolutions(SimulationStatus simulationStatus) {
        //extract all the solutions from the simualtion status and build new objects so we will work on local data
        List<Message> filledMessages = simulationStatus.getReceiver().getResults();
        SolutionSet solSet = new SolutionSet();
        Map<String, Solution> solMap = new HashMap<String, Solution>();
        for (Message filledM : filledMessages) {
            for (Message sentM : simulationStatus.getSentMessages()) {
                if (filledM.getMessageId().equals(sentM.getMessageId())) {
                    //obtain the solution of this individual
                    Solution temp = simulationStatus.getSolution(sentM.getMessageId());
                    Solution s = new Solution(temp);
                    solSet.add(s);
                    solMap.put(sentM.getMessageId(), s);
                }
            }
        }
        for (Message filledM : filledMessages) {
            for (Message sentM : simulationStatus.getSentMessages()) {
                if (filledM.getMessageId().equals(sentM.getMessageId())) {
                    List<Objective> objs = filledM.getIndividual().getObjectives();
                    int i = 0;
                    for (Objective o : objs) {
                        //obtain the solution of this individual
                        Solution s = solMap.get(sentM.getMessageId());
                        double value = s.getObjective(i);
                        value = (o.getValue() + value);//Add all the values. later we will divide it by the number of benchmarks
                        s.setObjective(i, value);
                        i++;
                    }
                }
            }
        }

        //compute the average
        //since the same solution exists  nrOfBenchmarks times in the sent messages list we have to divide by nr of benchmarks only once
        //so we first build a set of all the solutions (no duplciates)
        Set<Solution> solutions = new HashSet<Solution>();
        for (Message sentM : simulationStatus.getSentMessages()) {
            Solution s = solMap.get(sentM.getMessageId());
            solutions.add(s);
        }
        for (Solution s : solutions) {
            for (int i = 0; i < s.numberOfObjectives(); i++) {
                double value = s.getObjective(i);
                value = value / simulationStatus.getEnvironment().getInputDocument().getBenchmarks().size();//compute the average
//                System.out.println("FINAL for solution["+s.getDecisionVariables()+"] for objective["+i+"] = "+value);
                s.setObjective(i, value);
            }
        }
        return solSet;
    }

    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public static double[] concat(double[] first, double[] second) {
        double[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public static <T> T[] concatAll(T[] first, T[]... rest) {
        int totalLength = first.length;
        for (T[] array : rest) {
            totalLength += array.length;
        }
        T[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (T[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    public static Parameter[] getParameters(Solution solution, Environment environment) {
        Variable[] vars = solution.getDecisionVariables();
        Parameter[] params = environment.getInputDocument().getParameters();
        /** for all variables... associate them with a parameter */
        for (int i = 0; i < vars.length; i++) {
            try {
                Parameter p = params[i];
                Parameter parameter = (Parameter) p.clone();
//                System.out.printf("param %s - variable %s\n", parameter.getName(), vars[i].getValue());
                parameter.setVariable(vars[i]);
                //System.out.printf("%d - %d", vars[i].getValue(), parameter.getValue());
                params[i] = parameter;
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, "cloning of the parameter was not supported", ex);
            }
        }
        return params;
    }

    public static Parameter[] getParametersAndVitualParameters(Solution solution, Environment environment) {
        Variable[] vars = solution.getDecisionVariables();
        Parameter[] params = new Parameter[environment.getInputDocument().getParameters().length+environment.getInputDocument().getVirtualParameters().length];
        /** for all variables... associate them with a parameter */
        for (int i = 0; i < vars.length; i++) {
            try {
                Parameter p = environment.getInputDocument().getParameters()[i];
                Parameter parameter = (Parameter) p.clone();
//                System.out.printf("param %s - variable %s\n", parameter.getName(), vars[i].getValue());
                parameter.setVariable(vars[i]);
                //System.out.printf("%d - %d", vars[i].getValue(), parameter.getValue());
                params[i] = parameter;
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, "cloning of the parameter was not supported", ex);
            }
        }
        if (environment.getInputDocument().getVirtualParameters() != null) {
            for (Parameter p : environment.getInputDocument().getVirtualParameters()) {
                VirtualParameter e = (VirtualParameter) p;
                for (Parameter param : environment.getInputDocument().getParameters()) {
                    try {
                        e.addVariable(param.getName(), new Double((Integer) param.getValue()));
                    } catch (Exception ex) {}
                }
            }
            Parameter[] virtualParameters = environment.getInputDocument().getVirtualParameters();
            Parameter[] origParams = environment.getInputDocument().getParameters();
            System.arraycopy(virtualParameters, 0, params, origParams.length, virtualParameters.length);

        }
        return params;
    }
}
