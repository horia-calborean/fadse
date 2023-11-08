package jmetal.core.problem.integerproblem;

import java.util.List;
import jmetal.core.problem.Problem;
import jmetal.core.solution.integersolution.IntegerSolution;
import jmetal.core.util.bounds.Bounds;

/**
 * Interface representing integer problems
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public interface IntegerProblem extends Problem<IntegerSolution> {
  List<Bounds<Integer>> variableBounds() ;
}
