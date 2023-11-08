package jmetal.problem.multiobjective;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jmetal.core.problem.Problem;
import jmetal.core.solution.compositesolution.CompositeSolution;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.solution.doublesolution.impl.DefaultDoubleSolution;
import jmetal.core.solution.integersolution.IntegerSolution;
import jmetal.core.solution.integersolution.impl.DefaultIntegerSolution;
import jmetal.core.util.bounds.Bounds;

/**
 * Bi-objective problem for testing class {@link CompositeSolution )}. This problem requires an encoding where a
 * solution is composed of an integer solution and a double solution. For the sake of simplicity, it is assumed that
 * the lower and upper bounds of the variables of both solutions are the same.
 *
 * Objective 1: minimizing the sum of the distances of every variable to value N
 * Objective 2: minimizing the sum of the distances of every variable to value M
 */
@SuppressWarnings("serial")
public class MixedIntegerDoubleProblem implements Problem<CompositeSolution> {
  private int valueN;
  private int valueM;
  private List<Bounds<Integer>> integerBounds;
  private List<Bounds<Double>> doubleBounds;

  public MixedIntegerDoubleProblem() {
    this(10, 10, 100, -100, -1000, +1000);
  }

  /** Constructor */
  public MixedIntegerDoubleProblem(
      int numberOfIntegerVariables,
      int numberOfDoubleVariables,
      int n,
      int m,
      int lowerBound,
      int upperBound) {
    valueN = n;
    valueM = m;

    integerBounds = new ArrayList<>(numberOfIntegerVariables);
    doubleBounds = new ArrayList<>(numberOfDoubleVariables);

    for (int i = 0; i < numberOfIntegerVariables; i++) {
      integerBounds.add(Bounds.create(lowerBound, upperBound));
    }

    for (int i = 0; i < numberOfDoubleVariables; i++) {
      doubleBounds.add(Bounds.create((double) lowerBound, (double) upperBound));
    }
  }

  @Override
  public int numberOfVariables() {
    return 2 ;
  }

  @Override
  public int numberOfObjectives() {
    return 2 ;
  }

  @Override
  public int numberOfConstraints() {
    return 0;
  }

  @Override
  public String name() {
    return "MixedIntegerDoubleProblem";
  }

  /** Evaluate() method */
  @Override
  public CompositeSolution evaluate(CompositeSolution solution) {
    int approximationToN;
    int approximationToM;

    approximationToN = 0;
    approximationToM = 0;

    List<Integer> integerVariables = ((IntegerSolution) solution.variables().get(0)).variables();
    for (Integer integerVariable : integerVariables) {
      approximationToN += Math.abs(valueN - integerVariable);
      approximationToM += Math.abs(valueM - integerVariable);
    }

    List<Double> doubleVariables = ((DoubleSolution) solution.variables().get(1)).variables();
    for (Double doubleVariable : doubleVariables) {
      approximationToN += Math.abs(valueN - doubleVariable);
      approximationToM += Math.abs(valueM - doubleVariable);
    }

    solution.objectives()[0] = approximationToN ;
    solution.objectives()[1] = approximationToM ;

    return solution ;
  }

  @Override
  public CompositeSolution createSolution() {
    IntegerSolution integerSolution = new DefaultIntegerSolution(integerBounds, numberOfObjectives(), numberOfConstraints()) ;
    DoubleSolution doubleSolution = new DefaultDoubleSolution(doubleBounds, numberOfObjectives(), numberOfConstraints()) ;
    return new CompositeSolution(Arrays.asList(integerSolution, doubleSolution));
  }
}
