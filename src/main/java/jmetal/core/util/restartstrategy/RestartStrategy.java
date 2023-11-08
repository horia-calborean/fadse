package jmetal.core.util.restartstrategy;

import java.util.List;
import jmetal.core.problem.DynamicProblem;
import jmetal.core.solution.Solution;

/**
 * Created by antonio on 6/06/17.
 */
public interface RestartStrategy<S extends Solution<?>> {
  void restart(List<S> solutionList, DynamicProblem<S,?> problem);
}
