package jmetal.problem.tests.multiobjective.re;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jmetal.problem.multiobjective.re.RE42;
import org.junit.jupiter.api.Test;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.core.solution.doublesolution.DoubleSolution;

class RE42Test {

  @Test
  public void shouldConstructorCreateAProblemWithTheRightProperties() {
    DoubleProblem problem = new RE42();

    assertEquals(6, problem.numberOfVariables());
    assertEquals(4, problem.numberOfObjectives());
    assertEquals(0, problem.numberOfConstraints());
    assertEquals("RE42", problem.name());
  }

  @Test
  public void shouldEvaluateWorkProperly() {
    DoubleProblem problem = new RE42();
    DoubleSolution solution = problem.createSolution();
    problem.evaluate(solution);

    assertEquals(6, solution.variables().size());
    assertEquals(4, solution.objectives().length);
    assertEquals(0, solution.constraints().length);
  }
}
