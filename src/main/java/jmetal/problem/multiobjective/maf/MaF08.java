package jmetal.problem.multiobjective.maf;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import jmetal.core.problem.doubleproblem.impl.AbstractDoubleProblem;
import jmetal.core.solution.doublesolution.DoubleSolution;

/**
 * Class representing problem MaF08
 */
@SuppressWarnings("serial")
public class MaF08 extends AbstractDoubleProblem {

  public double const8[][];

  /**
   * Default constructor
   */
  public MaF08() {
    this(2, 10);
  }

  /**
   * Creates a MaF03 problem instance
   *
   * @param numberOfVariables Number of variables
   * @param numberOfObjectives Number of objective functions
   */
  public MaF08(Integer numberOfVariables,
      Integer numberOfObjectives) {
    numberOfObjectives(numberOfObjectives);
    numberOfConstraints(0);
    name("MaF08");

    double r = 1;
    const8 = polygonpoints(numberOfObjectives, r);

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
    // evaluate f
    for (int i = 0; i < numberOfObjectives; i++) {
      f[i] = Math.sqrt(Math.pow(const8[i][0] - x[0], 2) + Math.pow(const8[i][1] - x[1], 2));
    }

    for (int i = 0; i < numberOfObjectives; i++) {
      solution.objectives()[i] = f[i];
    }
    return solution ;
  }

  public static double[][] polygonpoints(int m, double r) {
    double[] startp = new double[2];
    startp[0] = 0;
    startp[1] = 1;
    double[][] p1 = new double[m][2];
    double[][] p = new double[m][2];
    p1[0] = startp;
    // vertexes with the number of edges(m),start vertex(startp),radius(r)
    for (int i = 1; i < m; i++) {
      p1[i] = nextPoint(2 * Math.PI / m * i, startp, r);
    }
    for (int i = 0; i < m; i++) {
      p[i] = p1[m - i - 1];
    }
    return p;
  }

  public static double[] nextPoint(double arc, double[] startp,
      double r) {// arc is radians��evaluation the next vertex with arc and r
    double[] p = new double[2];
    p[0] = startp[0] - r * Math.sin(arc);
    p[1] = r * Math.cos(arc);
    return p;
  }
}
