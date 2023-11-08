package jmetal.problem.tests.multiobjective.cre;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jmetal.problem.multiobjective.cre.CRE32;
import org.junit.jupiter.api.Test;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.core.solution.doublesolution.DoubleSolution;

class CRE32Test {
  @Test
  void shouldConstructorCreateAProblemWithTheRightProperties() {
    DoubleProblem problem = new CRE32();

    assertEquals(6, problem.numberOfVariables());
    assertEquals(3, problem.numberOfObjectives());
    assertEquals(9, problem.numberOfConstraints());
    assertEquals("CRE32", problem.name());
  }

  @Test
  void shouldEvaluateWorkProperly() {
    DoubleProblem problem = new CRE32();
    DoubleSolution solution = problem.createSolution();
    problem.evaluate(solution);

    assertEquals(6, solution.variables().size());
    assertEquals(3, solution.objectives().length);
    assertEquals(9, solution.constraints().length);
  }
}
