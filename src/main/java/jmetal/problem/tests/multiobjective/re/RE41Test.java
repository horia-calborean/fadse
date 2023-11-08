package jmetal.problem.tests.multiobjective.re;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jmetal.problem.multiobjective.re.RE41;
import org.junit.jupiter.api.Test;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.core.solution.doublesolution.DoubleSolution;

class RE41Test {

  @Test
  public void shouldConstructorCreateAProblemWithTheRightProperties() {
    DoubleProblem problem = new RE41();

    assertEquals(7, problem.numberOfVariables());
    assertEquals(4, problem.numberOfObjectives());
    assertEquals(0, problem.numberOfConstraints());
    assertEquals("RE41", problem.name());
  }

  @Test
  public void shouldEvaluateWorkProperly() {
    DoubleProblem problem = new RE41();
    DoubleSolution solution = problem.createSolution();
    problem.evaluate(solution);

    assertEquals(7, solution.variables().size());
    assertEquals(4, solution.objectives().length);
    assertEquals(0, solution.constraints().length);
  }
}
