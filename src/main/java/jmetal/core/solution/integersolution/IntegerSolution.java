package jmetal.core.solution.integersolution;

import jmetal.core.solution.Solution;
import jmetal.core.util.bounds.Bounds;

/**
 * Interface representing integer solutions, where the variables are a list of bounded integer values.
 * Each integer variable has associated a {@link Bounds<Integer>} object representing its lower and upper bounds.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public interface IntegerSolution extends Solution<Integer> {
  Bounds<Integer> getBounds(int index) ;
}
