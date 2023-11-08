package jmetal.auto.parameter.catalogue;

import java.util.List;
import jmetal.auto.parameter.CategoricalParameter;
import jmetal.component.catalogue.common.solutionscreation.SolutionsCreation;
import jmetal.component.catalogue.common.solutionscreation.impl.LatinHypercubeSamplingSolutionsCreation;
import jmetal.component.catalogue.common.solutionscreation.impl.RandomSolutionsCreation;
import jmetal.component.catalogue.common.solutionscreation.impl.ScatterSearchSolutionsCreation;
import jmetal.core.problem.binaryproblem.BinaryProblem;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.core.solution.binarysolution.BinarySolution;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.errorchecking.JMetalException;

public class CreateInitialSolutionsParameter extends CategoricalParameter {

  public CreateInitialSolutionsParameter(List<String> validValues) {
    this("createInitialSolutions", validValues);
  }

  public CreateInitialSolutionsParameter(String parameterName,
      List<String> validValues) {
    super(parameterName, validValues);
  }

  public SolutionsCreation<? extends DoubleSolution> getParameter(DoubleProblem problem, int populationSize) {
    switch (value()) {
      case "random":
        return new RandomSolutionsCreation<>(problem, populationSize);
      case "scatterSearch":
        return new ScatterSearchSolutionsCreation(problem, populationSize, 4);
      case "latinHypercubeSampling":
        return new LatinHypercubeSamplingSolutionsCreation(problem, populationSize);
      default:
        throw new JMetalException(
            value() + " is not a valid initialization strategy");
    }
  }

  public SolutionsCreation<? extends BinarySolution> getParameter(BinaryProblem problem, int populationSize) {
    if (value().equals("random")) {
      return new RandomSolutionsCreation<>(problem, populationSize);
    }
    throw new JMetalException(
        value() + " is not a valid initialization strategy");
  }
}