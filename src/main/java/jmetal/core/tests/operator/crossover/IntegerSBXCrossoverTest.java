package jmetal.core.tests.operator.crossover;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import org.junit.Ignore;
import org.junit.Test;
import jmetal.core.operator.crossover.impl.IntegerSBXCrossover;
import jmetal.core.solution.integersolution.IntegerSolution;
import jmetal.core.solution.integersolution.impl.DefaultIntegerSolution;
import jmetal.core.util.bounds.Bounds;
import jmetal.core.util.pseudorandom.JMetalRandom;
import jmetal.core.util.pseudorandom.impl.AuditableRandomGenerator;

public class IntegerSBXCrossoverTest {
	@Ignore
	@Test
	public void testJMetalRandomGeneratorNotUsedWhenCustomRandomGeneratorProvided() {
		// Configuration
		List<IntegerSolution> parents = new LinkedList<>();

		List<Bounds<Integer>> bounds = Arrays.asList(Bounds.create(0, 1)) ;

		parents.add(new DefaultIntegerSolution(bounds, 2, 0));
		parents.add(new DefaultIntegerSolution(bounds, 2, 0));

		// Check configuration leads to use default generator by default
		final int[] defaultUses = { 0 };
		JMetalRandom defaultGenerator = JMetalRandom.getInstance();
		AuditableRandomGenerator auditor = new AuditableRandomGenerator(defaultGenerator.getRandomGenerator());
		defaultGenerator.setRandomGenerator(auditor);
		auditor.addListener((a) -> defaultUses[0]++);

		new IntegerSBXCrossover(0.5, 0.5).execute(parents);
		assertTrue("No use of the default generator", defaultUses[0] > 0);

		// Test same configuration uses custom generator instead
		defaultUses[0] = 0;
		final int[] customUses = { 0 };
		new IntegerSBXCrossover(0.5, 0.5, () -> {
			customUses[0]++;
			return new Random().nextDouble();
		}).execute(parents);
		assertTrue("Default random generator used", defaultUses[0] == 0);
		assertTrue("No use of the custom generator", customUses[0] > 0);
	}

}
