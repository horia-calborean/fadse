package jmetal.algorithm.examples.singleobjective;

import java.util.List;
import jmetal.core.algorithm.Algorithm;
import jmetal.algorithm.examples.AlgorithmRunner;
import jmetal.algorithm.singleobjective.coralreefsoptimization.CoralReefsOptimizationBuilder;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.crossover.impl.SinglePointCrossover;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.operator.mutation.impl.BitFlipMutation;
import jmetal.core.operator.selection.SelectionOperator;
import jmetal.core.operator.selection.impl.BinaryTournamentSelection;
import jmetal.core.problem.binaryproblem.BinaryProblem;
import jmetal.problem.singleobjective.OneMax;
import jmetal.core.solution.binarysolution.BinarySolution;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.comparator.ObjectiveComparator;
import jmetal.core.util.fileoutput.SolutionListOutput;
import jmetal.core.util.fileoutput.impl.DefaultFileOutputContext;

/**
 * Class to configure and run a coral reefs optimization algorithm. The target
 * problem is OneMax.
 *
 * @author Inacio Medeiros <inaciogmedeiros@gmail.com>
 */
public class CoralReefsOptimizationRunner {
	/**
	 * Usage: java
	 * jmetal.runner.singleobjective.CoralReefsOptimizationRunner
	 */
	public static void main(String[] args) throws Exception {
		Algorithm<List<BinarySolution>> algorithm;
		BinaryProblem problem = new OneMax(512);

		CrossoverOperator<BinarySolution> crossoverOperator = new SinglePointCrossover(
				0.9);
		MutationOperator<BinarySolution> mutationOperator = new BitFlipMutation(
				1.0 / problem.bitsFromVariable(0));
		SelectionOperator<List<BinarySolution>, BinarySolution> selectionOperator = new BinaryTournamentSelection<BinarySolution>();

		algorithm = new CoralReefsOptimizationBuilder<BinarySolution>(problem,
				selectionOperator, crossoverOperator, mutationOperator)
				.setM(10).setN(10).setRho(0.6).setFbs(0.9).setFbr(0.1)
				.setFa(0.1).setPd(0.1).setAttemptsToSettle(3)
				.setComparator(new ObjectiveComparator<BinarySolution>(0))
				.build();

		AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(
				algorithm).execute();

		List<BinarySolution> population = algorithm.result();
		
		long computingTime = algorithmRunner.getComputingTime();

		new SolutionListOutput(population)
				.setVarFileOutputContext(
						new DefaultFileOutputContext("VAR.tsv"))
				.setFunFileOutputContext(
						new DefaultFileOutputContext("FUN.tsv")).print();

		JMetalLogger.logger.info("Total execution time: " + computingTime
				+ "ms");
		JMetalLogger.logger
				.info("Objectives values have been written to file FUN.tsv");
		JMetalLogger.logger
				.info("Variables values have been written to file VAR.tsv");

	}
}
