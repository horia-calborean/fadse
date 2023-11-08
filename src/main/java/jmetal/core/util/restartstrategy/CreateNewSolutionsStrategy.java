package jmetal.core.util.restartstrategy;

import java.util.List;
import jmetal.core.problem.DynamicProblem;
import jmetal.core.solution.Solution;

/**
 * @author Antonio J. Nebro
 */
public interface CreateNewSolutionsStrategy<S extends Solution<?>> {
  /**
   * Add a number of new solutions to a list of {@link Solution} objects
   * @param solutionList
   * @param problem
   * @param numberOfNewSolutions
   */
  void create(List<S> solutionList, DynamicProblem<S, ?> problem, int numberOfNewSolutions) ;
}
