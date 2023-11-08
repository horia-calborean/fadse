package jmetal.problem.multiobjective.lircmop;

import jmetal.core.solution.doublesolution.DoubleSolution;

/**
 * Class representing problem LIR-CMOP3, defined in: An Improved epsilon-constrained Method in
 * MOEA/D for CMOPs with Large Infeasible Regions Fan, Z., Li, W., Cai, X. et al. Soft Comput
 * (2019). https://doi.org/10.1007/s00500-019-03794-x
 */
@SuppressWarnings("serial")
public class LIRCMOP3 extends LIRCMOP1 {

  /** Constructor */
  public LIRCMOP3() {
    super();
    numberOfConstraints(3);
    name("LIRCMOP3");
  }

  /** EvaluateConstraints() method */
  public void evaluateConstraints(DoubleSolution solution) {
    double[] x = new double[numberOfVariables()];
    for (int i = 0; i < numberOfVariables(); i++) {
      x[i] = solution.variables().get(i);
    }

    final double a = 0.51;
    final double b = 0.5;
    final double c = 20.0;

    solution.constraints()[0] = (a - g1(x)) * (g1(x) - b);
    solution.constraints()[1] = (a - g2(x)) * (g2(x) - b);
    solution.constraints()[2] = Math.sin(c * Math.PI * x[0]) - 0.5;
  }
}
