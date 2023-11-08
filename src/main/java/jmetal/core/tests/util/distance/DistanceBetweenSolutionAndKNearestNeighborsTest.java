package jmetal.core.tests.util.distance;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.core.problem.doubleproblem.impl.FakeDoubleProblem;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.distance.impl.DistanceBetweenSolutionAndKNearestNeighbors;
import jmetal.core.util.distance.impl.EuclideanDistanceBetweenSolutionsInSolutionSpace;

public class DistanceBetweenSolutionAndKNearestNeighborsTest {
  private static final double EPSILON = 0.00000000001 ;

  @Test
  public void shouldGetDistanceReturnZeroIfTheSolutionListContainsOnlyTheSolution() {
    DoubleProblem problem = new FakeDoubleProblem(2, 2, 0) ;

    DoubleSolution solution = problem.createSolution() ;
    solution.variables().set(0, 1.0) ;
    solution.variables().set(1, 1.0) ;

    List<DoubleSolution> solutionList = new ArrayList<>() ;
    solutionList.add(solution) ;

    int k = 1  ;
    DistanceBetweenSolutionAndKNearestNeighbors<DoubleSolution> distance =
            new DistanceBetweenSolutionAndKNearestNeighbors<>(k, new EuclideanDistanceBetweenSolutionsInSolutionSpace<>()) ;

    double receivedValue = distance.compute(solution, solutionList) ;
    assertEquals(0.0, receivedValue, EPSILON) ;
  }

  @Test
  public void shouldGetDistanceWorkProperlyIfTheListContainsOnlyASolution() {
    DoubleProblem problem = new FakeDoubleProblem(2, 2, 0) ;

    DoubleSolution solution = problem.createSolution() ;
    solution.variables().set(0, 1.0) ;
    solution.variables().set(1, 1.0) ;

    DoubleSolution solution2 = problem.createSolution() ;
    solution2.variables().set(0, 2.0) ;
    solution2.variables().set(1, 2.0) ;

    List<DoubleSolution> solutionList = new ArrayList<>() ;
    solutionList.add(solution2) ;

    int k = 1 ;
    DistanceBetweenSolutionAndKNearestNeighbors<DoubleSolution> distance =
            new DistanceBetweenSolutionAndKNearestNeighbors<>(k, new EuclideanDistanceBetweenSolutionsInSolutionSpace<>()) ;
    double receivedValue = distance.compute(solution, solutionList) ;
    assertEquals(Math.sqrt(2), receivedValue, EPSILON) ;
  }

  @Test
  public void shouldGetDistanceWorkProperlyIfTheListContainsThreeSolutionsAndKIsEqualToTwo() {

    DoubleProblem problem = new FakeDoubleProblem(2, 2, 0) ;

    DoubleSolution solution = problem.createSolution() ;
    solution.variables().set(0, 1.0) ;
    solution.variables().set(1, 1.0) ;

    DoubleSolution solution2 = problem.createSolution() ;
    solution2.variables().set(0, 2.0) ;
    solution2.variables().set(1, 2.0) ;

    DoubleSolution solution3 = problem.createSolution() ;
    solution3.variables().set(0, 3.0) ;
    solution3.variables().set(1, 3.0) ;

    DoubleSolution solution4 = problem.createSolution() ;
    solution4.variables().set(0, 4.0) ;
    solution4.variables().set(1, 4.0) ;

    List<DoubleSolution> solutionList = List.of(solution, solution2, solution3, solution4) ;

    int k = 2 ;
    DistanceBetweenSolutionAndKNearestNeighbors<DoubleSolution> distance =
            new DistanceBetweenSolutionAndKNearestNeighbors<>(k, new EuclideanDistanceBetweenSolutionsInSolutionSpace<>()) ;

    double receivedValue = distance.compute(solution, solutionList) ;
    assertEquals((Math.sqrt(4+4)), receivedValue, EPSILON) ;
  }
}