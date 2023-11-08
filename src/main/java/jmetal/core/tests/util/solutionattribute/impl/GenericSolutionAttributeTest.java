package jmetal.core.tests.util.solutionattribute.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import jmetal.core.util.solutionattribute.impl.GenericSolutionAttribute;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import jmetal.core.problem.doubleproblem.impl.FakeDoubleProblem;
import jmetal.core.solution.doublesolution.DoubleSolution;

/** @author Antonio J. Nebro <antonio@lcc.uma.es> */
public class GenericSolutionAttributeTest {

  @Test
  public void
      shouldDefaultConstructorCreateASolutionAttributedWithAnIdentifierEqualToTheClassObject() {
    GenericSolutionAttribute<?, ?> genericSolutionAttribute;
    genericSolutionAttribute = new GenericSolutionAttribute<>();

    Object solutionAttributeId =
        ReflectionTestUtils.getField(genericSolutionAttribute, "identifier");

    assertEquals(genericSolutionAttribute.getClass(), solutionAttributeId);
  }

  @Test
  public void shouldConstructorCreateASolutionAttributedWithThePassedIdentifier() {
    GenericSolutionAttribute<?, ?> genericSolutionAttribute;
    Object attributeIdentifier = Double.valueOf(4);
    genericSolutionAttribute = new GenericSolutionAttribute<>(attributeIdentifier);

    Object solutionAttributeId =
        ReflectionTestUtils.getField(genericSolutionAttribute, "identifier");

    assertEquals(attributeIdentifier, solutionAttributeId);
  }

  @Test
  public void shouldGetAttributeIdentifierReturnTheRightIdentifier() {
    GenericSolutionAttribute<?, ?> genericSolutionAttribute;
    genericSolutionAttribute = new GenericSolutionAttribute<>();

    assertEquals(
        genericSolutionAttribute.getClass(), genericSolutionAttribute.getAttributeIdentifier());
  }

  @Test
  public void shouldGetAttributeReturnNullIfTheSolutionHasNoAttribute() {
    DoubleSolution solution = new FakeDoubleProblem(2, 2, 2).createSolution() ;

    GenericSolutionAttribute<DoubleSolution, ?> genericSolutionAttribute;
    genericSolutionAttribute = new GenericSolutionAttribute<>();

    assertNull(genericSolutionAttribute.getAttribute(solution));
  }

  @Test
  public void shouldGetAttributeReturnTheAttributeValue() {
    DoubleSolution solution = new FakeDoubleProblem(2, 2, 2).createSolution() ;

    GenericSolutionAttribute<DoubleSolution, Double> genericSolutionAttribute;
    genericSolutionAttribute = new GenericSolutionAttribute<>();

    Double value = 5.0 ;
    genericSolutionAttribute.setAttribute(solution, value);

    assertEquals(value, genericSolutionAttribute.getAttribute(solution));
  }

  @Test
  public void shouldSetAttributeAssignTheAttributeValueToTheSolution() {
    DoubleSolution solution = new FakeDoubleProblem(2, 2, 2).createSolution() ;

    GenericSolutionAttribute<DoubleSolution, Double> genericSolutionAttribute;
    genericSolutionAttribute = new GenericSolutionAttribute<>();

    Double value = 5.0;

    genericSolutionAttribute.setAttribute(solution, value);

    assertEquals(value, genericSolutionAttribute.getAttribute(solution));
  }

  @Test
  public void shouldGetAttributesReturnAnNoAttributesWhenInitiateAnPointSolution() {

    DoubleSolution solution = new FakeDoubleProblem(2, 2, 2).createSolution() ;

    assertTrue(solution.attributes().isEmpty());
  }

  @Test
  public void shouldReturnTheCorrectAttributesWhenGetAllAttributes() {
    DoubleSolution solution = new FakeDoubleProblem(2, 2, 2).createSolution() ;

    solution.attributes().put("fake-atribute-1", 1);
    solution.attributes().put("fake-atribute-2", 2);

    assertFalse(solution.attributes().isEmpty());
    assertEquals((int) solution.attributes().get("fake-atribute-1"), 1);
    assertEquals((int) solution.attributes().get("fake-atribute-2"), 2);
  }
}
