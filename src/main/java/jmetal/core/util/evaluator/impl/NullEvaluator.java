package jmetal.core.util.evaluator.impl;

import java.util.List;
import jmetal.core.problem.Problem;
import jmetal.core.util.errorchecking.JMetalException;
import jmetal.core.util.evaluator.SolutionListEvaluator;

/**
 * @author Antonio J. Nebro
 */
@SuppressWarnings("serial")
public class NullEvaluator<S> implements SolutionListEvaluator<S> {

  @Override
  public List<S> evaluate(List<S> solutionList, Problem<S> problem) throws JMetalException {
    return solutionList;
  }

  @Override
  public void shutdown() {
    // This method is an intentionally-blank override.
  }
}
