package jmetal.problem.tests.singleobjective;

import static org.assertj.core.api.Assertions.assertThat;

import jmetal.problem.singleobjective.Sphere;
import org.junit.jupiter.api.Test;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.core.solution.doublesolution.DoubleSolution;

class SphereTest {
  @Test
  void constructorMustCreateAValidInstance() {
    int variables = 3 ;
    DoubleProblem problem = new Sphere(3) ;

    assertThat(problem.numberOfVariables()).isEqualTo(variables) ;
    assertThat(problem.numberOfObjectives()).isEqualTo(1) ;
    assertThat(problem.numberOfConstraints()).isZero() ;
    assertThat(problem.name()).isEqualTo("Sphere") ;

    assertThat(problem.variableBounds().get(0).getLowerBound()).isEqualTo(-5.12) ;
    assertThat(problem.variableBounds().get(0).getUpperBound()).isEqualTo(5.12) ;
    assertThat(problem.variableBounds().get(problem.numberOfVariables()-1).getLowerBound()).isEqualTo(-5.12) ;
    assertThat(problem.variableBounds().get(problem.numberOfVariables()-1).getUpperBound()).isEqualTo(5.12) ;
  }

  @Test
  void createSolutionGeneratesAValidSolution() {
    DoubleProblem problem = new Sphere(20) ;
    DoubleSolution solution = problem.createSolution() ;

    assertThat(solution).isNotNull() ;
    assertThat(solution.variables()).hasSize(20) ;
    assertThat(solution.objectives()).hasSize(1) ;
    assertThat(solution.constraints()).isEmpty() ;
  }
}