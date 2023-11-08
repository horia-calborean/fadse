package jmetal.core.tests.util.legacy.front.impl;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import jmetal.core.util.legacy.front.impl.ArrayFront;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.test.util.ReflectionTestUtils;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.solution.doublesolution.impl.DefaultDoubleSolution;
import jmetal.core.solution.integersolution.IntegerSolution;
import jmetal.core.solution.integersolution.impl.DefaultIntegerSolution;
import jmetal.core.util.bounds.Bounds;
import jmetal.core.util.errorchecking.JMetalException;
import jmetal.core.util.errorchecking.exception.InvalidConditionException;
import jmetal.core.util.legacy.front.Front;
import jmetal.core.util.point.Point;
import jmetal.core.util.point.comparator.LexicographicalPointComparator;
import jmetal.core.util.point.impl.ArrayPoint;

/**
 * @author Antonio J. Nebro
 * @version 1.0
 */
public class ArrayFrontTest {
  private static final double EPSILON = 0.0000000000001;
  private static String frontDirectory ;
  private static String resourcesDirectory ;

  @BeforeAll
  public static void startup() throws IOException {
    Properties jMetalProperties = new Properties() ;
    jMetalProperties.load(new FileInputStream("../jmetal.properties"));

    resourcesDirectory = "../" + jMetalProperties.getProperty("resourcesDirectory") ;
    frontDirectory = resourcesDirectory + "/" +jMetalProperties.getProperty("referenceFrontsDirectory") ;
  }

  @Test
  public void shouldDefaultConstructorCreateAnEmptyArrayFront() {
    Front front = new ArrayFront();

    assertNull(ReflectionTestUtils.getField(front, "points"));
    assertEquals(0, ReflectionTestUtils.getField(front, "numberOfPoints"));
    assertEquals(0, ReflectionTestUtils.getField(front, "pointDimensions"));
  }

  @Test
  public void shouldCreateAnArrayFrontFromANullListRaiseAnAnException() {
    List<DoubleSolution> list = null;
    assertThrows(JMetalException.class, () -> new ArrayFront(list));
  }

  @Test
  public void shouldCreateAnArrayFrontFromAnEmptyListRaiseAnException() {
    List<DoubleSolution> list = new ArrayList<>(0);
    assertThrows(JMetalException.class, () -> new ArrayFront(list));
  }

  @Test
  public void shouldConstructorCreateAnArranFrontFromAFileContainingA2DFront()
      throws FileNotFoundException {
    Front storeFront = new ArrayFront(frontDirectory + "/ZDT1.csv");

    assertEquals(1001, storeFront.getNumberOfPoints());
    assertEquals(0.0, storeFront.getPoint(0).values()[0], 0.0001);
    assertEquals(1.0, storeFront.getPoint(0).values()[1], 0.0001);
    assertEquals(1.0, storeFront.getPoint(1000).values()[0], 0.0001);
    assertEquals(0.0, storeFront.getPoint(1000).values()[1], 0.0001);
  }

  @Test
  @Disabled
  public void shouldConstructorCreateAnArranFrontFromAFileContainingA3DFront()
      throws FileNotFoundException {
    Front storeFront = new ArrayFront(frontDirectory + "/DTLZ1.3D.csv");

    assertEquals(9901, storeFront.getNumberOfPoints());

    assertEquals(0.0, storeFront.getPoint(0).values()[0], 0.0001);
    assertEquals(0.0, storeFront.getPoint(0).values()[1], 0.0001);
    assertEquals(0.5, storeFront.getPoint(0).values()[2], 0.0001);
    assertEquals(0.49005, storeFront.getPoint(9999).values()[0], 0.0001);
    assertEquals(0.00495, storeFront.getPoint(9999).values()[1], 0.0001);
    assertEquals(0.005, storeFront.getPoint(9999).values()[2], 0.0001);
  }

  @Test
  public void shouldCreateAnArrayFrontFromAListOfSolutionsHavingOneDoubleSolutionObject() {
    int numberOfObjectives = 3;

    List<Bounds<Double>> bounds = Arrays.asList(Bounds.create(-1.0, 1.0));
    List<DoubleSolution> list =
        Arrays.asList(new DefaultDoubleSolution(bounds, numberOfObjectives, 0));
    Front front = new ArrayFront(list);

    assertNotNull(ReflectionTestUtils.getField(front, "points"));
    assertEquals(1, ReflectionTestUtils.getField(front, "numberOfPoints"));
    assertEquals(numberOfObjectives, ReflectionTestUtils.getField(front, "pointDimensions"));
  }

  @Test
  public void shouldCreateAnArrayFrontFromAListOfSolutionsHavingTwoDoubleSolutionObject() {
    int numberOfObjectives = 3;

    List<Bounds<Double>> bounds = Arrays.asList(Bounds.create(-1.0, 1.0));
    List<DoubleSolution> list =
        Arrays.asList(
            new DefaultDoubleSolution(bounds, numberOfObjectives, 0),
            new DefaultDoubleSolution(bounds, numberOfObjectives, 0));
    Front front = new ArrayFront(list);

    assertNotNull(ReflectionTestUtils.getField(front, "points"));
    assertEquals(2, ReflectionTestUtils.getField(front, "numberOfPoints"));
    assertEquals(numberOfObjectives, ReflectionTestUtils.getField(front, "pointDimensions"));
  }

  @Test
  public void shouldCreateAnArrayFrontFromAListOfSolutionsHavingOneSingleSolutionObject() {
    int numberOfObjectives = 3;

    List<Bounds<Integer>> bounds = Arrays.asList(Bounds.create(0, 1)) ;

    List<IntegerSolution> list =
        Arrays.asList(
            new DefaultIntegerSolution(bounds, numberOfObjectives, 0)) ;
    Front front = new ArrayFront(list);

    assertNotNull(ReflectionTestUtils.getField(front, "points"));
    assertEquals(1, ReflectionTestUtils.getField(front, "numberOfPoints"));
    assertEquals(numberOfObjectives, ReflectionTestUtils.getField(front, "pointDimensions"));
  }

  @Test
  public void shouldCreateAnArrayFrontFromANullFrontRaiseAnException() {
    Front front = null;
    assertThrows(JMetalException.class, () -> new ArrayFront(front));
  }

  @Test
  public void shouldCreateAnArrayFrontFromAnEmptyFrontRaiseAnException() {
    Front front = new ArrayFront(0, 0);
    assertThrows(JMetalException.class, () -> new ArrayFront(front));
  }

  @Test
  public void shouldCreateAnArrayFrontFromASolutionListResultInTwoEqualsFronts() {
    int numberOfObjectives = 3;

    List<Bounds<Integer>> bounds = Arrays.asList(Bounds.create(0, 1)) ;

    IntegerSolution solution1 =
        new DefaultIntegerSolution(bounds, numberOfObjectives, 0);
    solution1.objectives()[0] = 2;
    solution1.objectives()[1] = 235;
    solution1.objectives()[2] =-123;
    IntegerSolution solution2 =
        new DefaultIntegerSolution(bounds, numberOfObjectives, 0);
    solution2.objectives()[0] = -13234;
    solution2.objectives()[1] = 523;
    solution2.objectives()[2] = -123423455;

    List<IntegerSolution> list = Arrays.asList(solution1, solution2);

    Front front = new ArrayFront(list);

    assertNotNull(ReflectionTestUtils.getField(front, "points"));
    assertEquals(2, ReflectionTestUtils.getField(front, "numberOfPoints"));
    assertEquals(numberOfObjectives, ReflectionTestUtils.getField(front, "pointDimensions"));

    assertEquals(list.get(0).objectives()[0], front.getPoint(0).value(0), EPSILON);
    assertEquals(list.get(0).objectives()[1], front.getPoint(0).value(1), EPSILON);
    assertEquals(list.get(0).objectives()[2], front.getPoint(0).value(2), EPSILON);
    assertEquals(list.get(1).objectives()[0], front.getPoint(1).value(0), EPSILON);
    assertEquals(list.get(1).objectives()[1], front.getPoint(1).value(1), EPSILON);
    assertEquals(list.get(1).objectives()[2], front.getPoint(1).value(2), EPSILON);
  }

  @Test
  public void shouldCreateAnArrayFrontFromAnotherFrontResultInTwoEqualsFrontssss() {
    int numberOfPoints = 2;
    int pointDimensions = 2;
    Front front = new ArrayFront(numberOfPoints, pointDimensions);

    Point point1 = new ArrayPoint(pointDimensions);
    point1.value(0, 0.1323);
    point1.value(1, -30.1323);
    Point point2 = new ArrayPoint(pointDimensions);
    point2.value(0, +2342342.24232);
    point2.value(1, -23423423425.234);

    front.setPoint(0, point1);
    front.setPoint(1, point2);

    Front newFront = new ArrayFront(front);

    assertNotNull(ReflectionTestUtils.getField(newFront, "points"));
    assertEquals(numberOfPoints, ReflectionTestUtils.getField(newFront, "numberOfPoints"));
    assertEquals(pointDimensions, ReflectionTestUtils.getField(newFront, "pointDimensions"));

    assertEquals(front.getPoint(0).value(0), newFront.getPoint(0).value(0), EPSILON);
    assertEquals(front.getPoint(0).value(1), newFront.getPoint(0).value(1), EPSILON);
    assertEquals(front.getPoint(1).value(0), newFront.getPoint(1).value(0), EPSILON);
    assertEquals(front.getPoint(1).value(1), newFront.getPoint(1).value(1), EPSILON);
  }

  @Test
  public void shouldSetPointRaiseAnExceptionWhenThePointIsNull() {
    int numberOfPoints = 1;
    int numberOfPointDimensions = 2;
    Front front = new ArrayFront(numberOfPoints, numberOfPointDimensions);

    Executable executable = () -> front.setPoint(0, null);
    
    JMetalException cause = assertThrows(JMetalException.class, executable);
    assertThat(cause.getMessage(), containsString("The point is null"));
  }

  @Test
  public void shouldSetPointRaiseAnExceptionWhenTheIndexIsNegative() {
    int numberOfPoints = 1;
    int numberOfPointDimensions = 2;
    Front front = new ArrayFront(numberOfPoints, numberOfPointDimensions);

    Executable executable = () -> front.setPoint(-1, new ArrayPoint(1));
    
    JMetalException cause = assertThrows(JMetalException.class, executable);
    assertThat(cause.getMessage(), containsString("The index value is negative"));
  }

  @Test
  public void shouldSetPointRaiseAnExceptionWhenTheIndexIsGreaterThanTheFrontSize() {
    int numberOfPoints = 1;
    int numberOfPointDimensions = 2;
    Front front = new ArrayFront(numberOfPoints, numberOfPointDimensions);

    Executable executable = () -> front.setPoint(3, new ArrayPoint(1));
    
    JMetalException cause = assertThrows(JMetalException.class, executable);
    assertThat(cause.getMessage(),
            containsString("The index value (3) is greater than the number of " + "points (1)"));
  }

  @Test
  public void shouldSetPointAssignTheCorrectObject() {
    int numberOfPoints = 1;
    int numberOfPointDimensions = 2;
    Front front = new ArrayFront(numberOfPoints, numberOfPointDimensions);
    Point point = new ArrayPoint(1);
    front.setPoint(0, point);

    Point newPoint = front.getPoint(0);
    assertSame(point, newPoint);
  }

  @Test
  public void shouldGetPointRaiseAnExceptionWhenTheIndexIsNegative() {
    int numberOfPoints = 1;
    int numberOfPointDimensions = 2;
    Front front = new ArrayFront(numberOfPoints, numberOfPointDimensions);

    Executable executable = () -> front.getPoint(-1);
    
    JMetalException cause = assertThrows(JMetalException.class, executable);
    assertThat(cause.getMessage(), containsString("The index value is negative"));
  }

  @Test
  public void shouldGetPointRaiseAnExceptionWhenTheIndexIsGreaterThanTheFrontSize() {
    int numberOfPoints = 1;
    int numberOfPointDimensions = 2;
    Front front = new ArrayFront(numberOfPoints, numberOfPointDimensions);

    Executable executable = () -> front.getPoint(3);
    
    JMetalException cause = assertThrows(JMetalException.class, executable);
    assertThat(cause.getMessage(),
            containsString("The index value (3) is greater than the number of " + "points (1)"));
  }

  @Test
  public void shouldGetPointReturnTheCorrectObject() {
    int numberOfPoints = 1;
    int numberOfPointDimensions = 2;
    Front front = new ArrayFront(numberOfPoints, numberOfPointDimensions);
    Point point = new ArrayPoint(1);
    front.setPoint(0, point);

    assertSame(point, front.getPoint(0));
  }

  @Test
  public void shouldEqualsReturnTrueIfTheArgumentIsTheSameObject() {
    int numberOfPoints = 1;
    int numberOfPointDimensions = 2;
    Front front = new ArrayFront(numberOfPoints, numberOfPointDimensions);

    assertTrue(front.equals(front));
  }

  @Test
  public void shouldEqualsReturnFalseIfTheArgumentIsNull() {
    int numberOfPoints = 1;
    int numberOfPointDimensions = 2;
    Front front = new ArrayFront(numberOfPoints, numberOfPointDimensions);

    assertFalse(front.equals(null));
  }

  @SuppressWarnings("unlikely-arg-type")
  @Test
  public void shouldEqualsReturnFalseIfTheArgumentIsFromAWrongClass() {
    int numberOfPoints = 1;
    int numberOfPointDimensions = 2;
    Front front = new ArrayFront(numberOfPoints, numberOfPointDimensions);

    assertFalse(front.equals(new ArrayList<Integer>()));
  }

  @Test
  public void shouldEqualsReturnTrueIfTheArgumentIsEqual() {
    int numberOfPoints = 1;
    int pointDimensions = 2;
    Front front1 = new ArrayFront(numberOfPoints, pointDimensions);
    Front front2 = new ArrayFront(numberOfPoints, pointDimensions);

    Point point1 = new ArrayPoint(pointDimensions);
    point1.value(0, 0.1323);
    point1.value(1, -30.1323);
    Point point2 = new ArrayPoint(pointDimensions);
    point2.value(0, 0.1323);
    point2.value(1, -30.1323);

    front1.setPoint(0, point1);
    front2.setPoint(0, point2);

    assertTrue(front1.equals(front2));
  }

  @Test
  public void shouldEqualsReturnFalseIfTheComparedFrontHasADifferentNumberOfPoints() {
    int pointDimensions = 2;
    Front front1 = new ArrayFront(1, pointDimensions);
    Front front2 = new ArrayFront(2, pointDimensions);

    assertFalse(front1.equals(front2));
  }

  @Test
  public void shouldEqualsReturnFalseIfPointDimensionsOfTheFrontsIsDifferent() {
    Front front1 = new ArrayFront(1, 1);
    Front front2 = new ArrayFront(1, 2);

    assertFalse(front1.equals(front2));
  }

  @Test
  public void shouldEqualsReturnFalseIfTheFrontsAreDifferent() {
    int numberOfPoints = 1;
    int pointDimensions = 2;
    Front front1 = new ArrayFront(numberOfPoints, pointDimensions);
    Front front2 = new ArrayFront(numberOfPoints, pointDimensions);

    Point point1 = new ArrayPoint(pointDimensions);
    point1.value(0, 0.1323);
    point1.value(1, -3.1323);
    Point point2 = new ArrayPoint(pointDimensions);
    point2.value(0, 0.1323);
    point2.value(1, -30.1323);

    front1.setPoint(0, point1);
    front2.setPoint(0, point2);

    assertFalse(front1.equals(front2));
  }

  @Test
  public void shouldSortReturnAnOrderedFront() {
    int numberOfPoints = 3;
    int pointDimensions = 2;
    Front front1 = new ArrayFront(numberOfPoints, pointDimensions);

    Point point1 = new ArrayPoint(pointDimensions);
    point1.value(0, 10.0);
    point1.value(1, 12.0);
    Point point2 = new ArrayPoint(pointDimensions);
    point2.value(0, 8.0);
    point2.value(1, 80.0);
    Point point3 = new ArrayPoint(pointDimensions);
    point3.value(0, 5.0);
    point3.value(1, 50.0);

    front1.setPoint(0, point1);
    front1.setPoint(1, point2);
    front1.setPoint(2, point3);

    front1.sort(new LexicographicalPointComparator());

    assertEquals(5.0, front1.getPoint(0).value(0), EPSILON);
    assertEquals(8.0, front1.getPoint(1).value(0), EPSILON);
    assertEquals(10.0, front1.getPoint(2).value(0), EPSILON);
    assertEquals(50.0, front1.getPoint(0).value(1), EPSILON);
    assertEquals(80.0, front1.getPoint(1).value(1), EPSILON);
    assertEquals(12.0, front1.getPoint(2).value(1), EPSILON);
  }

  // TODO more test for ordering are missing

  @Test
  public void shouldCreateInputStreamThrownAnExceptionIfFileDoesNotExist()
      throws FileNotFoundException {
    String fileName = "abcdefadg";

    assertThrows(FileNotFoundException.class, () -> new ArrayFront(fileName));
  }

  @Test
  public void shouldReadFrontAnEmptyFileCreateAnEmptyFront() throws FileNotFoundException {
    String fileName = resourcesDirectory + "/unitTestsData/arrayFront/emptyFile.dat";
    Front front = new ArrayFront(fileName);

    assertEquals(0, front.getNumberOfPoints());
  }

  /** Test using a file containing: 1.0 2.0 -3.0 */
  @Test
  public void shouldReadFrontAFileWithOnePointCreateTheCorrectFront() throws FileNotFoundException {
    String fileName = resourcesDirectory + "/unitTestsData/arrayFront/fileWithOnePoint.dat";

    Front front = new ArrayFront(fileName);

    assertEquals(1, front.getNumberOfPoints());
    assertEquals(3, ReflectionTestUtils.getField(front, "pointDimensions"));
    assertEquals(1.0, front.getPoint(0).value(0), EPSILON);
    assertEquals(2.0, front.getPoint(0).value(1), EPSILON);
    assertEquals(-3.0, front.getPoint(0).value(2), EPSILON);
  }

  /** Test using a file containing: 3.0 2.3 asdfg */
  @Test
  @Disabled
  public void shouldReadFrontWithALineContainingWrongDataRaiseAnException()
      throws FileNotFoundException, JMetalException {
    String fileName = "../resources/unitTestsData/arrayFront/fileWithWrongData.dat";

    assertThrows(JMetalException.class, () -> new ArrayFront(fileName));
  }

  /** Test using a file containing: -30 234.234 90.25 15 -5.23 */
  @Test
  public void shouldReadFrontWithALineWithALineMissingDataRaiseAnException()
      throws FileNotFoundException, JMetalException {
    String fileName = resourcesDirectory + "/unitTestsData/arrayFront/fileWithMissingData.dat";

    assertThrows(InvalidConditionException.class, () -> new ArrayFront(fileName));
  }

  /** Test using a file containing: 1 2 3 4 5 6 7 8 9 10 11 12 -1 -2 -3 -4 */
  @Test
  public void shouldReadFrontFourPointsCreateTheCorrectFront()
      throws FileNotFoundException, JMetalException {
    String fileName = resourcesDirectory + "/unitTestsData/arrayFront/fileWithFourPoints.dat";

    Front front = new ArrayFront(fileName);

    assertEquals(4, front.getNumberOfPoints());
    assertEquals(4, ReflectionTestUtils.getField(front, "pointDimensions"));
    assertEquals(1, front.getPoint(0).value(0), EPSILON);
    assertEquals(6, front.getPoint(1).value(1), EPSILON);
    assertEquals(11, front.getPoint(2).value(2), EPSILON);
    assertEquals(-4, front.getPoint(3).value(3), EPSILON);
  }
}
