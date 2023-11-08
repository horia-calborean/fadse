package jmetal.auto.parameter.catalogue;

import java.util.List;
import jmetal.auto.parameter.CategoricalParameter;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.operator.mutation.impl.BitFlipMutation;
import jmetal.core.operator.mutation.impl.LinkedPolynomialMutation;
import jmetal.core.operator.mutation.impl.NonUniformMutation;
import jmetal.core.operator.mutation.impl.PolynomialMutation;
import jmetal.core.operator.mutation.impl.UniformMutation;
import jmetal.core.solution.binarysolution.BinarySolution;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.errorchecking.JMetalException;

public class MutationParameter extends CategoricalParameter {

  public MutationParameter(List<String> mutationOperators) {
    super("mutation", mutationOperators);
  }

  public MutationOperator<DoubleSolution> getDoubleSolutionParameter() {
    MutationOperator<DoubleSolution> result;
    int numberOfProblemVariables = (int) getNonConfigurableParameter("numberOfProblemVariables");
    double mutationProbability = (double) findGlobalParameter(
        "mutationProbabilityFactor").value() / numberOfProblemVariables;
    RepairDoubleSolutionStrategyParameter repairDoubleSolution =
        (RepairDoubleSolutionStrategyParameter) findGlobalParameter("mutationRepairStrategy");

    switch (value()) {
      case "polynomial":
        Double distributionIndex =
            (Double) findSpecificParameter("polynomialMutationDistributionIndex").value();
        result =
            new PolynomialMutation(
                mutationProbability, distributionIndex, repairDoubleSolution.getParameter());
        break;
      case "linkedPolynomial":
        distributionIndex =
            (Double) findSpecificParameter("linkedPolynomialMutationDistributionIndex").value();
        result =
            new LinkedPolynomialMutation(
                mutationProbability, distributionIndex, repairDoubleSolution.getParameter());
        break;
      case "uniform":
        Double perturbation = (Double) findSpecificParameter(
            "uniformMutationPerturbation").value();
        result =
            new UniformMutation(mutationProbability, perturbation,
                repairDoubleSolution.getParameter());
        break;
      case "nonUniform":
        perturbation = (Double) findSpecificParameter("nonUniformMutationPerturbation").value();
        int maxIterations = (Integer) getNonConfigurableParameter("maxIterations");
        result =
            new NonUniformMutation(mutationProbability, perturbation, maxIterations,
                repairDoubleSolution.getParameter());
        break;
      default:
        throw new JMetalException("Mutation operator does not exist: " + name());
    }
    return result;
  }

  public MutationOperator<BinarySolution> getBinarySolutionParameter() {
    MutationOperator<BinarySolution> result;
    int numberOfBitsInASolution = (int) getNonConfigurableParameter("numberOfBitsInASolution");
    double mutationProbability = (double) findGlobalParameter(
        "mutationProbabilityFactor").value() / numberOfBitsInASolution;

    if ("bitFlip".equals(value())) {
      result = new BitFlipMutation<>(mutationProbability);
    } else {
      throw new JMetalException("Mutation operator does not exist: " + name());
    }
    return result;
  }

  @Override
  public String name() {
    return "mutation";
  }
}
