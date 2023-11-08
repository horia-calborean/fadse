package jmetal.core.problem.integerproblem.impl;

import java.util.ArrayList;
import java.util.List;
import jmetal.core.problem.integerproblem.IntegerProblem;
import jmetal.core.solution.integersolution.IntegerSolution;

/**
 * Fake implementation of {@link IntegerProblem}. Intended to be used in unit tests.
 */
@SuppressWarnings("serial")
public class FakeIntegerProblem extends AbstractIntegerProblem {

  public FakeIntegerProblem(int numberOfVariables, int numberOfObjectives,
      int numberOfConstraints) {
    numberOfObjectives(numberOfObjectives);
    numberOfConstraints(numberOfConstraints);

    List<Integer> lowerLimit = new ArrayList<>(numberOfVariables);
    List<Integer> upperLimit = new ArrayList<>(numberOfVariables);

    for (int i = 0; i < numberOfVariables; i++) {
      lowerLimit.add(0);
      upperLimit.add(10);
    }

    variableBounds(lowerLimit, upperLimit);
  }

  public FakeIntegerProblem() {
    this(2, 2, 0);
  }

  @Override
  public IntegerSolution evaluate(IntegerSolution solution) {
    return solution;
  }
}
