package core.problem.ports;

import core.model.individual.FadseIndividual;
import core.model.objectives.Objective;
import core.network.ClientsRepository;
import input.model.InputData;
import input.model.setup.CommonSetupParameters;
import input.ports.parameter.problem.ProblemParameter;
import org.uma.jmetal.problem.doubleproblem.impl.AbstractDoubleProblem;
import org.uma.jmetal.problem.integerproblem.impl.AbstractIntegerProblem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.solution.integersolution.IntegerSolution;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@SuppressWarnings("unchecked cast")
public abstract class MicroArchitectureProblem extends AbstractIntegerProblem {
    protected final InputData inputData;

    public MicroArchitectureProblem(InputData inputData) {
        this.inputData = inputData;

        ProblemParameter<?>[] designVariables = (ProblemParameter<?>[]) inputData.get(CommonSetupParameters.DESIGN_VARIABLES);
        setRangeLimitsFrom(designVariables);

        Map<String, Objective> objectives = (Map<String, Objective>) inputData.get(CommonSetupParameters.OBJECTIVES);
        numberOfObjectives(objectives.size());
    }

    @Override
    public IntegerSolution evaluate(IntegerSolution integerSolution) {
        List<String> benchmarks = (List<String>) inputData.get(CommonSetupParameters.BENCHMARKS);

        ProblemParameter<?>[] designVariables = (ProblemParameter<?>[]) inputData.get(CommonSetupParameters.DESIGN_VARIABLES);
        List<Integer> optimizedValues = integerSolution.variables();
        ProblemParameter<?>[] newDesignVariables = getNewVariablesFrom(designVariables, optimizedValues);

        FadseIndividual individual;

        for (int benchmarkIndex = 0; benchmarkIndex <= benchmarks.size() - 1; benchmarkIndex++) {
            String benchmark = benchmarks.get(benchmarkIndex);

            individual = new FadseIndividual(inputData, benchmark);
            individual.setParameters(newDesignVariables);

            sendForSimulation(individual, integerSolution);

            // TODO - Andrei -> The app does not wait here for the response
            List<Objective> evaluatedObjectives = individual.getObjectives();

            int j = 0;
            for (Objective objective : evaluatedObjectives) {
                double value = integerSolution.objectives()[j];

                value = (objective.getValue() + (benchmarkIndex) * value) / (benchmarkIndex + 1);

                integerSolution.objectives()[j] = value;
                j++;
            }
        }

        return integerSolution;
    }

    protected void setRangeLimitsFrom(ProblemParameter<?>[] designVariables) {
        List<Integer> lowerLimits = new ArrayList<>(designVariables.length);
        List<Integer> upperLimits = new ArrayList<>(designVariables.length);

        IntStream.range(0, designVariables.length).forEach((i) -> {
            Integer lower = ((Number) designVariables[i].getLowerBound()).intValue();
            Integer upper = ((Number) designVariables[i].getUpperBound()).intValue();
            lowerLimits.add(lower);
            upperLimits.add(upper);
        });

        variableBounds(lowerLimits, upperLimits);
    }

    protected ProblemParameter<?>[] getNewVariablesFrom(ProblemParameter<?>[] designVariables, List<Integer> optimizedValues) {
        ProblemParameter<?>[] newDesignVariables = new ProblemParameter<?>[designVariables.length];

        for (int i = 0; i < designVariables.length; i++) {
            newDesignVariables[i] = designVariables[i].clone();
            newDesignVariables[i].setValueFromDouble(optimizedValues.get(i));
        }

        return newDesignVariables;
    }

    protected void sendForSimulation(FadseIndividual individual, Solution<?> currentSolution) {
        ClientsRepository clientsRepository;

        try {
            clientsRepository = ClientsRepository.getInstance(inputData);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        clientsRepository.performSimulation(individual, currentSolution);
    }
}