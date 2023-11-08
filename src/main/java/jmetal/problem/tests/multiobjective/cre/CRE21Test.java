package jmetal.problem.tests.multiobjective.cre;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jmetal.problem.multiobjective.cre.CRE21;
import org.junit.jupiter.api.Test;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.core.solution.doublesolution.DoubleSolution;

class CRE21Test {
  @Test
  public void shouldConstructorCreateAProblemWithTheRightProperties() {
    DoubleProblem problem = new CRE21();

    assertEquals(3, problem.numberOfVariables());
    assertEquals(2, problem.numberOfObjectives());
    assertEquals(3, problem.numberOfConstraints());
    assertEquals("CRE21", problem.name());
  }

  @Test
  public void shouldEvaluateWorkProperly() {
    DoubleProblem problem = new CRE21();
    DoubleSolution solution = problem.createSolution();
    problem.evaluate(solution);

    assertEquals(3, solution.variables().size());
    assertEquals(2, solution.objectives().length);
    assertEquals(3, solution.constraints().length);
  }
}
