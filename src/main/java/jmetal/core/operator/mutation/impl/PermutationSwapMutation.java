package jmetal.core.operator.mutation.impl;

import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.solution.permutationsolution.PermutationSolution;
import jmetal.core.util.errorchecking.Check;
import jmetal.core.util.pseudorandom.BoundedRandomGenerator;
import jmetal.core.util.pseudorandom.JMetalRandom;
import jmetal.core.util.pseudorandom.RandomGenerator;

/**
 * This class implements a swap mutation. The solution type of the solution must be Permutation.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 * @author Juan J. Durillo
 */
@SuppressWarnings("serial")
public class PermutationSwapMutation<T> implements MutationOperator<PermutationSolution<T>> {
  private double mutationProbability;
  private RandomGenerator<Double> mutationRandomGenerator;
  private BoundedRandomGenerator<Integer> positionRandomGenerator;

  /** Constructor */
  public PermutationSwapMutation(double mutationProbability) {
    this(
        mutationProbability,
        () -> JMetalRandom.getInstance().nextDouble(),
        (a, b) -> JMetalRandom.getInstance().nextInt(a, b));
  }

  /** Constructor */
  public PermutationSwapMutation(
      double mutationProbability, RandomGenerator<Double> randomGenerator) {
    this(
        mutationProbability,
        randomGenerator,
        BoundedRandomGenerator.fromDoubleToInteger(randomGenerator));
  }

  /** Constructor */
  public PermutationSwapMutation(
      double mutationProbability,
      RandomGenerator<Double> mutationRandomGenerator,
      BoundedRandomGenerator<Integer> positionRandomGenerator) {
    Check.probabilityIsValid(mutationProbability);
    this.mutationProbability = mutationProbability;
    this.mutationRandomGenerator = mutationRandomGenerator;
    this.positionRandomGenerator = positionRandomGenerator;
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

  /* Execute() method */
  @Override
  public PermutationSolution<T> execute(PermutationSolution<T> solution) {
    Check.notNull(solution);

    doMutation(solution);
    return solution;
  }

  /** Performs the operation */
  public void doMutation(PermutationSolution<T> solution) {
    int permutationLength;
    permutationLength = solution.variables().size();

    if ((permutationLength != 0) && (permutationLength != 1)) {
      if (mutationRandomGenerator.getRandomValue() < mutationProbability) {
        int pos1 = positionRandomGenerator.getRandomValue(0, permutationLength - 1);
        int pos2 = positionRandomGenerator.getRandomValue(0, permutationLength - 1);

        while (pos1 == pos2) {
          if (pos1 == (permutationLength - 1))
            pos2 = positionRandomGenerator.getRandomValue(0, permutationLength - 2);
          else pos2 = positionRandomGenerator.getRandomValue(pos1, permutationLength - 1);
        }

        T temp = solution.variables().get(pos1);
        solution.variables().set(pos1, solution.variables().get(pos2));
        solution.variables().set(pos2, temp);
      }
    }
  }
}
