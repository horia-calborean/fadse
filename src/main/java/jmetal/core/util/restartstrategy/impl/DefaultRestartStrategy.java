package jmetal.core.util.restartstrategy.impl;

import java.util.List;
import jmetal.core.problem.DynamicProblem;
import jmetal.core.solution.Solution;
import jmetal.core.util.restartstrategy.CreateNewSolutionsStrategy;
import jmetal.core.util.restartstrategy.RemoveSolutionsStrategy;
import jmetal.core.util.restartstrategy.RestartStrategy;

/**
 * Created by antonio on 6/06/17.
 */
public class DefaultRestartStrategy<S extends Solution<?>> implements RestartStrategy<S> {
  private final RemoveSolutionsStrategy<S> removeSolutionsStrategy;
  private final CreateNewSolutionsStrategy<S> createNewSolutionsStrategy;

  public DefaultRestartStrategy(RemoveSolutionsStrategy<S> removeSolutionsStrategy,
                                CreateNewSolutionsStrategy<S> createNewSolutionsStrategy) {
    this.removeSolutionsStrategy = removeSolutionsStrategy ;
    this.createNewSolutionsStrategy = createNewSolutionsStrategy ;
  }

  public void restart(List<S> solutionList, DynamicProblem<S,?> problem) {
    int numberOfRemovedSolutions = removeSolutionsStrategy.remove(solutionList, problem);
    createNewSolutionsStrategy.create(solutionList, problem, numberOfRemovedSolutions); ;
  }
}
