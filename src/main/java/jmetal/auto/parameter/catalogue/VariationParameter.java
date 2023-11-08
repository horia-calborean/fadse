package jmetal.auto.parameter.catalogue;

import java.util.List;
import jmetal.auto.parameter.CategoricalParameter;
import jmetal.component.catalogue.ea.variation.Variation;
import jmetal.component.catalogue.ea.variation.impl.CrossoverAndMutationVariation;
import jmetal.component.catalogue.ea.variation.impl.DifferentialEvolutionCrossoverVariation;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.solution.binarysolution.BinarySolution;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.errorchecking.Check;
import jmetal.core.util.errorchecking.JMetalException;
import jmetal.core.util.sequencegenerator.SequenceGenerator;

public class VariationParameter extends CategoricalParameter {
  public VariationParameter(List<String> variationStrategies) {
    super("variation", variationStrategies);
  }

  public Variation<? extends DoubleSolution> getDoubleSolutionParameter() {
    Variation<DoubleSolution> result;

    switch (value()) {
      case "crossoverAndMutationVariation":
        int offspringPopulationSize = (Integer)findSpecificParameter("offspringPopulationSize").value() ;
        CrossoverParameter crossoverParameter =
            (CrossoverParameter) findSpecificParameter("crossover");
        MutationParameter mutationParameter = (MutationParameter) findSpecificParameter("mutation");

        CrossoverOperator<DoubleSolution> crossoverOperator = crossoverParameter.getDoubleSolutionParameter();
        MutationOperator<DoubleSolution> mutationOperatorOperator =
            mutationParameter.getDoubleSolutionParameter();

        result =
            new CrossoverAndMutationVariation<>(
                offspringPopulationSize, crossoverOperator, mutationOperatorOperator);
        break;
      case "differentialEvolutionVariation":
        var differentialEvolutionCrossoverParameter =
            (DifferentialEvolutionCrossoverParameter)
                findSpecificParameter("differentialEvolutionCrossover");
        Check.notNull(differentialEvolutionCrossoverParameter);

        var mutationDEParameter = (MutationParameter) findSpecificParameter("mutation");
        Check.notNull(mutationDEParameter);

        var subProblemIdGenerator =
            (SequenceGenerator<Integer>) getNonConfigurableParameter("subProblemIdGenerator");
        Check.notNull(subProblemIdGenerator);

        result =
            new DifferentialEvolutionCrossoverVariation(
                1,
                differentialEvolutionCrossoverParameter.getParameter(),
                mutationDEParameter.getDoubleSolutionParameter(),
                subProblemIdGenerator);
        break;
      default:
        throw new JMetalException("Variation component unknown: " + value());
    }

    return result;
  }

  public Variation<? extends BinarySolution> getBinarySolutionParameter() {
    Variation<BinarySolution> result;
    int offspringPopulationSize = (Integer)findGlobalParameter("offspringPopulationSize").value() ;

    if ("crossoverAndMutationVariation".equals(value())) {
      CrossoverParameter crossoverParameter =
          (CrossoverParameter) findSpecificParameter("crossover");
      MutationParameter mutationParameter = (MutationParameter) findSpecificParameter("mutation");

      CrossoverOperator<BinarySolution> crossoverOperator = crossoverParameter.getBinarySolutionParameter();
      MutationOperator<BinarySolution> mutationOperatorOperator =
          mutationParameter.getBinarySolutionParameter();

      result =
          new CrossoverAndMutationVariation<>(
              offspringPopulationSize, crossoverOperator, mutationOperatorOperator);
    } else {
      throw new JMetalException("Variation component unknown: " + value());
    }

    return result;
  }

  @Override
  public String name() {
    return "variation";
  }
}
