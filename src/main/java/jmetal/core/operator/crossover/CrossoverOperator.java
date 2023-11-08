package jmetal.core.operator.crossover;

import java.util.List;
import jmetal.core.operator.Operator;

/**
 * Interface representing crossover operators. They will receive a list of solutions and return
 * another list of solutions
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 *
 * @param <Source> The class of the solutions
 */
public interface CrossoverOperator<Source> extends Operator<List<Source>,List<Source>> {
  double crossoverProbability() ;
  int numberOfRequiredParents() ;
  int numberOfGeneratedChildren() ;
}
