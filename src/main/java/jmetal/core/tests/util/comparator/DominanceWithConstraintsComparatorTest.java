package jmetal.core.tests.util.comparator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import jmetal.core.problem.doubleproblem.impl.FakeDoubleProblem;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.comparator.constraintcomparator.ConstraintComparator;
import jmetal.core.util.comparator.dominanceComparator.impl.DominanceWithConstraintsComparator;

/**
 Test cases
 - The solutions are feasible
 - Both solutions have the same constraint violation value
 - The first solution has a better constraint violation value
 - The second solution has a better constraint violation value
 */
class DominanceWithConstraintsComparatorTest {

  @Test
  public void compareTwoFeasibleSolutionsIgnoreTheConstraints() {
    DoubleSolution solution1 = new FakeDoubleProblem(2, 2, 1).createSolution();
    DoubleSolution solution2 = new FakeDoubleProblem(2, 2, 1).createSolution();
    solution1.objectives()[0] = 1.0;
    solution1.objectives()[1] = 5.0;
    solution1.constraints()[0] = 0 ;

    solution2.objectives()[0] = -1.0;
    solution2.objectives()[1] = 5.0;
    solution2.constraints()[0] = 0 ;

    var dominanceWithConstraintsComparator = new DominanceWithConstraintsComparator<>() ;
    assertThat(dominanceWithConstraintsComparator.compare(solution1, solution2)).isEqualTo(1) ;
  }

  @Test
  public void compareTwoSolutionsWithTheSameConstraintValuesIgnoreTheConstraints() {
    DoubleSolution solution1 = new FakeDoubleProblem(2, 2, 1).createSolution();
    DoubleSolution solution2 = new FakeDoubleProblem(2, 2, 1).createSolution();
    solution1.objectives()[0] = 1.0;
    solution1.objectives()[1] = 5.0;
    solution1.constraints()[0] = -2 ;

    solution2.objectives()[0] = -1.0;
    solution2.objectives()[1] = 5.0;
    solution2.constraints()[0] = -2 ;

    ConstraintComparator<DoubleSolution> constraintComparator = Mockito.mock(ConstraintComparator.class) ;
    Mockito.when(constraintComparator.compare(solution1, solution2)).thenReturn(0) ;

    var dominanceWithConstraintsComparator = new DominanceWithConstraintsComparator<>(constraintComparator) ;

    assertThat(dominanceWithConstraintsComparator.compare(solution1, solution2)).isEqualTo(1) ;
  }

  @Test
  public void compareReturnsMinusOneIfTheFirstSolutionHasABetterConstraintViolationValue() {
    DoubleSolution solution1 = new FakeDoubleProblem(2, 2, 1).createSolution();
    DoubleSolution solution2 = new FakeDoubleProblem(2, 2, 1).createSolution();
    solution1.objectives()[0] = 1.0;
    solution1.objectives()[1] = 1.0;
    solution1.constraints()[0] = -1 ;

    solution2.objectives()[0] = 0.0;
    solution2.objectives()[1] = 0.0;
    solution2.constraints()[0] = -2 ;

    ConstraintComparator<DoubleSolution> constraintComparator = Mockito.mock(ConstraintComparator.class) ;
    Mockito.when(constraintComparator.compare(solution1, solution2)).thenReturn(-1) ;

    var dominanceWithConstraintsComparator = new DominanceWithConstraintsComparator<>(constraintComparator) ;

    assertThat(dominanceWithConstraintsComparator.compare(solution1, solution2)).isEqualTo(-1) ;
  }

  @Test
  public void compareReturnsOneIfTheSecondSolutionHasABetterConstraintViolationValue() {
    DoubleSolution solution1 = new FakeDoubleProblem(2, 2, 1).createSolution();
    DoubleSolution solution2 = new FakeDoubleProblem(2, 2, 1).createSolution();
    solution1.objectives()[0] = 0.0;
    solution1.objectives()[1] = 0.0;
    solution1.constraints()[0] = -2 ;

    solution2.objectives()[0] = 1.0;
    solution2.objectives()[1] = 1.0;
    solution2.constraints()[0] = -1 ;

    ConstraintComparator<DoubleSolution> constraintComparator = Mockito.mock(ConstraintComparator.class) ;
    Mockito.when(constraintComparator.compare(solution1, solution2)).thenReturn(1) ;

    var dominanceWithConstraintsComparator = new DominanceWithConstraintsComparator<>(constraintComparator) ;

    assertThat(dominanceWithConstraintsComparator.compare(solution1, solution2)).isEqualTo(1) ;
  }

}