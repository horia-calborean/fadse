package jmetal.core.tests.operator.mutation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jmetal.core.operator.mutation.MutationOperator;
import org.junit.Test;
import jmetal.core.operator.mutation.impl.BitFlipMutation;
import jmetal.core.operator.mutation.impl.CompositeMutation;
import jmetal.core.operator.mutation.impl.PolynomialMutation;
import jmetal.core.problem.doubleproblem.impl.FakeDoubleProblem;
import jmetal.core.solution.binarysolution.impl.DefaultBinarySolution;
import jmetal.core.solution.compositesolution.CompositeSolution;
import jmetal.core.util.errorchecking.exception.EmptyCollectionException;
import jmetal.core.util.errorchecking.exception.NullParameterException;

public class CompositeMutationTest {

  @Test(expected = NullParameterException.class)
  public void shouldConstructorRaiseAnExceptionIfTheParameterListIsNull() {
    new CompositeMutation(null);
  }

  @Test(expected = EmptyCollectionException.class)
  public void shouldConstructorRaiseAnExceptionIfTheParameterListIsEmtpy() {
    new CompositeMutation(new ArrayList<>());
  }

  @Test
  public void shouldConstructorCreateAValidOperatorWhenAddingASingleMutationOperator() {
    PolynomialMutation mutation = new PolynomialMutation(0.9, 20.0);
    List<MutationOperator<?>> operatorList = new ArrayList<>();
    operatorList.add(mutation);

    CompositeMutation operator = new CompositeMutation(operatorList);
    assertNotNull(operator);
    assertEquals(1, operator.getOperators().size());
  }

  @Test
  public void shouldConstructorCreateAValidOperatorWhenAddingTwoMutationOperators() {
    PolynomialMutation polynomialMutation = new PolynomialMutation(0.9, 20.0);
    BitFlipMutation bitFlipMutation = new BitFlipMutation(0.9);

    List<MutationOperator<?>> operatorList = new ArrayList<>();
    operatorList.add(polynomialMutation);
    operatorList.add(bitFlipMutation);

    CompositeMutation operator = new CompositeMutation(operatorList);
    assertNotNull(operator);
    assertEquals(2, operator.getOperators().size());
  }

  @Test
  public void shouldExecuteWorkProperlyWithASingleMutationOperator() {
    CompositeMutation operator =
            new CompositeMutation(Arrays.asList(new PolynomialMutation(1.0, 20.0)));
    FakeDoubleProblem problem = new FakeDoubleProblem();
    CompositeSolution solution = new CompositeSolution(Arrays.asList(problem.createSolution()));

    CompositeSolution mutatedSolution = operator.execute(solution) ;

    assertNotNull(mutatedSolution);
    assertEquals(1, mutatedSolution.variables().size());
    //assertTrue(mutatedSolution.getVariable(0) instanceof PolynomialMutation);
  }

  @Test
  public void shouldExecuteWorkProperlyWithTwoMutationOperators() {
    CompositeMutation operator =
            new CompositeMutation(
                    Arrays.asList(new PolynomialMutation(1.0, 20.0), new BitFlipMutation(0.01)));

    FakeDoubleProblem doubleProblem = new FakeDoubleProblem(2, 2, 0);
    CompositeSolution solution =
            new CompositeSolution(
                    Arrays.asList(
                            doubleProblem.createSolution(),
                            new DefaultBinarySolution(Arrays.asList(20, 20), 2)));

    CompositeSolution mutatedSolution = operator.execute(solution);

    assertNotNull(mutatedSolution);
    assertEquals(2, mutatedSolution.variables().size());
  }

  @Test (expected = ClassCastException.class)
  public void shouldExecuteRaiseAnExceptionIfTheTypesOfTheSolutionsDoNotMatchTheMutationOperators() {
    CompositeMutation operator =
            new CompositeMutation(
                    Arrays.asList(new PolynomialMutation(1.0, 20.0), new BitFlipMutation(0.01)));

    FakeDoubleProblem doubleProblem = new FakeDoubleProblem(2, 2, 0);
    CompositeSolution solution =
            new CompositeSolution(
                    Arrays.asList(
                            doubleProblem.createSolution(),
                            doubleProblem.createSolution())) ;

    operator.execute(solution);
  }
}