package jmetal.core.operator.mutation.impl;

import java.util.List;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.solution.doublesolution.repairsolution.RepairDoubleSolution;
import jmetal.core.solution.doublesolution.repairsolution.impl.RepairDoubleSolutionWithBoundValue;
import jmetal.core.util.bounds.Bounds;
import jmetal.core.util.errorchecking.Check;
import jmetal.core.util.errorchecking.JMetalException;
import jmetal.core.util.grouping.CollectionGrouping;
import jmetal.core.util.pseudorandom.PseudoRandomGenerator;
import jmetal.core.util.pseudorandom.impl.JavaRandomGenerator;

/**
 * This class implements the grouped polynomial mutation operator presented in:
 * https://doi.org/10.1109/SSCI.2016.7850214
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 * @author Juan J. Durillo
 */
@SuppressWarnings("serial")
public class GroupedPolynomialMutation implements MutationOperator<DoubleSolution> {
  private static final double DEFAULT_DISTRIBUTION_INDEX = 20.0;
  private double distributionIndex;
  private RepairDoubleSolution solutionRepair;
  private CollectionGrouping<List<Double>> variableGrouping;

  private PseudoRandomGenerator randomGenerator;

  /** Constructor */
  public GroupedPolynomialMutation(CollectionGrouping<List<Double>> variableGrouping) {
    this(DEFAULT_DISTRIBUTION_INDEX, variableGrouping);
  }

  /** Constructor */
  public GroupedPolynomialMutation(
      double distributionIndex, CollectionGrouping<List<Double>> variableGrouping) {
    this(distributionIndex, new RepairDoubleSolutionWithBoundValue(), variableGrouping);
  }

  /** Constructor */
  public GroupedPolynomialMutation(
      double distributionIndex,
      RepairDoubleSolution solutionRepair,
      CollectionGrouping<List<Double>> variableGrouping) {
    this(distributionIndex, solutionRepair, new JavaRandomGenerator(), variableGrouping);
  }

  /** Constructor */
  public GroupedPolynomialMutation(
      double distributionIndex,
      RepairDoubleSolution solutionRepair,
      PseudoRandomGenerator randomGenerator,
      CollectionGrouping<List<Double>> variableGrouping) {
    Check.that(distributionIndex >= 0, "Distribution index is negative: " + distributionIndex);
    this.distributionIndex = distributionIndex;
    this.solutionRepair = solutionRepair;
    this.randomGenerator = randomGenerator;
    this.variableGrouping = variableGrouping;
  }

  /* Getters */
  @Override
  public double mutationProbability() {
    return 1.0;
  }

  public double getDistributionIndex() {
    return distributionIndex;
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
    double rnd, delta1, delta2, mutPow, deltaq;
    double y, yl, yu, val, xy;

    variableGrouping.computeGroups(solution.variables());
    int groupIndex = randomGenerator.nextInt(0, variableGrouping.numberOfGroups() - 1);
    List<Integer> variableIndex = variableGrouping.group(groupIndex);

    for (int i = 0; i < variableIndex.size(); i++) {
      y = solution.variables().get(variableIndex.get(i));
      Bounds<Double> bounds = solution.getBounds(variableIndex.get(i));
      yl = bounds.getLowerBound();
      yu = bounds.getUpperBound();
      if (yl == yu) {
        y = yl;
      } else {
        delta1 = (y - yl) / (yu - yl);
        delta2 = (yu - y) / (yu - yl);
        rnd = randomGenerator.nextDouble();
        mutPow = 1.0 / (distributionIndex + 1.0);
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
      solution.variables().set(variableIndex.get(i), y);
    }
  }
}
