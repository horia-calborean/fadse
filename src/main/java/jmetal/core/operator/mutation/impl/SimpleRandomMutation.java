package jmetal.core.operator.mutation.impl;

import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.bounds.Bounds;
import jmetal.core.util.errorchecking.JMetalException;
import jmetal.core.util.pseudorandom.JMetalRandom;
import jmetal.core.util.pseudorandom.RandomGenerator;

/**
 * This class implements a random mutation operator for double solutions
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class SimpleRandomMutation implements MutationOperator<DoubleSolution> {
  private double mutationProbability;
  private RandomGenerator<Double> randomGenerator;

  /** Constructor */
  public SimpleRandomMutation(double probability) {
    this(probability, () -> JMetalRandom.getInstance().nextDouble());
  }

  /** Constructor */
  public SimpleRandomMutation(double probability, RandomGenerator<Double> randomGenerator) {
    if (probability < 0) {
      throw new JMetalException("Mutation probability is negative: " + mutationProbability);
    }

    this.mutationProbability = probability;
    this.randomGenerator = randomGenerator;
  }

  /* Getters */
  @Override
  public double mutationProbability() {
    return mutationProbability;
  }

  /* Setters */
  public void setMutationProbability(double mutationProbability) {
    this.mutationProbability = mutationProbability;
  }

  /** Execute() method */
  @Override
  public DoubleSolution execute(DoubleSolution solution) throws JMetalException {
    if (null == solution) {
      throw new JMetalException("Null parameter");
    }

    doMutation(mutationProbability, solution);

    return solution;
  }

  /** Implements the mutation operation */
  private void doMutation(double probability, DoubleSolution solution) {
    for (int i = 0; i < solution.variables().size(); i++) {
      if (randomGenerator.getRandomValue() <= probability) {
        Bounds<Double> bounds = solution.getBounds(i);
        Double lowerBound = bounds.getLowerBound();
        Double upperBound = bounds.getUpperBound();
        Double randomValue = randomGenerator.getRandomValue();
        Double value = lowerBound + ((upperBound - lowerBound) * randomValue);

        solution.variables().set(i, value);
      }
    }
  }
}
