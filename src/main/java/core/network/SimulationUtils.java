package core.network;

import input.model.setup.CommonSetupParameters;
import org.uma.jmetal.solution.Solution;
import core.model.objectives.Objective;

import java.util.*;

public class SimulationUtils {
    public static <S extends Solution<?>> List<S> insertObjectivesValuesIntoSolutions(SimulationStatus simulationStatus) {
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
                int benchmarkSize = ((List<String>) simulationStatus.getInputData().get(CommonSetupParameters.BENCHMARKS)).size();
                value = value / benchmarkSize;//compute the average
//                System.out.println("FINAL for solution["+s.getDecisionVariables()+"] for objective["+i+"] = "+value);
                s.objectives()[i] = value;
            }
        }
        return population;
    }
}