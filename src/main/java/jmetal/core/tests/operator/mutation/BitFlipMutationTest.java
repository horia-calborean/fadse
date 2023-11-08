package jmetal.core.tests.operator.mutation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Random;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import jmetal.core.operator.mutation.impl.BitFlipMutation;
import jmetal.core.problem.binaryproblem.BinaryProblem;
import jmetal.core.problem.binaryproblem.impl.FakeBinaryProblem;
import jmetal.core.solution.binarysolution.BinarySolution;
import jmetal.core.solution.binarysolution.impl.DefaultBinarySolution;
import jmetal.core.util.errorchecking.exception.InvalidProbabilityValueException;
import jmetal.core.util.errorchecking.exception.NullParameterException;
import jmetal.core.util.pseudorandom.JMetalRandom;
import jmetal.core.util.pseudorandom.RandomGenerator;
import jmetal.core.util.pseudorandom.impl.AuditableRandomGenerator;

public class BitFlipMutationTest {
  private static final double EPSILON = 0.00000000000001 ;

  @Test
  public void shouldConstructorAssignTheCorrectProbabilityValue() {
    double mutationProbability = 0.1 ;
    BitFlipMutation mutation = new BitFlipMutation(mutationProbability) ;
    assertEquals(mutationProbability, (Double) ReflectionTestUtils
        .getField(mutation, "mutationProbability"), EPSILON) ;
  }

  @Test (expected = InvalidProbabilityValueException.class)
  public void shouldConstructorFailWhenPassedANegativeProbabilityValue() {
    double mutationProbability = -0.1 ;
    new BitFlipMutation(mutationProbability) ;
  }

  @Test
  public void shouldGetMutationProbabilityReturnTheRightValue() {
    double mutationProbability = 0.1 ;
    BitFlipMutation mutation = new BitFlipMutation(mutationProbability) ;
    assertEquals(mutationProbability, mutation.mutationProbability(), EPSILON) ;
  }

  @Test (expected = NullParameterException.class)
  public void shouldExecuteWithNullParameterThrowAnException() {
    BitFlipMutation mutation = new BitFlipMutation(0.1) ;

    mutation.execute(null) ;
  }

  @Test
  public void shouldMutateASingleVariableSolutionReturnTheSameSolutionIfNoBitsAreMutated() {
    @SuppressWarnings("unchecked")
	RandomGenerator<Double> randomGenerator = mock(RandomGenerator.class) ;
    double mutationProbability = 0.01;

    Mockito.when(randomGenerator.getRandomValue()).thenReturn(0.02, 0.02, 0.02, 0.02) ;

    BitFlipMutation mutation = new BitFlipMutation(mutationProbability) ;
    BinaryProblem problem = new FakeBinaryProblem(1, 4) ;
    BinarySolution solution = problem.createSolution() ;
    BinarySolution oldSolution = (BinarySolution)solution.copy() ;

    ReflectionTestUtils.setField(mutation, "randomGenerator", randomGenerator);

    mutation.execute(solution) ;

    assertEquals(oldSolution, solution) ;
    verify(randomGenerator, times(4)).getRandomValue();
  }

  @Test
  public void shouldMutateASingleVariableSolutionWhenASingleBitIsMutated() {
    @SuppressWarnings("unchecked")
	RandomGenerator<Double> randomGenerator = mock(RandomGenerator.class) ;
    double mutationProbability = 0.01;

    Mockito.when(randomGenerator.getRandomValue()).thenReturn(0.02, 0.0, 0.02, 0.02) ;

    BitFlipMutation mutation = new BitFlipMutation(mutationProbability) ;
    BinaryProblem problem = new FakeBinaryProblem(1, 4) ;
    BinarySolution solution = problem.createSolution() ;
    BinarySolution oldSolution = (BinarySolution)solution.copy() ;

    ReflectionTestUtils.setField(mutation, "randomGenerator", randomGenerator);

    mutation.execute(solution) ;

    assertNotEquals(oldSolution.variables().get(0).get(1), solution.variables().get(0).get(1)) ;
    verify(randomGenerator, times(4)).getRandomValue();
  }

  @Test
  public void shouldMutateATwoVariableSolutionReturnTheSameSolutionIfNoBitsAreMutated() {
    @SuppressWarnings("unchecked")
	RandomGenerator<Double> randomGenerator = mock(RandomGenerator.class) ;
    double mutationProbability = 0.01;

    Mockito.when(randomGenerator.getRandomValue()).thenReturn(0.02, 0.02, 0.02, 0.02, 0.2, 0.2, 0.2, 0.2) ;

    BitFlipMutation mutation = new BitFlipMutation(mutationProbability) ;
    BinaryProblem problem = new FakeBinaryProblem(2, 4) ;
    BinarySolution solution = problem.createSolution() ;
    BinarySolution oldSolution = (BinarySolution)solution.copy() ;

    ReflectionTestUtils.setField(mutation, "randomGenerator", randomGenerator);

    mutation.execute(solution) ;

    assertEquals(oldSolution, solution) ;
    verify(randomGenerator, times(8)).getRandomValue();
  }

  @Test
  public void shouldMutateATwoVariableSolutionWhenTwoBitsAreMutated() {
    @SuppressWarnings("unchecked")
	RandomGenerator<Double> randomGenerator = mock(RandomGenerator.class) ;
    double mutationProbability = 0.01;

    Mockito.when(randomGenerator.getRandomValue()).thenReturn(0.01, 0.02, 0.02, 0.02, 0.02, 0.02, 0.01, 0.02) ;

    BitFlipMutation mutation = new BitFlipMutation(mutationProbability) ;
    BinaryProblem problem = new FakeBinaryProblem(2, 4) ;
    BinarySolution solution = problem.createSolution() ;
    BinarySolution oldSolution = (BinarySolution)solution.copy() ;

    ReflectionTestUtils.setField(mutation, "randomGenerator", randomGenerator);

    mutation.execute(solution) ;

    assertNotEquals(oldSolution.variables().get(0).get(0), solution.variables().get(0).get(0)) ;
    assertNotEquals(oldSolution.variables().get(1).get(2), solution.variables().get(1).get(2)) ;
    verify(randomGenerator, times(8)).getRandomValue();
 }

  @Test
	public void shouldJMetalRandomGeneratorNotBeUsedWhenCustomRandomGeneratorProvided() {
		// Configuration
		double mutationProbability = 0.1;

		BinarySolution solution = new DefaultBinarySolution(List.of(2), 2) ;

		// Check configuration leads to use default generator by default
		final int[] defaultUses = { 0 };
		JMetalRandom defaultGenerator = JMetalRandom.getInstance();
		AuditableRandomGenerator auditor = new AuditableRandomGenerator(defaultGenerator.getRandomGenerator());
		defaultGenerator.setRandomGenerator(auditor);
		auditor.addListener((a) -> defaultUses[0]++);

		new BitFlipMutation(mutationProbability).execute(solution);
		assertTrue("No use of the default generator", defaultUses[0] > 0);

		// Test same configuration uses custom generator instead
		defaultUses[0] = 0;
		final int[] customUses = { 0 };
		new BitFlipMutation(mutationProbability, () -> {
			customUses[0]++;
			return new Random().nextDouble();
		}).execute(solution);
		assertTrue("Default random generator used", defaultUses[0] == 0);
		assertTrue("No use of the custom generator", customUses[0] > 0);
	}
}
