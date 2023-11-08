package jmetal.algorithm.multiobjective.pesa2;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import jmetal.algorithm.multiobjective.pesa2.util.AdaptiveGridArchive;
import jmetal.core.solution.integersolution.IntegerSolution;

/**
 * Created by ajnebro on 16/11/16.
 */
public class AdaptiveGridArchiveTest {
  @Test
  public void shouldConstructorCreateAnArchiveWithTheRightCapacity() {
    AdaptiveGridArchive<IntegerSolution> archive ;

    int capacity = 100 ;
    archive = new AdaptiveGridArchive<>(100, 2, 2) ;

    assertEquals(capacity, archive.maximumSize()) ;
  }

  @Test
  public void shouldConstructorCreateAnEmptyArchive() {
    AdaptiveGridArchive<IntegerSolution> archive ;

    archive = new AdaptiveGridArchive<>(100, 2, 2) ;

    assertEquals(0, archive.size()) ;
  }

  @Test
  public void shouldProneDoNothingIfTheArchiveIsEmpty() {
    AdaptiveGridArchive<IntegerSolution> archive ;

    archive = new AdaptiveGridArchive<>(4, 2, 2) ;
    archive.prune();

    assertEquals(0, archive.size()) ;
  }
}