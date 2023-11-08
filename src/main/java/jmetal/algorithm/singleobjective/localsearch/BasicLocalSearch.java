package jmetal.algorithm.singleobjective.localsearch;

import java.util.Comparator;
import jmetal.core.algorithm.impl.AbstractLocalSearch;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.problem.Problem;
import jmetal.core.solution.Solution;
import jmetal.core.util.pseudorandom.JMetalRandom;

public class BasicLocalSearch<S extends Solution<?>> extends AbstractLocalSearch<S> {

  private S initialSolution;
  private int evaluations;
  private int maxEvaluations;
  private MutationOperator<S> mutationOperator;
  private Comparator<S> comparator;
  private Problem<S> problem;

  public BasicLocalSearch(S initialSolution, int maxEvaluations,
      Problem<S> problem, MutationOperator<S> mutationOperator, Comparator<S> comparator) {
    this.initialSolution = initialSolution;
    this.maxEvaluations = maxEvaluations;
    this.mutationOperator = mutationOperator;
    this.comparator = comparator;
    this.problem = problem;
  }

  @Override
  protected S setCurrentSolution() {
    return initialSolution;
  }

  @Override
  protected void initProgress() {
    evaluations = 1;
  }

  @Override
  protected void updateProgress() {
    evaluations++;
  }

  @Override
  protected boolean isStoppingConditionReached() {
    return evaluations >= maxEvaluations;
  }

  @Override
  protected S updateCurrentSolution(S currentSolution) {
    S newSolution = mutationOperator.execute((S) currentSolution.copy());
    problem.evaluate(newSolution);

    int result = comparator.compare(newSolution, currentSolution);
    if ((result == -1) || ((result == 0) && (
        JMetalRandom.getInstance().getRandomGenerator().nextDouble() < 0.5))) {
      currentSolution = newSolution;
    }

    return currentSolution;
  }

  @Override
  public String name() {
    return "Basic local search";
  }

  @Override
  public String description() {
    return "Basic local search";
  }
}
