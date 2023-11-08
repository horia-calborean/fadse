package jmetal.core.solution.doublesolution;

import jmetal.core.solution.Solution;
import jmetal.core.util.bounds.Bounds;

/**
 * Interface representing double solutions, where the variables are a list of bounded double values.
 * Each double variable has associated a {@link Bounds<Double>} object representing its lower and upper bounds.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public interface DoubleSolution extends Solution<Double> {
  Bounds<Double> getBounds(int index) ;
}
