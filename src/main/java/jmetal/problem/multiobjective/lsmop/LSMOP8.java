package jmetal.problem.multiobjective.lsmop;

import jmetal.problem.multiobjective.lsmop.functions.Function;
import jmetal.problem.multiobjective.lsmop.functions.Griewank;
import jmetal.problem.multiobjective.lsmop.functions.Sphere;
import jmetal.core.util.errorchecking.JMetalException;

public class LSMOP8 extends AbstractLSMOP5_8 {

  /**
   * Creates a default LSMOP8 problem (7 variables and 3 objectives)
   */
  public LSMOP8() {
    this(5, 300, 3);
  }

  /**
   * Creates a LSMOP8 problem instance
   *
   * @param nk                 Number of subcomponents in each variable group
   * @param numberOfVariables  Number of variables
   * @param numberOfObjectives Number of objective functions
   */


  public LSMOP8(int nk, int numberOfVariables, int numberOfObjectives) throws JMetalException {
    super(nk, numberOfVariables, numberOfObjectives);
    name("LSMOP8");
  }

  @Override
  protected Function getOddFunction() {
    return new Griewank();
  }

  @Override
  protected Function getEvenFunction() {
    return new Sphere();
  }
}
