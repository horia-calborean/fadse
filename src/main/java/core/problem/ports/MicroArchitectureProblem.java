package core.problem.ports;

import core.model.objectives.Objective;
import input.model.InputData;
import input.model.setup.CommonSetupParameters;
import input.ports.parameter.problem.ProblemParameter;
import org.uma.jmetal.problem.doubleproblem.impl.AbstractDoubleProblem;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;

import java.util.List;

@SuppressWarnings("unchecked cast")
public abstract class MicroArchitectureProblem extends AbstractDoubleProblem {
    protected final InputData inputData;

    public MicroArchitectureProblem(InputData inputData) {
        this.inputData = inputData;
    }

    @Override
    public DoubleSolution evaluate(DoubleSolution doubleSolution) {
        List<String> benchmarks = (List<String>) inputData.get(CommonSetupParameters.BENCHMARKS);

        ProblemParameter<?>[] parameters = Utils.getParameters(solution, environment);

        Individual ind;

        for (int benchmarkIndex = 0; benchmarkIndex <= benchmarks.size() - 1; benchmarkIndex++) {
            String benchmark = benchmarks.get(benchmarkIndex);

            ind = new Individual(environment, benchmark);
            ind.setParameters(parameters);

            simulate(ind);

            List<Objective> evaluatedObjectives = ind.getObjectives();

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

    protected abstract void simulate();
}