package jmetal.core.problem.doubleproblem.impl;

import java.util.ArrayList;
import java.util.List;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.core.solution.doublesolution.DoubleSolution;

/**
 * Fake implementation of {@link DoubleProblem} that does nothing. Intended to be used in unit tests.
 */
@SuppressWarnings("serial")
public class FakeDoubleProblem extends AbstractDoubleProblem {
  public FakeDoubleProblem(int numberOfVariables, int numberOfObjectives,
      int numberOfConstraints) {
    numberOfObjectives(numberOfObjectives);
    numberOfConstraints(numberOfConstraints);

    List<Double> lowerLimit = new ArrayList<>(numberOfVariables);
    List<Double> upperLimit = new ArrayList<>(numberOfVariables);

    for (int i = 0; i < numberOfVariables; i++) {
      lowerLimit.add(0.0);
      upperLimit.add(1.0);
    }

    variableBounds(lowerLimit, upperLimit);
  }

  public FakeDoubleProblem() {
    this(2, 2, 0);
  }

  @Override
  public DoubleSolution evaluate(DoubleSolution solution) {
    return solution;
  }
}
