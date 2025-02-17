package ro.ulbsibiu.fadse.utils;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.util.errorchecking.JMetalException;
import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.environment.Objective;
import ro.ulbsibiu.fadse.environment.parameters.Parameter;
import ro.ulbsibiu.fadse.environment.parameters.VirtualParameter;
import ro.ulbsibiu.fadse.extended.problems.simulators.network.Message;
import ro.ulbsibiu.fadse.extended.problems.simulators.network.server.status.SimulationStatus;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Utils {
    private Random r;

    public Random getRandom() {
        if (r == null) {
            r = new Random();
        }
        return r;
    }

    public <S extends Solution<?>> String generateCSV(List<S> population) {
        StringBuilder csvOutput = new StringBuilder();

        for (S individual : population) {
            StringBuilder csvLine = new StringBuilder();
            List<?> individualVariables = individual.variables();

            for (Object variable : individualVariables) {
                try {
                    csvLine.append(variable).append(",");
                } catch (JMetalException ex) {
                    Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
                    csvLine.append("unknown" + ",");
                }
            }

            int numberOfObjectives = individual.objectives().length;

            for (int objectiveIndex = 0; objectiveIndex < numberOfObjectives; objectiveIndex++) {
                double objVal = individual.objectives()[objectiveIndex];
                csvLine.append(objVal).append(",");
            }

            csvLine = new StringBuilder(csvLine.substring(0, csvLine.length() - 1));
            csvLine.append(System.lineSeparator());
            csvOutput.append(csvLine);
        }
        return csvOutput.toString();
    }

    public String generateCSVHeader(Environment environment) {
        StringBuilder header = new StringBuilder();
        for (Parameter p : environment.getInputDocument().getParameters()) {
            header.append(p.getName()).append(",");
        }
        for (Objective o : environment.getInputDocument().getObjectives().values()) {
            header.append(o.getName()).append(",");
        }
        header = new StringBuilder(header.substring(0, header.length() - 1));
        header.append(System.lineSeparator());
        return header.toString();
    }

    public <S extends Solution<?>> List<S> insertObjectivesValuesIntoSolutions(SimulationStatus simulationStatus) {
        // extract all the solutions from the simulation status and build new objects, so we will work on local data
        List<Message> filledMessages = simulationStatus.getReceiver().getResults();
        List<S> population = new ArrayList<>();
        Map<String, S> solMap = new HashMap<>();

        for (Message filledM : filledMessages) {
            for (Message sentM : simulationStatus.getSentMessages()) {
                if (filledM.getMessageId().equals(sentM.getMessageId())) {
                    //obtain the solution of this individual
                    S temp = simulationStatus.getSolution(sentM.getMessageId());
                    S s = (S) temp.copy();
                    population.add(s);
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
                        S s = solMap.get(sentM.getMessageId());
                        double value = s.objectives()[i];
                        value = (o.getValue() + value);//Add all the values. later we will divide it by the number of benchmarks
                        s.objectives()[i] = value;
                        i++;
                    }
                }
            }
        }

        //compute the average
        //since the same solution exists  nrOfBenchmarks times in sent messages list we have to divide by nr of benchmarks only once,
        //so we first build a set of all the solutions (no duplicates)
        Set<S> solutions = new HashSet<>();
        for (Message sentM : simulationStatus.getSentMessages()) {
            S s = solMap.get(sentM.getMessageId());
            solutions.add(s);
        }
        for (S s : solutions) {
            for (int i = 0; i < s.objectives().length; i++) {
                double value = s.objectives()[i];
                value = value / simulationStatus.getEnvironment().getInputDocument().getBenchmarks().size();//compute the average
//                System.out.println("FINAL for solution["+s.getDecisionVariables()+"] for objective["+i+"] = "+value);
                s.objectives()[i] = value;
            }
        }
        return population;
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

    public static Parameter[] getParameters(DoubleSolution solution, Environment environment) {
        List<Double> vars = solution.variables();
        Parameter[] params = environment.getInputDocument().getParameters();
        for (int i = 0; i < vars.size(); i++) {
            try {
                Parameter p = params[i];
                Parameter parameter = (Parameter) p.clone();
                params[i] = parameter;
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, "cloning of the parameter was not supported", ex);
            }
        }
        return params;
    }

    public static <S extends Solution<?>> Parameter[] getParametersAndVirtualParameters(S solution, Environment environment) {
        List<Object> vars = (List<Object>) solution.variables();
        Parameter[] params = new Parameter[environment.getInputDocument().getParameters().length+environment.getInputDocument().getVirtualParameters().length];
        for (int i = 0; i < vars.size(); i++) {
            try {
                Parameter p = environment.getInputDocument().getParameters()[i];
                Parameter parameter = (Parameter) p.clone();
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
                    } catch (Exception ignored) {}
                }
            }
            Parameter[] virtualParameters = environment.getInputDocument().getVirtualParameters();
            Parameter[] origParams = environment.getInputDocument().getParameters();
            System.arraycopy(virtualParameters, 0, params, origParams.length, virtualParameters.length);

        }
        return params;
    }
}