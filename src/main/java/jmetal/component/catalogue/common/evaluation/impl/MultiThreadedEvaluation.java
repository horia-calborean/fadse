package jmetal.component.catalogue.common.evaluation.impl;

import java.util.List;
import jmetal.component.catalogue.common.evaluation.Evaluation;
import jmetal.core.problem.Problem;
import jmetal.core.solution.Solution;
import jmetal.core.util.errorchecking.Check;

/**
 * Class that evaluates a list of solutions using threads.
 *
 * @author Antonio J. Nebro (ajnebro@uma.es)
 *
 * @param <S>
 */
public class MultiThreadedEvaluation<S extends Solution<?>> implements Evaluation<S> {
  private int computedEvaluations;
  private final Problem<S> problem;
  private final int numberOfThreads ;

  public MultiThreadedEvaluation(int numberOfThreads, Problem<S> problem) {
    Check.that(numberOfThreads >= 0, "The number of threads is a negative value: " + numberOfThreads) ;
    Check.notNull(problem);

    if (numberOfThreads == 0) {
      numberOfThreads = Runtime.getRuntime().availableProcessors();
    }
    System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism",
        "" + numberOfThreads);

    this.numberOfThreads = numberOfThreads ;
    this.problem = problem;
    computedEvaluations = 0;
  }

  @Override
  public List<S> evaluate(List<S> solutionList) {
    Check.notNull(solutionList);
    solutionList.parallelStream().forEach(problem::evaluate);
    computedEvaluations = solutionList.size();

    return solutionList;
  }

  @Override
  public int computedEvaluations() {
    return computedEvaluations;
  }

  public int numberOfThreads() {
    return numberOfThreads ;
  }

  @Override
  public Problem<S> problem() {
    return problem ;
  }
}
