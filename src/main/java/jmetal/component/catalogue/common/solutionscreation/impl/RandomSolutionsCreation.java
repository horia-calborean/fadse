package jmetal.component.catalogue.common.solutionscreation.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import jmetal.component.catalogue.common.solutionscreation.SolutionsCreation;
import jmetal.core.problem.Problem;
import jmetal.core.solution.Solution;

/**
 * Class that creates a list of randomly instantiated solutions. The randomness is assumed because
 * the {@link Problem#createSolution()} in general creates solutions with random values.
 *
 * @param <S>
 */
public class RandomSolutionsCreation<S extends Solution<?>> implements SolutionsCreation<S> {
  private final int numberOfSolutionsToCreate;
  private final Problem<S> problem;

  /**
   * Creates the list of solutions
   * @param problem Problem defining the solutions
   * @param numberOfSolutionsToCreate
   */
  public RandomSolutionsCreation(Problem<S> problem, int numberOfSolutionsToCreate) {
    this.problem = problem;
    this.numberOfSolutionsToCreate = numberOfSolutionsToCreate;
  }

  public List<S> create() {
    List<S> solutionList = new ArrayList<>(numberOfSolutionsToCreate);
    IntStream.range(0, numberOfSolutionsToCreate)
        .forEach(i -> solutionList.add(problem.createSolution()));

    return solutionList;
  }
}
