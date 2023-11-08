package jmetal.problem.multiobjective;

import java.util.ArrayList;
import java.util.List;
import jmetal.core.problem.doubleproblem.impl.AbstractDoubleProblem;
import jmetal.core.solution.doublesolution.DoubleSolution;

/**
 * Class representing problem Kursawe
 */
@SuppressWarnings("serial")
public class Kursawe extends AbstractDoubleProblem {

  /**
   * Constructor. Creates a default instance (3 variables) of the Kursawe problem
   */
  public Kursawe() {
    this(3);
  }

  /**
   * Constructor. Creates a new instance of the Kursawe problem.
   *
   * @param numberOfVariables Number of variables of the problem
   */
  public Kursawe(Integer numberOfVariables) {
    numberOfObjectives(2);
    name("Kursawe");

    List<Double> lowerLimit = new ArrayList<>(numberOfVariables);
    List<Double> upperLimit = new ArrayList<>(numberOfVariables);

    for (int i = 0; i < numberOfVariables; i++) {
      lowerLimit.add(-5.0);
      upperLimit.add(5.0);
    }

    variableBounds(lowerLimit, upperLimit);
  }

  /**
   * Evaluate() method
   */
  public DoubleSolution evaluate(DoubleSolution solution) {
    double[] fx = new double[solution.objectives().length];
    double[] x = new double[numberOfVariables()];
    for (int i = 0; i < solution.variables().size(); i++) {
      x[i] = solution.variables().get(i);
    }

    fx[0] = 0.0;
    for (int i = 0; i < solution.variables().size() - 1; i++) {
      double xi = x[i] * x[i];
      double xj = x[i + 1] * x[i + 1];
      double aux = (-0.2) * Math.sqrt(xi + xj);
      fx[0] += (-10.0) * Math.exp(aux);
    }

    fx[1] = 0.0;
    for (int i = 0; i < solution.variables().size(); i++) {
      fx[1] += Math.pow(Math.abs(x[i]), 0.8) +
          5.0 * Math.sin(Math.pow(x[i], 3.0));
    }

    solution.objectives()[0] = fx[0];
    solution.objectives()[1] = fx[1];

    return solution;
  }
}
