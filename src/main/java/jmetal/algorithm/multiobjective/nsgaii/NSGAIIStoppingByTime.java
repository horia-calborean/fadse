package jmetal.algorithm.multiobjective.nsgaii;

import java.util.Comparator;
import java.util.List;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.operator.selection.SelectionOperator;
import jmetal.core.problem.Problem;
import jmetal.core.solution.Solution;
import jmetal.core.util.evaluator.SolutionListEvaluator;

/**
 * This class shows a version of NSGA-II having a stopping condition depending on run-time
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class NSGAIIStoppingByTime<S extends Solution<?>> extends NSGAII<S> {
  private long initComputingTime ;
  private long thresholdComputingTime ;
  /**
   * Constructor
   */
  public NSGAIIStoppingByTime(Problem<S> problem, int populationSize,
                              long maxComputingTime, int matingPoolSize, int offspringPopulationSize,
                              CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
                              SelectionOperator<List<S>, S> selectionOperator, Comparator<S> dominanceComparator,
                              SolutionListEvaluator<S> evaluator) {
    super(problem, 0, populationSize, matingPoolSize, offspringPopulationSize,
            crossoverOperator, mutationOperator,
        selectionOperator, dominanceComparator, evaluator);

    initComputingTime = System.currentTimeMillis() ;
    thresholdComputingTime = maxComputingTime ;
  }

  @Override protected boolean isStoppingConditionReached() {
    long currentComputingTime = System.currentTimeMillis() - initComputingTime ;
    return currentComputingTime > thresholdComputingTime ;
  }
}
