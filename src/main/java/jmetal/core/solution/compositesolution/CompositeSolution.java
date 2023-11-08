package jmetal.core.solution.compositesolution;

import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;
import jmetal.core.solution.AbstractSolution;
import jmetal.core.solution.Solution;
import jmetal.core.util.errorchecking.Check;

/**
 * Class representing solutions composed of a list of solutions. The idea is that each decision
 * variable can be a solution of any type, so we can create mixed solutions (e.g., solutions
 * combining any of the existing encodings).
 *
 * The adopted approach has the advantage of easing the reuse of existing variation operators,
 * but all the solutions in the list will need to have the same function and constraint violation
 * values.
 *
 * It is assumed that problems using instances of this class will properly manage the solutions it contains.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class CompositeSolution extends AbstractSolution<Solution<?>> {
  /**
   * Constructor.
   * @param solutions Collection of solutions composing the composite solution. All of them have to have the same
   *                  number of objectives and constraints.
   */
  public CompositeSolution(List<Solution<?>> solutions) {
    super(solutions.size(), solutions.get(0).objectives().length, solutions.get(0).constraints().length);
    Check.notNull(solutions);
    Check.collectionIsNotEmpty(solutions);
    int numberOfObjectives = solutions.get(0).objectives().length;
    int numberOfConstraints = solutions.get(0).constraints().length;
    for (Solution<?> solution : solutions) {
      Check.that(
          solution.objectives().length == numberOfObjectives,
          "The solutions in the list must have the same number of objectives: "
              + numberOfObjectives);
      Check.that(
              solution.constraints().length == numberOfConstraints,
              "The solutions in the list must have the same number of constraints: "
                      + numberOfConstraints);
    }

    for (int i = 0 ; i < solutions.size(); i++) {
      variables().set(i, solutions.get(i)) ;
    }
  }

  /**
   * Copy constructor
   * @param solution
   */
  public CompositeSolution(CompositeSolution solution) {
    super(solution.variables().size(), solution.objectives().length, solution.constraints().length) ;

    IntStream.range(0, solution.variables().size()).forEach(i -> variables().set(i, solution.variables().get(i).copy()));
    IntStream.range(0, solution.objectives().length).forEach(i -> objectives()[i] = solution.objectives()[i]);
    IntStream.range(0, solution.constraints().length).forEach(i -> constraints()[i] = solution.constraints()[i]);

    attributes = new HashMap<>(solution.attributes) ;
  }

  @Override
  public Solution<Solution<?>> copy() {
    return new CompositeSolution(this);
  }
}
