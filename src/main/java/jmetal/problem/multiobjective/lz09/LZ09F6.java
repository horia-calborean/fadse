package jmetal.problem.multiobjective.lz09;

import java.util.ArrayList;
import java.util.List;
import jmetal.core.problem.doubleproblem.impl.AbstractDoubleProblem;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.errorchecking.JMetalException;

/**
 * Class representing problem LZ09F6
 */
@SuppressWarnings("serial")
public class LZ09F6 extends AbstractDoubleProblem {
  private LZ09 lz09;

  /**
   * Creates a default LZ09F6 problem (30 variables and 3 objectives)
   */
  public LZ09F6() {
    this(31, 1, 32);
  }

  /**
   * Creates a LZ09F6 problem instance
   */
  public LZ09F6(Integer ptype,
                Integer dtype,
                Integer ltype) throws JMetalException {
    int numberOfVariables = 10;
    numberOfObjectives(3);
    numberOfConstraints(0);
    name("LZ09F6");

    lz09 = new LZ09(numberOfVariables,
            numberOfObjectives(),
            ptype,
            dtype,
            ltype);

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
    List<Double> x = new ArrayList<Double>(numberOfVariables());
    List<Double> y = new ArrayList<Double>(solution.objectives().length);

    for (int i = 0; i < numberOfVariables(); i++) {
      x.add(solution.variables().get(i));
      y.add(0.0);
    }

    lz09.objective(x, y);

    for (int i = 0; i < solution.objectives().length; i++) {
      solution.objectives()[i] = y.get(i);
    }
    return solution ;
  }
}

