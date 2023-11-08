package jmetal.problem.multiobjective;

import java.util.ArrayList;
import java.util.List;
import jmetal.core.problem.doubleproblem.impl.AbstractDoubleProblem;
import jmetal.core.solution.doublesolution.DoubleSolution;

/** Class representing problem Schaffer */
public class Schaffer extends AbstractDoubleProblem {
  private static final long serialVersionUID = -2366503015218789989L;

  /** Constructor. Creates a default instance of problem Schaffer */
  public Schaffer() {
    int numberOfVariables = 1;
    numberOfObjectives(2);
    name("Schaffer");

    List<Double> lowerLimit = new ArrayList<>(numberOfVariables);
    List<Double> upperLimit = new ArrayList<>(numberOfVariables);

    for (int i = 0; i < numberOfVariables; i++) {
      lowerLimit.add(-100000.0);
      upperLimit.add(100000.0);
    }

    variableBounds(lowerLimit, upperLimit);
  }

  /** Evaluate() method */
  public DoubleSolution evaluate(DoubleSolution solution) {
    double[] f = new double[solution.objectives().length];
    double value = solution.variables().get(0);

    f[0] = value * value;
    f[1] = (value - 2.0) * (value - 2.0);

    solution.objectives()[0] = f[0];
    solution.objectives()[1] = f[1];

    return solution;
  }
}
