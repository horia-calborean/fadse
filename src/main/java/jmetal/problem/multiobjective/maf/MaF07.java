package jmetal.problem.multiobjective.maf;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import jmetal.core.problem.doubleproblem.impl.AbstractDoubleProblem;
import jmetal.core.solution.doublesolution.DoubleSolution;

/**
 * Class representing problem MaF07
 */
@SuppressWarnings("serial")
public class MaF07 extends AbstractDoubleProblem {

  /**
   * Default constructor
   */
  public MaF07() {
    this(22, 3) ;
  }

  /**
   * Creates a MaF07 problem instance
   *
   * @param numberOfVariables Number of variables
   * @param numberOfObjectives Number of objective functions
   */
  public MaF07(Integer numberOfVariables,
      Integer numberOfObjectives) {
    numberOfObjectives(numberOfObjectives);
    numberOfConstraints(0);
    name("MaF07");

    List<Double> lower = new ArrayList<>(numberOfVariables), upper = new ArrayList<>(
        numberOfVariables);

    IntStream.range(0, numberOfVariables).forEach(i -> {
      lower.add(0.0);
      upper.add(1.0);
    });

    variableBounds(lower, upper);
  }

  /**
   * Evaluates a solution
   *
   * @param solution The solution to evaluate
   */
  @Override
  public DoubleSolution evaluate(DoubleSolution solution) {
    int numberOfVariables = solution.variables().size();
    int numberOfObjectives = solution.objectives().length;

    double[] x = new double[numberOfVariables];
    double[] f = new double[numberOfObjectives];

    for (int i = 0; i < numberOfVariables; i++) {
      x[i] = solution.variables().get(i);
    }

    // evaluate g,h
    double g = 0, h = 0, sub1;
    for (int i = numberOfObjectives - 1; i < numberOfVariables; i++) {
      g += x[i];
    }
    g = 1 + 9 * g / (numberOfVariables - numberOfObjectives + 1);
    sub1 = 1 + g;
    for (int i = 0; i < numberOfObjectives - 1; i++) {
      h += (x[i] * (1 + Math.sin(3 * Math.PI * x[i])) / sub1);
    }
    h = numberOfObjectives - h;
    // evaluate f1,...,m-1,m
    for (int i = 0; i < numberOfObjectives; i++) {
      f[i] = x[i];
    }
    f[numberOfObjectives - 1] = h * sub1;

    for (int i = 0; i < numberOfObjectives; i++) {
      solution.objectives()[i] = f[i];
    }
    return solution ;
  }
}
