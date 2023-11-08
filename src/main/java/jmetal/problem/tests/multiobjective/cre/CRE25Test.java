package jmetal.problem.tests.multiobjective.cre;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jmetal.problem.multiobjective.cre.CRE25;
import org.junit.jupiter.api.Test;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.core.solution.doublesolution.DoubleSolution;

class CRE25Test {
  @Test
  public void shouldConstructorCreateAProblemWithTheRightProperties() {
    DoubleProblem problem = new CRE25();

    assertEquals(4, problem.numberOfVariables());
    assertEquals(2, problem.numberOfObjectives());
    assertEquals(1, problem.numberOfConstraints());
    assertEquals("CRE25", problem.name());
  }

  @Test
  public void shouldEvaluateWorkProperly() {
    DoubleProblem problem = new CRE25();
    DoubleSolution solution = problem.createSolution();
    problem.evaluate(solution);

    assertEquals(4, solution.variables().size());
    assertEquals(2, solution.objectives().length);
    assertEquals(1, solution.constraints().length);
  }
}
