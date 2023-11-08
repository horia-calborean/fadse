package jmetal.algorithm.examples.multiobjective.nsgaii;

import java.io.IOException;
import java.util.List;
import jmetal.core.algorithm.Algorithm;
import jmetal.algorithm.examples.AlgorithmRunner;
import jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.crossover.impl.PMXCrossover;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.operator.mutation.impl.PermutationSwapMutation;
import jmetal.core.operator.selection.SelectionOperator;
import jmetal.core.operator.selection.impl.BinaryTournamentSelection;
import jmetal.problem.multiobjective.MultiobjectiveTSP;
import jmetal.core.problem.permutationproblem.PermutationProblem;
import jmetal.core.solution.permutationsolution.PermutationSolution;
import jmetal.core.util.AbstractAlgorithmRunner;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.comparator.RankingAndCrowdingDistanceComparator;
import jmetal.core.util.errorchecking.JMetalException;
import jmetal.core.util.fileoutput.SolutionListOutput;
import jmetal.core.util.fileoutput.impl.DefaultFileOutputContext;
import jmetal.core.util.pseudorandom.JMetalRandom;

/**
 * Class for configuring and running the NSGA-II algorithm to solve the bi-objective TSP
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class NSGAIITSPRunner extends AbstractAlgorithmRunner {

  /**
   * @param args Command line arguments.
   */
  public static void main(String[] args) throws JMetalException, IOException {

    PermutationProblem<PermutationSolution<Integer>> problem = new MultiobjectiveTSP(
        "resources/tspInstances/kroA100.tsp", "resources/tspInstances/kroB100.tsp");

    CrossoverOperator<PermutationSolution<Integer>> crossover = new PMXCrossover(0.9);

    double mutationProbability = 0.2;
    MutationOperator<PermutationSolution<Integer>> mutation = new PermutationSwapMutation<Integer>(
        mutationProbability);

    SelectionOperator<List<PermutationSolution<Integer>>, PermutationSolution<Integer>> selection =
        new BinaryTournamentSelection<PermutationSolution<Integer>>(
            new RankingAndCrowdingDistanceComparator<PermutationSolution<Integer>>());

    int populationSize = 100;
    Algorithm<List<PermutationSolution<Integer>>> algorithm =
        new NSGAIIBuilder<PermutationSolution<Integer>>(
            problem, crossover, mutation, populationSize)
            .setSelectionOperator(selection)
            .setMaxEvaluations(10000)
            .build();

    AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();

    List<PermutationSolution<Integer>> population = algorithm.result();
    long computingTime = algorithmRunner.getComputingTime();

    new SolutionListOutput(population)
        .setVarFileOutputContext(new DefaultFileOutputContext("VAR.tsv"))
        .setFunFileOutputContext(new DefaultFileOutputContext("FUN.tsv"))
        .print();

    JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");
    JMetalLogger.logger.info("Random seed: " + JMetalRandom.getInstance().getSeed());
    JMetalLogger.logger.info("Objectives values have been written to file FUN.tsv");
    JMetalLogger.logger.info("Variables values have been written to file VAR.tsv");
  }
}
