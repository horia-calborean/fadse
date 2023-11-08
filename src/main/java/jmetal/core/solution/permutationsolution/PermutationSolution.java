package jmetal.core.solution.permutationsolution;

import jmetal.core.solution.Solution;

/**
 * Interface representing permutation based solutions
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public interface PermutationSolution<T> extends Solution<T> {
  int getLength() ;
}
