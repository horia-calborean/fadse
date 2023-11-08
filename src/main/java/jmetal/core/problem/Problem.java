package jmetal.core.problem;

import java.io.Serializable;

/**
 * Interface representing a multi-objective optimization problem. A single-objective problem is
 * a multi-objective one with an objective.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 *
 * @param <S> Encoding
 */
public interface Problem<S> extends Serializable {
  int numberOfVariables() ;
  int numberOfObjectives() ;
  int numberOfConstraints() ;
  String name() ;

  /**
   * This method receives a solution, evaluates it, and returns the evaluated solution.
   * @param solution
   * @return
   */
  S evaluate(S solution) ;
  S createSolution() ;
}
