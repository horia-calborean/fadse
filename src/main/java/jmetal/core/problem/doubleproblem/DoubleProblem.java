package jmetal.core.problem.doubleproblem;

import java.util.List;
import jmetal.core.problem.Problem;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.bounds.Bounds;

/**
 * Interface representing continuous problems
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public interface DoubleProblem extends Problem<DoubleSolution> {
  List<Bounds<Double>> variableBounds() ;
}
