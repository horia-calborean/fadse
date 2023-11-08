package jmetal.problem.tests.singleobjective;

import static org.assertj.core.api.Assertions.assertThat;

import jmetal.problem.singleobjective.Griewank;
import org.junit.jupiter.api.Test;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.core.solution.doublesolution.DoubleSolution;

class GriewankTest {
  @Test
  void constructorMustCreateAValidInstance() {
    int variables = 3 ;
    DoubleProblem problem = new Griewank(3) ;

    assertThat(problem.numberOfVariables()).isEqualTo(variables) ;
    assertThat(problem.numberOfObjectives()).isEqualTo(1) ;
    assertThat(problem.numberOfConstraints()).isZero() ;
    assertThat(problem.name()).isEqualTo("Griewank") ;

    assertThat(problem.variableBounds().get(0).getLowerBound()).isEqualTo(-600) ;
    assertThat(problem.variableBounds().get(0).getUpperBound()).isEqualTo(600) ;
    assertThat(problem.variableBounds().get(problem.numberOfVariables()-1).getLowerBound()).isEqualTo(-600) ;
    assertThat(problem.variableBounds().get(problem.numberOfVariables()-1).getUpperBound()).isEqualTo(600) ;
  }

  @Test
  void createSolutionGeneratesAValidSolution() {
    DoubleProblem problem = new Griewank(20) ;
    DoubleSolution solution = problem.createSolution() ;

    assertThat(solution).isNotNull() ;
    assertThat(solution.variables()).hasSize(20) ;
    assertThat(solution.objectives()).hasSize(1) ;
    assertThat(solution.constraints()).isEmpty() ;
  }
}