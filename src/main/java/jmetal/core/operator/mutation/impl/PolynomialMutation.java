package jmetal.core.operator.mutation.impl;

import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.solution.doublesolution.repairsolution.RepairDoubleSolution;
import jmetal.core.solution.doublesolution.repairsolution.impl.RepairDoubleSolutionWithBoundValue;
import jmetal.core.util.bounds.Bounds;
import jmetal.core.util.errorchecking.Check;
import jmetal.core.util.errorchecking.JMetalException;
import jmetal.core.util.pseudorandom.JMetalRandom;
import jmetal.core.util.pseudorandom.RandomGenerator;

/**
 * This class implements a polynomial mutation operator
 *
 * <p>The implementation is based on the NSGA-II code available in
 * http://www.iitk.ac.in/kangal/codes.shtml
 *
 * <p>If the lower and upper bounds of a variable are the same, no mutation is carried out and the
 * bound value is returned.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 * @author Juan J. Durillo
 */
@SuppressWarnings("serial")
public class PolynomialMutation implements MutationOperator<DoubleSolution> {
  private static final double DEFAULT_PROBABILITY = 0.01;
  private static final double DEFAULT_DISTRIBUTION_INDEX = 20.0;
  private double distributionIndex;
  private double mutationProbability;
  private RepairDoubleSolution solutionRepair;

  private RandomGenerator<Double> randomGenerator;

  /** Constructor */
  public PolynomialMutation() {
    this(DEFAULT_PROBABILITY, DEFAULT_DISTRIBUTION_INDEX);
  }

  /** Constructor */
  public PolynomialMutation(
      DoubleProblem problem, double distributionIndex, RandomGenerator<Double> randomGenerator) {
    this(1.0 / problem.numberOfVariables(), distributionIndex);
    this.randomGenerator = randomGenerator;
  }

  /** Constructor */
  public PolynomialMutation(double mutationProbability, double distributionIndex) {
    this(mutationProbability, distributionIndex, new RepairDoubleSolutionWithBoundValue());
  }

  /** Constructor */
  public PolynomialMutation(
      double mutationProbability,
      double distributionIndex,
      RandomGenerator<Double> randomGenerator) {
    this(
        mutationProbability,
        distributionIndex,
        new RepairDoubleSolutionWithBoundValue(),
        randomGenerator);
  }

  /** Constructor */
  public PolynomialMutation(
      double mutationProbability, double distributionIndex, RepairDoubleSolution solutionRepair) {
    this(
        mutationProbability,
        distributionIndex,
        solutionRepair,
        () -> JMetalRandom.getInstance().nextDouble());
  }

  /** Constructor */
  public PolynomialMutation(
      double mutationProbability,
      double distributionIndex,
      RepairDoubleSolution solutionRepair,
      RandomGenerator<Double> randomGenerator) {
    Check.that(distributionIndex >= 0, "Distribution index is negative: " + distributionIndex);
    Check.probabilityIsValid(mutationProbability);
    this.mutationProbability = mutationProbability;
    this.distributionIndex = distributionIndex;
    this.solutionRepair = solutionRepair;
    this.randomGenerator = randomGenerator;
  }

  /* Getters */
  @Override
  public double mutationProbability() {
    return mutationProbability;
  }

  public double getDistributionIndex() {
    return distributionIndex;
  }

  /* Setters */
  public void setMutationProbability(double probability) {
    this.mutationProbability = probability;
  }

  public void setDistributionIndex(double distributionIndex) {
    this.distributionIndex = distributionIndex;
  }

  /** Execute() method */
  @Override
  public DoubleSolution execute(DoubleSolution solution) throws JMetalException {
    Check.notNull(solution);

    doMutation(solution);

    return solution;
  }

  /** Perform the mutation operation */
  private void doMutation(DoubleSolution solution) {

    for (int i = 0; i < solution.variables().size(); i++) {
      if (randomGenerator.getRandomValue() <= mutationProbability) {
        double y = solution.variables().get(i);
        Bounds<Double> bounds = solution.getBounds(i);
        double yl = bounds.getLowerBound();
        double yu = bounds.getUpperBound();
        if (yl == yu) {
          y = yl;
        } else {
          double delta1 = (y - yl) / (yu - yl);
          double delta2 = (yu - y) / (yu - yl);
          double rnd = randomGenerator.getRandomValue();
          double mutPow = 1.0 / (distributionIndex + 1.0);
          double deltaq;
          double val;
          double xy;
          if (rnd <= 0.5) {
            xy = 1.0 - delta1;
            val = 2.0 * rnd + (1.0 - 2.0 * rnd) * (Math.pow(xy, distributionIndex + 1.0));
            deltaq = Math.pow(val, mutPow) - 1.0;
          } else {
            xy = 1.0 - delta2;
            val = 2.0 * (1.0 - rnd) + 2.0 * (rnd - 0.5) * (Math.pow(xy, distributionIndex + 1.0));
            deltaq = 1.0 - Math.pow(val, mutPow);
          }
          y = y + deltaq * (yu - yl);
          y = solutionRepair.repairSolutionVariableValue(y, yl, yu);
        }
        solution.variables().set(i, y);
      }
    }
  }
}
