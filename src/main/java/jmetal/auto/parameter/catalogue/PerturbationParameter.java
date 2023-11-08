package jmetal.auto.parameter.catalogue;

import java.util.List;
import jmetal.auto.parameter.CategoricalParameter;
import jmetal.component.catalogue.pso.perturbation.Perturbation;
import jmetal.component.catalogue.pso.perturbation.impl.FrequencySelectionMutationBasedPerturbation;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.errorchecking.JMetalException;

public class  PerturbationParameter extends CategoricalParameter {
  public PerturbationParameter(List<String> perturbationStrategies) {
    super("perturbation", perturbationStrategies);
  }

  public Perturbation getParameter() {
    Perturbation result;

    if ("frequencySelectionMutationBasedPerturbation".equals(value())) {
      MutationParameter mutationParameter = (MutationParameter) findSpecificParameter("mutation");
      MutationOperator<DoubleSolution> mutationOperator =
          mutationParameter.getDoubleSolutionParameter();

      int frequencyOfApplication = (int) findSpecificParameter(
          "frequencyOfApplicationOfMutationOperator").value();

      result =
          new FrequencySelectionMutationBasedPerturbation(mutationOperator, frequencyOfApplication);
    } else {
      throw new JMetalException("Perturbation component unknown: " + value());
    }

    return result;
  }

  @Override
  public String name() {
    return "perturbation";
  }
}
