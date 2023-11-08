package jmetal.core.tests.solution.util.repairsolution.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Random;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import jmetal.core.solution.doublesolution.repairsolution.RepairDoubleSolution;
import jmetal.core.solution.doublesolution.repairsolution.impl.RepairDoubleSolutionWithRandomValue;
import jmetal.core.util.errorchecking.exception.InvalidConditionException;
import jmetal.core.util.pseudorandom.JMetalRandom;
import jmetal.core.util.pseudorandom.impl.AuditableRandomGenerator;

/**
 * @author Antonio J. Nebro
 * @version 1.0
 */
public class RepairDoubleSolutionWithRandomValueTest {
  private RepairDoubleSolution repair;

  @Before public void setup() {
    repair = new RepairDoubleSolutionWithRandomValue();
  }

  @Test(expected = InvalidConditionException.class)
  public void shouldRRepairRaiseAnExceptionIfTheBoundsAreIncorrect() {
    repair.repairSolutionVariableValue(0.0, 1.0, -1.0);
  }

  @Test
  public void shouldRepairAssignARandomValueIfValueIsLessThanTheLowerBound() {
    double lowerBound = -1.0;
    double upperBound = 1.0;
    assertThat(repair.repairSolutionVariableValue(-3, lowerBound, upperBound),
        Matchers.lessThanOrEqualTo(upperBound));

    assertThat(repair.repairSolutionVariableValue(-3, lowerBound, upperBound),
        Matchers.greaterThanOrEqualTo(lowerBound));
  }

  @Test
  public void shouldRepairAssignARandomValueIfValueIsGreaterThanTheUpperBound() {
    double lowerBound = -1.0;
    double upperBound = 1.0;
    assertThat(repair.repairSolutionVariableValue(4, lowerBound, upperBound),
        Matchers.lessThanOrEqualTo(upperBound));

    assertThat(repair.repairSolutionVariableValue(4, lowerBound, upperBound),
        Matchers.greaterThanOrEqualTo(lowerBound));
  }
  
	@Test
	public void shouldJMetalRandomGeneratorNotBeUsedWhenCustomRandomGeneratorProvided() {
		// Configuration
		double lowerBound = -1.0;
		double upperBound = 1.0;
		int value = 4;

		// Check configuration leads to use default generator by default
		final int[] defaultUses = { 0 };
		JMetalRandom defaultGenerator = JMetalRandom.getInstance();
		AuditableRandomGenerator auditor = new AuditableRandomGenerator(defaultGenerator.getRandomGenerator());
		defaultGenerator.setRandomGenerator(auditor);
		auditor.addListener((a) -> defaultUses[0]++);

		new RepairDoubleSolutionWithRandomValue().repairSolutionVariableValue(value, lowerBound, upperBound);
		assertTrue("No use of the default generator", defaultUses[0] > 0);

		// Test same configuration uses custom generator instead
		defaultUses[0] = 0;
		final int[] customUses = { 0 };
		new RepairDoubleSolutionWithRandomValue((a, b) -> {
			customUses[0]++;
			return new Random().nextDouble()*(b-a)+a;
		}).repairSolutionVariableValue(value, lowerBound, upperBound);
		assertTrue("Default random generator used", defaultUses[0] == 0);
		assertTrue("No use of the custom generator", customUses[0] > 0);
	}
}
