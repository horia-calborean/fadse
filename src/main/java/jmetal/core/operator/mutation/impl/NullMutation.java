package jmetal.core.operator.mutation.impl;

import jmetal.core.operator.mutation.MutationOperator;

/**
 * This class is intended to perform no mutation. It can be useful when configuring a genetic
 * algorithm and we want to use only crossover.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class NullMutation<S> implements MutationOperator<S> {

  /** Execute() method */
  @Override
  public S execute(S source) {
    return source;
  }

  @Override
  public double mutationProbability() {
    return 1.0;
  }
}
