package core.problem.ports;

import core.model.individual.FadseIndividual;
import core.model.objectives.Objective;
import input.model.InputData;
import input.model.setup.CommonSetupParameters;
import input.ports.parameter.problem.ProblemParameter;
import org.uma.jmetal.problem.doubleproblem.impl.AbstractDoubleProblem;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@SuppressWarnings("unchecked cast")
public abstract class MicroArchitectureProblem extends AbstractDoubleProblem {
    protected final InputData inputData;

    public MicroArchitectureProblem(InputData inputData) {
        this.inputData = inputData;

        ProblemParameter<?>[] designVariables = (ProblemParameter<?>[]) inputData.get(CommonSetupParameters.PARAMETERS);
        setRangeLimitsFrom(designVariables);
    }

    @Override
    public DoubleSolution evaluate(DoubleSolution doubleSolution) {
        List<String> benchmarks = (List<String>) inputData.get(CommonSetupParameters.BENCHMARKS);

        ProblemParameter<?>[] designVariables = (ProblemParameter<?>[]) inputData.get(CommonSetupParameters.PARAMETERS);
        List<Double> optimizedValues = doubleSolution.variables();
        ProblemParameter<?>[] newDesignVariables = getNewVariablesFrom(designVariables, optimizedValues);

        FadseIndividual individual;

        for (int benchmarkIndex = 0; benchmarkIndex <= benchmarks.size() - 1; benchmarkIndex++) {
            String benchmark = benchmarks.get(benchmarkIndex);

            individual = new FadseIndividual(environment, benchmark);
            individual.setParameters(newDesignVariables);

            simulate(individual);

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
            Double lower = (Double) designVariables[i].getLowerBound();
            Double upper = (Double) designVariables[i].getUpperBound();
            lowerLimits.add(lower);
            upperLimits.add(upper);
        });

        this.variableBounds(lowerLimits, upperLimits);
    }

    protected ProblemParameter<?>[] getNewVariablesFrom(ProblemParameter<?>[] designVariables, List<Double> optimizedValues) {
        ProblemParameter<?>[] newDesignVariables = new ProblemParameter<?>[designVariables.length];

        for (int i = 0; i < designVariables.length; i++) {
            newDesignVariables[i] = designVariables[i].clone();
            newDesignVariables[i].setValueFromDouble(optimizedValues.get(i));
        }

        return newDesignVariables;
    }

    protected void simulate(FadseIndividual individual) {

    }
}