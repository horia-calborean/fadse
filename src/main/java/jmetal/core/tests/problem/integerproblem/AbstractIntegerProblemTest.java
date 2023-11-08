package jmetal.core.tests.problem.integerproblem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jmetal.core.problem.integerproblem.impl.AbstractIntegerProblem;
import jmetal.core.solution.integersolution.IntegerSolution;

class AbstractIntegerProblemTest {
  private AbstractIntegerProblem problem ;

  @BeforeEach
  public void setup() {
    problem = new AbstractIntegerProblem() {
      @Override
      public IntegerSolution evaluate(IntegerSolution solution) {
        return null;
      }
    } ;
  }

  @Test
  void constructorCreatesADefaultConfiguredProblemInstance() {
    assertThat(problem.numberOfObjectives()).isZero() ;
    assertThat(problem.numberOfConstraints()).isZero() ;
    assertThatThrownBy(() -> problem.numberOfVariables()).isInstanceOf(NullPointerException.class) ;
  }

  @Test
  void setVariableBoundsWorkProperly() {
    problem.variableBounds(List.of(1,2,3), List.of(2,3,4));

    assertThat(problem.variableBounds()).hasSize(3) ;
    assertThat(problem.numberOfVariables()).isEqualTo(3) ;
  }

  @Test
  void createSolutionProducesAValidInstance() {
    problem.variableBounds(List.of(1,2,3), List.of(2,3,4));

    IntegerSolution solution = problem.createSolution() ;
    assertThat(solution.variables()).hasSize(3) ;
    assertThat(solution.objectives()).isEmpty(); ;
    assertThat(solution.constraints()).isEmpty(); ;
  }
}