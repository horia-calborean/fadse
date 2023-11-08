package jmetal.core.problem.permutationproblem;

import jmetal.core.problem.Problem;
import jmetal.core.solution.permutationsolution.PermutationSolution;

/**
 * Interface representing permutation problems
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public interface PermutationProblem<S extends PermutationSolution<?>> extends Problem<S> {
  int length() ;
}
