package jmetal.problem.multiobjective.dtlz;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import jmetal.core.problem.doubleproblem.impl.AbstractDoubleProblem;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.errorchecking.JMetalException;

/** Class representing problem DTLZ5 */
@SuppressWarnings("serial")
public class DTLZ5 extends AbstractDoubleProblem {
  /** Creates a default DTLZ5 problem (12 variables and 3 objectives) */
  public DTLZ5() {
    this(12, 3);
  }

  /**
   * Creates a DTLZ5 problem instance
   *
   * @param numberOfVariables Number of variables
   * @param numberOfObjectives Number of objective functions
   */
  public DTLZ5(Integer numberOfVariables, Integer numberOfObjectives) throws JMetalException {
    numberOfObjectives(numberOfObjectives);
    name("DTLZ5");

    List<Double> lowerLimit = new ArrayList<>(numberOfVariables) ;
    List<Double> upperLimit = new ArrayList<>(numberOfVariables) ;

    for (int i = 0; i < numberOfVariables; i++) {
      lowerLimit.add(0.0);
      upperLimit.add(1.0);
    }

    variableBounds(lowerLimit, upperLimit);
  }

  /** Evaluate() method */
  public DoubleSolution evaluate(DoubleSolution solution) {
    int numberOfVariables = numberOfVariables();
    int numberOfObjectives = solution.objectives().length;
    double[] theta = new double[numberOfObjectives - 1];
    double g = 0.0;

    double[] f = new double[numberOfObjectives];
    double[] x = new double[numberOfVariables];

    int k = numberOfVariables() - solution.objectives().length + 1;

    for (int i = 0; i < numberOfVariables; i++) {
      x[i] = solution.variables().get(i);
    }

    for (int i = numberOfVariables - k; i < numberOfVariables; i++) {
      g += (x[i] - 0.5) * (x[i] - 0.5);
    }

    double t = java.lang.Math.PI / (4.0 * (1.0 + g));

    theta[0] = x[0] * java.lang.Math.PI / 2.0;
    for (int i = 1; i < (numberOfObjectives - 1); i++) {
      theta[i] = t * (1.0 + 2.0 * g * x[i]);
    }

    for (int i = 0; i < numberOfObjectives; i++) {
      f[i] = 1.0 + g;
    }

    for (int i = 0; i < numberOfObjectives; i++) {
      for (int j = 0; j < numberOfObjectives - (i + 1); j++) {
        f[i] *= java.lang.Math.cos(theta[j]);
      }
      if (i != 0) {
        int aux = numberOfObjectives - (i + 1);
        f[i] *= java.lang.Math.sin(theta[aux]);
      }
    }

    IntStream.range(0, numberOfObjectives).forEach(i -> solution.objectives()[i] = f[i]);

    return solution;
  }
}
