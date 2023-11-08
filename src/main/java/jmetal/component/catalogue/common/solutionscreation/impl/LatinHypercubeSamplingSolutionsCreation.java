package jmetal.component.catalogue.common.solutionscreation.impl;

import java.util.ArrayList;
import java.util.List;
import jmetal.component.catalogue.common.solutionscreation.SolutionsCreation;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.solution.doublesolution.impl.DefaultDoubleSolution;
import jmetal.core.util.NormalizeUtils;
import jmetal.core.util.bounds.Bounds;

public class LatinHypercubeSamplingSolutionsCreation
    implements SolutionsCreation<DoubleSolution> {
  private final int numberOfSolutionsToCreate;
  private final DoubleProblem problem;

  public LatinHypercubeSamplingSolutionsCreation(
      DoubleProblem problem, int numberOfSolutionsToCreate) {
    this.problem = problem;
    this.numberOfSolutionsToCreate = numberOfSolutionsToCreate;
  }

  public List<DoubleSolution> create() {
    int[][] latinHypercube = new int[numberOfSolutionsToCreate][problem.numberOfVariables()];
    for (int dim = 0; dim < problem.numberOfVariables(); dim++) {
      List<Integer> permutation = getPermutation(numberOfSolutionsToCreate);
      for (int v = 0; v < numberOfSolutionsToCreate; v++) {
        latinHypercube[v][dim] = permutation.get(v);
      }
    }

    List<DoubleSolution> solutionList = new ArrayList<>(numberOfSolutionsToCreate);
    for (int i = 0; i < numberOfSolutionsToCreate; i++) {
      DoubleSolution newSolution =
          new DefaultDoubleSolution(problem.variableBounds(), problem.numberOfObjectives(), problem.numberOfConstraints());
      for (int j = 0; j < problem.numberOfVariables(); j++) {
        Bounds<Double> bounds = problem.variableBounds().get(j);
        newSolution.variables().set(
            j,
            NormalizeUtils.normalize(
                latinHypercube[i][j],
                bounds.getLowerBound(),
                bounds.getUpperBound(),
                0,
                numberOfSolutionsToCreate));
      }

      solutionList.add(newSolution);
    }

    return solutionList;
  }

  private List<Integer> getPermutation(int permutationLength) {
    List<Integer> randomSequence = new ArrayList<>(permutationLength);

    for (int j = 0; j < permutationLength; j++) {
      randomSequence.add(j);
    }

    java.util.Collections.shuffle(randomSequence);

    return randomSequence;
  }
}
