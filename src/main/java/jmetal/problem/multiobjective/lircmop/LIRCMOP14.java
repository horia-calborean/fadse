package jmetal.problem.multiobjective.lircmop;

import jmetal.core.solution.doublesolution.DoubleSolution;

/**
 * Class representing problem LIR-CMOP13, defined in: An Improved epsilon-constrained Method in
 * MOEA/D for CMOPs with Large Infeasible Regions Fan, Z., Li, W., Cai, X. et al. Soft Comput
 * (2019). https://doi.org/10.1007/s00500-019-03794-x
 */
@SuppressWarnings("serial")
public class LIRCMOP14 extends LIRCMOP13 {
  /** Constructor */
  public LIRCMOP14() {
    this(30);
  }
  /** Constructor */
  public LIRCMOP14(int numberOfVariables) {
    super(numberOfVariables);
    numberOfConstraints(3);
    name("LIRCMOP14");
  }

  /** EvaluateConstraints() method */
  public void evaluateConstraints(DoubleSolution solution) {
    double[] constraint = new double[numberOfConstraints()];

    double f = 0;
    for (int i = 0; i < solution.objectives().length; i++) {
      f += Math.pow(solution.objectives()[i], 2);
    }
    constraint[0] = (f - 3 * 3) * (f - 2 * 2);
    constraint[1] = (f - 1.9 * 1.9) * (f - 1.8 * 1.8);
    constraint[2] = (f - 1.75 * 1.75) * (f - 1.6 * 1.6);

    solution.constraints()[0] = constraint[0];
    solution.constraints()[1] = constraint[1];
  }

  protected double g1(double[] x) {
    double result = 0.0;
    for (int i = 2; i < numberOfVariables(); i += 2) {
      result += 10 * Math.pow(x[i] - 0.5, 2.0);
    }
    return result;
  }
}
