package jmetal.core.tests.util.distance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import jmetal.core.util.distance.impl.EuclideanDistanceBetweenVectors;
import jmetal.core.util.errorchecking.exception.InvalidConditionException;
import jmetal.core.util.errorchecking.exception.NullParameterException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@DisplayName("The compute() method of class EuclideanDistanceBetweenVectors")
public class EuclideanDistanceBetweenVectorsTest {
  private static final double EPSILON = 0.0000000000001;

  private EuclideanDistanceBetweenVectors distance;

  @BeforeEach
  public void setup() {
    distance = new EuclideanDistanceBetweenVectors();
  }

  @Test
  @DisplayName("raises and exception if the first point is null")
  void shouldFirstPointToCompareEqualsToNullRaiseAnException() {
    assertThrows(NullParameterException.class, () -> distance.compute(null, new double[]{1, 2}));
  }

  @Test
  @DisplayName("raises and exception if the second point is null")
  void shouldSecondPointToCompareEqualsToNullRaiseAnException() {
    assertThrows(NullParameterException.class, () -> distance.compute(new double[]{1, 2}, null));
  }

  @Test
  @DisplayName("raises and exception if the dimension of the points are not the same")
  void shouldPassingPointsWithDifferentDimensionsRaiseAnException() {
    assertThrows(InvalidConditionException.class, () -> distance.compute(new double[]{1, 2}, new double[]{1, 2, 3}));
  }

  @Test
  @DisplayName("returns the right distance when the points have dimension zero")
  void shouldCalculatingDistanceOfPointsWithZeroDimensionReturnZero() {
    assertEquals(0, distance.compute(new double[]{}, new double[]{}), EPSILON);
  }

  @Test
  @DisplayName("returns the right distance when the points have dimension one")
  void shouldCalculatingDistanceOfPointsWithOneDimensionReturnTheCorrectValue() {
    assertEquals(4.0, distance.compute(new double[]{-2.0}, new double[]{2}), EPSILON);
  }

  /*
   * Case A: the distance between points {0.3, 0.4} and {0.2, 0.3} must be 0.02
   */
  @Test
  @DisplayName("returns the right distance when the points have dimension two")
  void shouldCalculatingDistanceOfPointsWithTwoDimensionsWorkProperly() {
    assertAll(
            () -> assertEquals(Math.sqrt(0.02), distance.compute(new double[]{0.3, 0.4}, new double[]{0.2, 0.3}), EPSILON),
            () -> assertEquals(Math.sqrt(8.0), distance.compute(new double[]{0.0, 0.0}, new double[]{2, 2}), EPSILON));
  }
}
