package ro.ulbsibiu.fadse.utils;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.uma.jmetal.util.VectorUtils;
import org.uma.jmetal.util.errorchecking.JMetalException;
import ro.ulbsibiu.fadse.environment.SimulationIO;
import ro.ulbsibiu.fadse.environment.Objective;
import ro.ulbsibiu.fadse.environment.parameters.SimulatorParameter;
import ro.ulbsibiu.fadse.environment.parameters.VirtualParameter;
import ro.ulbsibiu.fadse.extended.problems.simulators.network.Message;
import ro.ulbsibiu.fadse.extended.problems.simulators.network.server.status.SimulationStatus;
import jmetal.base.Solution;
import jmetal.base.SolutionSet;
import jmetal.base.Variable;

public class Utils<S extends org.uma.jmetal.solution.Solution<?>> {
    public String generateCSV(List<S> solutionList) {
        StringBuilder csvOutput = new StringBuilder();
        int noOfSolutions = solutionList.size();

        for (int solutionIndex = 0; solutionIndex <= noOfSolutions - 1; solutionIndex++) {
            StringBuilder csvLine = new StringBuilder();
            S solution = solutionList.get(solutionIndex);

            List<?> variables = solution.variables();
            int noOfVariables = variables.size();

            for (int variableIndex = 0; variableIndex <= noOfVariables - 1; variableIndex++) {
                try {
                    csvLine.append(variables.get(variableIndex)).append(",");
                } catch (JMetalException ex) {
                    Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
                    csvLine.append("unknown" + ",");
                }
            }

            double[] objectives = solution.objectives();
            int noOfObjectives = solution.objectives().length;

            for (int objectiveIndex = 0; objectiveIndex <= noOfObjectives - 1; objectiveIndex++) {
                double objective = objectives[objectiveIndex];
                csvLine.append(objective).append(",");
            }
            csvLine = new StringBuilder(csvLine.substring(0, csvLine.length() - 1));
            csvLine.append(System.lineSeparator());
            csvOutput.append(csvLine);
        }
        return csvOutput.toString();
    }

    public String generateCSVHeader(SimulationIO simulationIO) {
        StringBuilder header = new StringBuilder();

        SimulatorParameter[] designSpaceParameters = simulationIO.getDesignSpaceDocument().getParameters();

        for (SimulatorParameter p : designSpaceParameters) {
            header.append(p.getName()).append(",");
        }

        for (Objective o : simulationIO.getDesignSpaceDocument().getObjectives().values()) {
            header.append(o.getName()).append(",");
        }

        header = new StringBuilder(header.substring(0, header.length() - 1));
        header.append(System.lineSeparator());

        return header.toString();
    }

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
                value = value / simulationStatus.getEnvironment().getDesignSpaceDocument().getBenchmarks().size();//compute the average
//                System.out.println("FINAL for solution["+s.getDecisionVariables()+"] for objective["+i+"] = "+value);
                s.setObjective(i, value);
            }
        }
        return solSet;
    }

    public static SimulatorParameter[] getParameters(Solution solution, SimulationIO environment) {
        Variable[] vars = solution.getDecisionVariables();
        SimulatorParameter[] params = environment.getDesignSpaceDocument().getParameters();
        /** for all variables... associate them with a parameter */
        for (int i = 0; i < vars.length; i++) {
            try {
                SimulatorParameter p = params[i];
                SimulatorParameter parameter = (SimulatorParameter) p.clone();
                parameter.setValue(vars[i]);
                params[i] = parameter;
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, "cloning of the parameter was not supported", ex);
            }
        }
        return params;
    }

    public static SimulatorParameter[] getParametersAndVirtualParameters(org.uma.jmetal.solution.Solution solution, SimulationIO environment) {
        double[] vars = VectorUtils.toArray(solution.variables());
        int paramsLength = environment.getDesignSpaceDocument().getParameters().length * 2;
        SimulatorParameter[] params = new SimulatorParameter[paramsLength];

        for (int i = 0; i < vars.length; i++) {
            try {
                SimulatorParameter p = environment.getDesignSpaceDocument().getParameters()[i];
                SimulatorParameter parameter = (SimulatorParameter) p.clone();
                parameter.setValue(vars[i]);
                params[i] = parameter;
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, "cloning of the parameter was not supported", ex);
            }
        }
        if (environment.getDesignSpaceDocument().getVirtualParameters() != null) {
            for (SimulatorParameter p : environment.getDesignSpaceDocument().getVirtualParameters()) {
                VirtualParameter e = (VirtualParameter) p;
                for (SimulatorParameter param : environment.getDesignSpaceDocument().getParameters()) {
                    try {
                        e.addVariable(param.getName(), new Double((Integer) param.getValue()));
                    } catch (Exception ex) {}
                }
            }
            SimulatorParameter[] virtualParameters = environment.getDesignSpaceDocument().getVirtualParameters();
            SimulatorParameter[] origParams = environment.getDesignSpaceDocument().getParameters();
            System.arraycopy(virtualParameters, 0, params, origParams.length, virtualParameters.length);

        }
        return params;
    }
}