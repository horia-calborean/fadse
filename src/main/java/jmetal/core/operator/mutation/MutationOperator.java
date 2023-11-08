package jmetal.core.operator.mutation;

import jmetal.core.operator.Operator;

/**
 * Interface representing mutation operators
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 *
 * @param <Source> The solution class of the solution to be mutated
 */
public interface MutationOperator<Source> extends Operator<Source, Source> {
    Source execute(Source solution);
    double mutationProbability() ;
}
