package jmetal.problem.tests.multiobjective.re;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jmetal.problem.multiobjective.re.RE34;
import org.junit.jupiter.api.Test;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.core.solution.doublesolution.DoubleSolution;

class RE34Test {

  @Test
  public void shouldConstructorCreateAProblemWithTheRightProperties() {
    DoubleProblem problem = new RE34();

    assertEquals(5, problem.numberOfVariables());
    assertEquals(3, problem.numberOfObjectives());
    assertEquals(0, problem.numberOfConstraints());
    assertEquals("RE34", problem.name());
  }

  @Test
  public void shouldEvaluateWorkProperly() {
    DoubleProblem problem = new RE34();
    DoubleSolution solution = problem.createSolution();
    problem.evaluate(solution);

    assertEquals(5, solution.variables().size());
    assertEquals(3, solution.objectives().length);
    assertEquals(0, solution.constraints().length);
  }
}
