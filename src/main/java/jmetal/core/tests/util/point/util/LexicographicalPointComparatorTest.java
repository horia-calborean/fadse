package jmetal.core.tests.util.point.util;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import jmetal.core.util.errorchecking.exception.NullParameterException;
import jmetal.core.util.point.Point;
import jmetal.core.util.point.comparator.LexicographicalPointComparator;
import jmetal.core.util.point.impl.ArrayPoint;

/**
 * @author Antonio J. Nebro
 * @version 1.0
 */
public class LexicographicalPointComparatorTest {
  private Point point1 ;
  private Point point2 ;

  private LexicographicalPointComparator comparator ;

  @Before
  public void startup() {
    comparator = new LexicographicalPointComparator() ;
  }

  @Test(expected = NullParameterException.class)
  public void shouldFirstPointToCompareEqualsToNullRaiseAnException() {
    point2 = new ArrayPoint(2) ;

    comparator.compare(null, point2);
  }

  @Test (expected = NullParameterException.class)
  public void shouldSecondPointToCompareEqualsToNullRaiseAnException() {
    point1 = new ArrayPoint(2) ;

    comparator.compare(point1, null);
  }

  @Test
  public void shouldCompareIdenticalPointsReturnZero() {
    point1 = new ArrayPoint(2) ;
    point1.value(0, 1.0);
    point1.value(1, 3.0);

    point2 = new ArrayPoint(2) ;
    point2.value(0, 1.0);
    point2.value(1, 3.0);

    assertEquals(0, comparator.compare(point1, point2));
  }

  @Test
  public void shouldCompareIdenticalPointsButTheFirstValueReturnMinus1() {
    point1 = new ArrayPoint(4) ;
    point1.value(0, 1.0);
    point1.value(1, 0.0);
    point1.value(2, 5.0);
    point1.value(3, 7.0);

    point2 = new ArrayPoint(4) ;
    point2.value(0, -1.0);
    point2.value(1, 0.0);
    point2.value(2, 5.0);
    point2.value(3, 7.0);

    assertEquals(1, comparator.compare(point1, point2));
  }

  @Test
  public void shouldCompareIdenticalPointsButTheFirstValueReturnPlus1() {
    point1 = new ArrayPoint(4) ;
    point1.value(0, 1.0);
    point1.value(1, 0.0);
    point1.value(2, 5.0);
    point1.value(3, 7.0);

    point2 = new ArrayPoint(4) ;
    point2.value(0, -1.0);
    point2.value(1, 0.0);
    point2.value(2, 5.0);
    point2.value(3, 7.0);

    assertEquals(-1, comparator.compare(point2, point1));
  }

  @Test
  public void shouldCompareIdenticalPointsButTheLastValueReturnMinus1() {
    point1 = new ArrayPoint(4) ;
    point1.value(0, 1.0);
    point1.value(1, 0.0);
    point1.value(2, 5.0);
    point1.value(3, 0.0);

    point2 = new ArrayPoint(4) ;
    point2.value(0, 1.0);
    point2.value(0, 0.0);
    point2.value(0, 5.0);
    point2.value(0, 7.0);

    assertEquals(-1, comparator.compare(point1, point2));
  }

  @Test
  public void shouldCompareIdenticalPointsButTheLastValueReturnPlus1() {
    point1 = new ArrayPoint(4) ;
    point1.value(0, 1.0);
    point1.value(1, 0.0);
    point1.value(2, 5.0);
    point1.value(3, 7.0);

    point2 = new ArrayPoint(4) ;
    point2.value(0, 1.0);
    point2.value(0, 0.0);
    point2.value(0, 5.0);
    point2.value(0, 0.0);

    assertEquals(1, comparator.compare(point1, point2));
  }

  @Test
  public void shouldCompareEmptyPointsReturnZero() {
    point1 = new ArrayPoint(0) ;
    point2 = new ArrayPoint(0) ;

    assertEquals(0, comparator.compare(point1, point2));
  }

  @Test
  public void shouldCompareDifferentLengthPointsReturnTheCorrectValue() {
    point1 = new ArrayPoint(4) ;
    point1.value(0, 1.0);
    point1.value(1, 0.0);
    point1.value(2, 5.0);
    point1.value(3, 7.0);

    point2 = new ArrayPoint(3) ;
    point2.value(0, 1.0);
    point2.value(1, 0.0);
    point2.value(2, 5.0);

    assertEquals(0, comparator.compare(point1, point2));
  }
}
