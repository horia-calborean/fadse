package jmetal.core.tests.operator.selection;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import jmetal.core.operator.selection.impl.BinaryTournamentSelection;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.core.problem.doubleproblem.impl.FakeDoubleProblem;
import jmetal.core.solution.Solution;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.errorchecking.exception.EmptyCollectionException;
import jmetal.core.util.errorchecking.exception.InvalidConditionException;
import jmetal.core.util.errorchecking.exception.NullParameterException;

/**
 * @author Antonio J. Nebro
 * @version 1.0
 */
public class BinaryTournamentSelectionTest {

  @Test
  public void shouldExecuteRaiseAnExceptionIfTheListOfSolutionsIsNull() {
    BinaryTournamentSelection<Solution<Object>> selection =
        new BinaryTournamentSelection<Solution<Object>>();
    assertThrows(NullParameterException.class, () -> selection.execute(null));
  }

  @Test
  public void shouldExecuteRaiseAnExceptionIfTheListOfSolutionsIsEmpty() {
    BinaryTournamentSelection<Solution<Object>> selection =
        new BinaryTournamentSelection<Solution<Object>>();
    assertThrows(EmptyCollectionException.class, () -> selection.execute(new ArrayList<>(0)));
  }

  @Test
  public void shouldExecuteReturnAValidSolutionIsWithCorrectParameters() {
    @SuppressWarnings("unchecked")
    DoubleProblem problem = new FakeDoubleProblem(2, 2, 0);

    var solution1 = problem.createSolution();
    var solution2 = problem.createSolution();
    var solution3 = problem.createSolution();

    List<DoubleSolution> population = List.of(solution1, solution2, solution3);

    BinaryTournamentSelection<DoubleSolution> selection = new BinaryTournamentSelection<>();
    assertNotNull(selection.execute(population));
  }

  @Test
  public void shouldExecuteRaiseAnExceptionIfTheListContainsOneSolution() {
    @SuppressWarnings("unchecked")
    Solution<Object> solution = mock(Solution.class);

    assertThrows(
        InvalidConditionException.class,
        () -> new BinaryTournamentSelection<Solution<Object>>().execute(List.of(solution)));
  }

  @Test
  public void shouldExecuteWorkProperlyIfTheTwoSolutionsInTheListAreNondominated() {
    Comparator<DoubleSolution> comparator = mock(Comparator.class);

    DoubleSolution solution1 = mock(DoubleSolution.class);
    DoubleSolution solution2 = mock(DoubleSolution.class);

    List<DoubleSolution> population = Arrays.<DoubleSolution>asList(solution1, solution2);

    BinaryTournamentSelection<DoubleSolution> selection =
        new BinaryTournamentSelection<DoubleSolution>(comparator);
    DoubleSolution result = selection.execute(population);

    assertThat(result, Matchers.either(Matchers.is(solution1)).or(Matchers.is(solution2)));
    verify(comparator).compare(any(DoubleSolution.class), any(DoubleSolution.class));
  }
}
