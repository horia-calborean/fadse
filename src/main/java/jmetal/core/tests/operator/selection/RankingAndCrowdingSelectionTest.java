package jmetal.core.tests.operator.selection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import jmetal.core.operator.selection.impl.RankingAndCrowdingSelection;
import jmetal.core.solution.Solution;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.errorchecking.exception.EmptyCollectionException;
import jmetal.core.util.errorchecking.exception.NullParameterException;

/**
 * @author Antonio J. Nebro
 * @version 1.0
 */
class RankingAndCrowdingSelectionTest {

  @Test
  void shouldExecuteRaiseAnExceptionIfTheSolutionListIsNull() {
    RankingAndCrowdingSelection<Solution<?>> selection = new RankingAndCrowdingSelection<Solution<?>>(4) ;
    assertThrows(NullParameterException.class, () -> selection.execute(null)) ;
  }

  @Test
  void shouldExecuteRaiseAnExceptionIfTheSolutionListIsEmpty() {
    RankingAndCrowdingSelection<DoubleSolution> selection = new RankingAndCrowdingSelection<DoubleSolution>(4) ;
    List<DoubleSolution> list = new ArrayList<>() ;
    assertThrows(EmptyCollectionException.class, () -> selection.execute(list)) ;
  }

  @Test
  void shouldDefaultConstructorReturnASingleSolution() {
    RankingAndCrowdingSelection<Solution<?>> selection = new RankingAndCrowdingSelection<Solution<?>>(1) ;

    int result = selection.numberOfSolutionsToSelect() ;
    int expectedResult = 1 ;
    assertEquals(expectedResult, result) ;
  }

  @Test
  void shouldNonDefaultConstructorReturnTheCorrectNumberOfSolutions() {
    int solutionsToSelect = 4 ;
    RankingAndCrowdingSelection<Solution<?>> selection = new RankingAndCrowdingSelection<Solution<?>>(solutionsToSelect) ;

    int result = selection.numberOfSolutionsToSelect() ;
    assertEquals(solutionsToSelect, result) ;
  }
}
