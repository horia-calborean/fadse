package jmetal.component.catalogue.common.solutionscreation.impl;

import java.util.ArrayList;
import java.util.List;
import jmetal.component.catalogue.common.solutionscreation.SolutionsCreation;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.solution.doublesolution.impl.DefaultDoubleSolution;
import jmetal.core.util.bounds.Bounds;
import jmetal.core.util.pseudorandom.JMetalRandom;

public class ScatterSearchSolutionsCreation implements SolutionsCreation<DoubleSolution> {
  private final int numberOfSolutionsToCreate;
  private final DoubleProblem problem;
  private final int numberOfSubRanges;
  protected int[] sumOfFrequencyValues;
  protected int[] sumOfReverseFrequencyValues;
  protected int[][] frequency;
  protected int[][] reverseFrequency;

  public ScatterSearchSolutionsCreation(
      DoubleProblem problem, int numberOfSolutionsToCreate, int numberOfSubRanges) {
    this.problem = problem;
    this.numberOfSolutionsToCreate = numberOfSolutionsToCreate;
    this.numberOfSubRanges = numberOfSubRanges;

    sumOfFrequencyValues = new int[problem.numberOfVariables()];
    sumOfReverseFrequencyValues = new int[problem.numberOfVariables()];
    frequency = new int[numberOfSubRanges][problem.numberOfVariables()];
    reverseFrequency = new int[numberOfSubRanges][problem.numberOfVariables()];
  }

  public List<DoubleSolution> create() {
    List<DoubleSolution> solutionList = new ArrayList<>(numberOfSolutionsToCreate);

    for (int i = 0; i < numberOfSolutionsToCreate; i++) {
      List<Double> variables = generateVariables();
      DoubleSolution newSolution =
          new DefaultDoubleSolution(problem.variableBounds(), problem.numberOfObjectives(), problem.numberOfConstraints());
      for (int j = 0; j < problem.numberOfVariables(); j++) {
        newSolution.variables().set(j, variables.get(j));
      }

      solutionList.add(newSolution);
    }

    return solutionList;
  }

  private List<Double> generateVariables() {
    List<Double> vars = new ArrayList<>(problem.numberOfVariables());

    double value;
    int range;

    for (int i = 0; i < problem.numberOfVariables(); i++) {
      sumOfReverseFrequencyValues[i] = 0;
      for (int j = 0; j < numberOfSubRanges; j++) {
        reverseFrequency[j][i] = sumOfFrequencyValues[i] - frequency[j][i];
        sumOfReverseFrequencyValues[i] += reverseFrequency[j][i];
      }

      if (sumOfReverseFrequencyValues[i] == 0) {
        range = JMetalRandom.getInstance().nextInt(0, numberOfSubRanges - 1);
      } else {
        value = JMetalRandom.getInstance().nextInt(0, sumOfReverseFrequencyValues[i] - 1);
        range = 0;
        while (value > reverseFrequency[range][i]) {
          value -= reverseFrequency[range][i];
          range++;
        }
      }

      frequency[range][i]++;
      sumOfFrequencyValues[i]++;

      Bounds<Double> bounds = problem.variableBounds().get(i);
      Double lowerBound = bounds.getLowerBound();
      Double upperBound = bounds.getUpperBound();
      double low = lowerBound + range * (upperBound - lowerBound) / numberOfSubRanges;
      double high = low + (upperBound - lowerBound) / numberOfSubRanges;

      vars.add(i, JMetalRandom.getInstance().nextDouble(low, high));
    }

    return vars;
  }
}
