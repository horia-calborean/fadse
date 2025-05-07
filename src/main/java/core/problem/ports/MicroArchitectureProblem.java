package core.problem.ports;

import core.model.individual.FadseIndividual;
import core.model.objectives.Objective;
import core.network.ClientsRepository;
import input.model.InputData;
import input.model.setup.CommonSetupParameters;
import input.ports.parameter.problem.ProblemParameter;
import org.uma.jmetal.problem.doubleproblem.impl.AbstractDoubleProblem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@SuppressWarnings("unchecked cast")
public abstract class MicroArchitectureProblem extends AbstractDoubleProblem {
    protected final InputData inputData;

    public MicroArchitectureProblem(InputData inputData) {
        this.inputData = inputData;

        ProblemParameter<?>[] designVariables = (ProblemParameter<?>[]) inputData.get(CommonSetupParameters.DESIGN_VARIABLES);
        setRangeLimitsFrom(designVariables);

        Map<String, Objective> objectives = (Map<String, Objective>) inputData.get(CommonSetupParameters.OBJECTIVES);
        numberOfObjectives(objectives.size());
    }

    @Override
    public DoubleSolution evaluate(DoubleSolution doubleSolution) {
        List<String> benchmarks = (List<String>) inputData.get(CommonSetupParameters.BENCHMARKS);

        ProblemParameter<?>[] designVariables = (ProblemParameter<?>[]) inputData.get(CommonSetupParameters.DESIGN_VARIABLES);
        List<Double> optimizedValues = doubleSolution.variables();
        ProblemParameter<?>[] newDesignVariables = getNewVariablesFrom(designVariables, optimizedValues);

        FadseIndividual individual;

        for (int benchmarkIndex = 0; benchmarkIndex <= benchmarks.size() - 1; benchmarkIndex++) {
            String benchmark = benchmarks.get(benchmarkIndex);

            individual = new FadseIndividual(inputData, benchmark);
            individual.setParameters(newDesignVariables);

            sendForSimulation(individual, doubleSolution);

            List<Objective> evaluatedObjectives = individual.getObjectives();

            int j = 0;
            for (Objective objective : evaluatedObjectives) {
                double value = doubleSolution.objectives()[j];

                value = (objective.getValue() + (benchmarkIndex) * value) / (benchmarkIndex + 1);

                doubleSolution.objectives()[j] = value;
                j++;
            }
        }

        return doubleSolution;
    }

    protected void setRangeLimitsFrom(ProblemParameter<?>[] designVariables) {
        List<Double> lowerLimits = new ArrayList<>(designVariables.length);
        List<Double> upperLimits = new ArrayList<>(designVariables.length);

        IntStream.range(0, designVariables.length).forEach((i) -> {
            Double lower = ((Number) designVariables[i].getLowerBound()).doubleValue();
            Double upper = ((Number) designVariables[i].getUpperBound()).doubleValue();
            lowerLimits.add(lower);
            upperLimits.add(upper);
        });

        variableBounds(lowerLimits, upperLimits);
    }

    protected ProblemParameter<?>[] getNewVariablesFrom(ProblemParameter<?>[] designVariables, List<Double> optimizedValues) {
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