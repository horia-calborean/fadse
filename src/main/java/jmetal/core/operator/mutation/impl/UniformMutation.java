package jmetal.core.operator.mutation.impl;

import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.solution.doublesolution.repairsolution.RepairDoubleSolution;
import jmetal.core.solution.doublesolution.repairsolution.impl.RepairDoubleSolutionWithBoundValue;
import jmetal.core.util.bounds.Bounds;
import jmetal.core.util.errorchecking.Check;
import jmetal.core.util.pseudorandom.JMetalRandom;
import jmetal.core.util.pseudorandom.RandomGenerator;

/**
 * This class implements a uniform mutation operator.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 * @author Juan J. Durillo
 */
@SuppressWarnings("serial")
public class UniformMutation implements MutationOperator<DoubleSolution> {
  private double perturbation;
  private Double mutationProbability = null;
  private RandomGenerator<Double> randomGenerator;
  private RepairDoubleSolution solutionRepair;

  /** Constructor */
  public UniformMutation(double mutationProbability, double perturbation) {
    this(
        mutationProbability,
        perturbation,
        new RepairDoubleSolutionWithBoundValue(),
        () -> JMetalRandom.getInstance().nextDouble());
  }

  /** Constructor */
  public UniformMutation(
      double mutationProbability,
      double perturbation,
      RepairDoubleSolution solutionRepair,
      RandomGenerator<Double> randomGenerator) {
    this.mutationProbability = mutationProbability;
    this.perturbation = perturbation;
    this.randomGenerator = randomGenerator;
    this.solutionRepair = solutionRepair;
  }

  /** Constructor */
  public UniformMutation(
      double mutationProbability,
      double perturbation,
      RepairDoubleSolution solutionRepair) {
    this(mutationProbability, perturbation, solutionRepair, () -> JMetalRandom.getInstance().nextDouble()) ;
  }


  /* Getters */
  public double getPerturbation() {
    return perturbation;
  }

  @Override
  public double mutationProbability() {
    return mutationProbability;
  }

  /* Setters */
  public void setPerturbation(Double perturbation) {
    this.perturbation = perturbation;
  }

  public void setMutationProbability(Double mutationProbability) {
    this.mutationProbability = mutationProbability;
  }

  /**
   * Perform the operation
   *
   * @param probability Mutation setProbability
   * @param solution The solution to mutate
   */
  public void doMutation(double probability, DoubleSolution solution) {
    for (int i = 0; i < solution.variables().size(); i++) {
      if (randomGenerator.getRandomValue() < probability) {
        double rand = randomGenerator.getRandomValue();
        double tmp = (rand - 0.5) * perturbation;

        tmp += solution.variables().get(i);

        Bounds<Double> bounds = solution.getBounds(i);
        tmp =
            solutionRepair.repairSolutionVariableValue(
                tmp, bounds.getLowerBound(), bounds.getUpperBound());

        solution.variables().set(i, tmp);
      }
    }
  }

  /** Execute() method */
  @Override
  public DoubleSolution execute(DoubleSolution solution) {
    Check.notNull(solution);

    doMutation(mutationProbability, solution);

    return solution;
  }
}
