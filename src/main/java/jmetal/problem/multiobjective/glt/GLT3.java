package jmetal.problem.multiobjective.glt;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import jmetal.core.problem.doubleproblem.impl.AbstractDoubleProblem;
import jmetal.core.solution.doublesolution.DoubleSolution;

/**
 * Problem GLT3. Defined in
 * F. Gu, H.-L. Liu, and K. C. Tan, “A multiobjective evolutionary
 * algorithm using dynamic weight design method,” International Journal
 * of Innovative Computing, Information and Control, vol. 8, no. 5B, pp.
 * 3677–3688, 2012.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class GLT3 extends AbstractDoubleProblem {

  /**
   * Default constructor
   */
  public GLT3() {
    this(10) ;
  }

  /**
   * Constructor
   * @param numberOfVariables
   */
  public GLT3(int numberOfVariables) {
    numberOfObjectives(2);
    name("GLT3");

    List<Double> lowerLimit = new ArrayList<>(numberOfVariables) ;
    List<Double> upperLimit = new ArrayList<>(numberOfVariables) ;

    lowerLimit.add(0.0) ;
    upperLimit.add(1.0) ;
    IntStream.range(1, numberOfVariables).forEach(i -> {
      lowerLimit.add(-1.0);
      upperLimit.add(1.0);
    });

    variableBounds(lowerLimit, upperLimit);
  }

  @Override
  public DoubleSolution evaluate(DoubleSolution solution) {
    solution.objectives()[0] = (1.0 + g(solution))*solution.variables().get(0);
    if (solution.objectives()[0] < 0.05) {
      solution.objectives()[1] = (1.0 + g(solution))*(1.0 - 19.0*solution.variables().get(0)) ;
    } else {
      solution.objectives()[1] = (1.0 + g(solution))*(1.0/19.0 - solution.variables().get(0)/19.0) ;
    }
    return solution ;
  }

  private double g(DoubleSolution solution) {
    double result = 0.0 ;

    for (int i = 1; i < solution.variables().size(); i++) {
      double value =solution.variables().get(i)
              - Math.sin(2*Math.PI*solution.variables().get(0)+i*Math.PI/solution.variables().size()) ;

      result += value * value ;
    }

    return result ;
  }
}
