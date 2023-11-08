package jmetal.core.tests.util.point.impl;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import jmetal.core.util.point.impl.ArrayPoint;
import jmetal.core.util.point.impl.NadirPoint;
import org.junit.Test;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.core.problem.doubleproblem.impl.FakeDoubleProblem;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.point.Point;

/**
 * Created by ajnebro on 12/2/16.
 */
public class NadirPointTest {
  private static final double EPSILON = 0.00000000001 ;
  private static final double DEFAULT_INITIAL_VALUE = Double.NEGATIVE_INFINITY ;
  private NadirPoint referencePoint ;

  @Test
  public void shouldConstructorCreateANadirPointWithAllObjectiveValuesCorrectlyInitialized() {
    int numberOfObjectives = 4 ;

    referencePoint = new NadirPoint(numberOfObjectives) ;

    assertEquals(numberOfObjectives, referencePoint.dimension()) ;

    for (int i = 0 ; i < numberOfObjectives; i++) {
      assertEquals(DEFAULT_INITIAL_VALUE, referencePoint.value(i), EPSILON) ;
    }
  }

  @Test
  public void shouldUpdateWithOneSolutionMakeTheNadirPointHaveTheSolutionValues() {
    int numberOfObjectives = 2 ;

    referencePoint = new NadirPoint(numberOfObjectives) ;

    DoubleProblem problem = new FakeDoubleProblem(3, 2, 0) ;

    DoubleSolution solution = problem.createSolution() ;
    solution.objectives()[0] = 1 ;
    solution.objectives()[1] = 2 ;

    referencePoint.update(solution.objectives());
    assertEquals(1, referencePoint.value(0), EPSILON) ;
    assertEquals(2, referencePoint.value(1), EPSILON) ;
  }

  @Test
  public void shouldUpdateWithTwoSolutionsLeadToTheCorrectNadirPoint() {
    int numberOfObjectives = 2 ;

    referencePoint = new NadirPoint(numberOfObjectives) ;

    DoubleProblem problem = new FakeDoubleProblem(3, 2, 0) ;

    DoubleSolution solution1 = problem.createSolution() ;
    solution1.objectives()[0] = 0 ;
    solution1.objectives()[1] = 1 ;

    DoubleSolution solution2 = problem.createSolution() ;
    solution2.objectives()[0] = 1 ;
    solution2.objectives()[1] = 0 ;

    referencePoint.update(solution1.objectives());
    referencePoint.update(solution2.objectives());

    assertEquals(1.0, referencePoint.value(0), EPSILON) ;
    assertEquals(1.0, referencePoint.value(1), EPSILON) ;
  }

  @Test
  public void shouldUpdateWithThreeSolutionsLeadToTheCorrectNadirPoint() {
    int numberOfObjectives = 3 ;

    referencePoint = new NadirPoint(numberOfObjectives) ;

    DoubleProblem problem = new FakeDoubleProblem(3, 3, 0) ;
    DoubleSolution solution1 = problem.createSolution() ;
    solution1.objectives()[0] = 3.0 ;
    solution1.objectives()[1] = 1.0 ;
    solution1.objectives()[2] = 2.0 ;

    DoubleSolution solution2 = problem.createSolution() ;
    solution2.objectives()[0] = 0.2 ;
    solution2.objectives()[1] = 4.0 ;
    solution2.objectives()[2] = 5.5 ;

    DoubleSolution solution3 = problem.createSolution() ;
    solution3.objectives()[0] = 5.0 ;
    solution3.objectives()[1] = 6.0 ;
    solution3.objectives()[2] = 1.5 ;

    referencePoint.update(solution1.objectives());
    referencePoint.update(solution2.objectives());
    referencePoint.update(solution3.objectives());

    assertEquals(5.0, referencePoint.value(0), EPSILON) ;
    assertEquals(6.0, referencePoint.value(1), EPSILON) ;
    assertEquals(5.5, referencePoint.value(2), EPSILON) ;
  }

  @Test
  public void shouldUpdateAListOfSolutionsLeadToTheCorrectNadirPoint() {
    int numberOfObjectives = 3 ;

    referencePoint = new NadirPoint(numberOfObjectives) ;

    DoubleProblem problem = new FakeDoubleProblem(3, 3, 0) ;
    DoubleSolution solution1 = problem.createSolution() ;
    solution1.objectives()[0] = 3.0 ;
    solution1.objectives()[1] = 1.0 ;
    solution1.objectives()[2] = 2.0 ;

    DoubleSolution solution2 = problem.createSolution() ;
    solution2.objectives()[0] = 0.2 ;
    solution2.objectives()[1] = 4.0 ;
    solution2.objectives()[2] = 5.5 ;

    DoubleSolution solution3 = problem.createSolution() ;
    solution3.objectives()[0] = 5.0 ;
    solution3.objectives()[1] = 6.0 ;
    solution3.objectives()[2] = 1.5 ;

    List<DoubleSolution> solutionList = Arrays.asList(solution1, solution2, solution3) ;

    referencePoint.update(solutionList);

    assertEquals(5.0, referencePoint.value(0), EPSILON) ;
    assertEquals(6.0, referencePoint.value(1), EPSILON) ;
    assertEquals(5.5, referencePoint.value(2), EPSILON) ;
  }

  @Test
  public void shouldSetAssignTheRightValues() {
    Point point = new ArrayPoint(new double[]{2, 3, 3}) ;

    point.set(new double[]{5, 6, 7}) ;
    assertEquals(5, point.value(0), EPSILON);
    assertEquals(6, point.value(1), EPSILON);
    assertEquals(7, point.value(2), EPSILON);
  }
}