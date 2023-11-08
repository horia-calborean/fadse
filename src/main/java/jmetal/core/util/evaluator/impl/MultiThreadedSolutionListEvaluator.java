package jmetal.core.util.evaluator.impl;

import java.util.List;
import jmetal.core.problem.Problem;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.evaluator.SolutionListEvaluator;

/**
 * @author Antonio J. Nebro
 */
public class MultiThreadedSolutionListEvaluator<S> implements SolutionListEvaluator<S> {

  private final int numberOfThreads;

  public MultiThreadedSolutionListEvaluator(int numberOfThreads) {
    if (numberOfThreads == 0) {
      this.numberOfThreads = Runtime.getRuntime().availableProcessors();
    } else {
      this.numberOfThreads = numberOfThreads;
      System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism",
          "" + this.numberOfThreads);
    }
    String message = "Number of cores: " + numberOfThreads ;
    JMetalLogger.logger.info(message);
  }

  @Override
  public List<S> evaluate(List<S> solutionList, Problem<S> problem) {
    solutionList.parallelStream().forEach(problem::evaluate);

    return solutionList;
  }

  public int numberOfThreads() {
    return numberOfThreads;
  }

  @Override
  public void shutdown() {
    //This method is an intentionally-blank override.
  }
}
