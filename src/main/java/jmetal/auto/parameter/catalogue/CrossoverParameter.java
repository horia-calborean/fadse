package jmetal.auto.parameter.catalogue;

import java.util.List;
import jmetal.auto.parameter.CategoricalParameter;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.crossover.impl.BLXAlphaCrossover;
import jmetal.core.operator.crossover.impl.HUXCrossover;
import jmetal.core.operator.crossover.impl.SBXCrossover;
import jmetal.core.operator.crossover.impl.SinglePointCrossover;
import jmetal.core.operator.crossover.impl.UniformCrossover;
import jmetal.core.operator.crossover.impl.WholeArithmeticCrossover;
import jmetal.core.solution.binarysolution.BinarySolution;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.errorchecking.JMetalException;

/**
 * Factory for crossover operators.
 */
public class CrossoverParameter extends CategoricalParameter {

  public CrossoverParameter(List<String> crossoverOperators) {
    super("crossover", crossoverOperators);
  }

  public CrossoverOperator<DoubleSolution> getDoubleSolutionParameter() {
    Double crossoverProbability = (Double) findGlobalParameter("crossoverProbability").value();
    RepairDoubleSolutionStrategyParameter repairDoubleSolution =
        (RepairDoubleSolutionStrategyParameter) findGlobalParameter("crossoverRepairStrategy");

    CrossoverOperator<DoubleSolution> result;
    switch (value()) {
      case "SBX":
        Double distributionIndex =
            (Double) findSpecificParameter("sbxDistributionIndex").value();
        result =
            new SBXCrossover(
                crossoverProbability, distributionIndex, repairDoubleSolution.getParameter());
        break;
      case "BLX_ALPHA":
        Double alpha = (Double) findSpecificParameter("blxAlphaCrossoverAlphaValue").value();
        result =
            new BLXAlphaCrossover(crossoverProbability, alpha, repairDoubleSolution.getParameter());
        break;
      case "wholeArithmetic":
        result =
            new WholeArithmeticCrossover(crossoverProbability, repairDoubleSolution.getParameter());
        break;
      default:
        throw new JMetalException("Crossover operator does not exist: " + name());
    }
    return result;
  }

  public CrossoverOperator<BinarySolution> getBinarySolutionParameter() {
    Double crossoverProbability = (Double) findGlobalParameter("crossoverProbability").value();

    CrossoverOperator<BinarySolution> result;
    switch (value()) {
      case "HUX":
        result = new HUXCrossover<>(crossoverProbability);
        break;
      case "uniform":
        result = new UniformCrossover<>(crossoverProbability);
        break;
      case "singlePoint":
        result = new SinglePointCrossover<>(crossoverProbability);
        break;
      default:
        throw new JMetalException("Crossover operator does not exist: " + name());
    }
    return result;
  }
}
