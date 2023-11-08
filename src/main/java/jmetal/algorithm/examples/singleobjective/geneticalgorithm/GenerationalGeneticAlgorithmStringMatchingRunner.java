package jmetal.algorithm.examples.singleobjective.geneticalgorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import jmetal.core.algorithm.Algorithm;
import jmetal.algorithm.examples.AlgorithmRunner;
import jmetal.algorithm.singleobjective.geneticalgorithm.GeneticAlgorithmBuilder;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.crossover.impl.NullCrossover;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.operator.mutation.impl.CharSequenceRandomMutation;
import jmetal.core.operator.selection.SelectionOperator;
import jmetal.core.operator.selection.impl.BinaryTournamentSelection;
import jmetal.problem.singleobjective.StringMatching;
import jmetal.core.solution.sequencesolution.impl.CharSequenceSolution;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.comparator.RankingAndCrowdingDistanceComparator;
import jmetal.core.util.fileoutput.SolutionListOutput;
import jmetal.core.util.fileoutput.impl.DefaultFileOutputContext;

/**
 * Class to configure and run a generational genetic algorithm. The target problem is {@link StringMatching}.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class GenerationalGeneticAlgorithmStringMatchingRunner {
  public static void main(String[] args) {
    StringMatching problem;
    Algorithm<CharSequenceSolution> algorithm;
    CrossoverOperator<CharSequenceSolution> crossover;
    MutationOperator<CharSequenceSolution> mutation;
    SelectionOperator<List<CharSequenceSolution>, CharSequenceSolution> selection;

    problem = new StringMatching("jMetal is an optimization framework");

    crossover = new NullCrossover<>();

    double mutationProbability = 1.0 / problem.numberOfVariables();
    mutation = new CharSequenceRandomMutation(mutationProbability, problem.getAlphabet());

    selection = new BinaryTournamentSelection<>(new RankingAndCrowdingDistanceComparator<>());

    algorithm =
        new GeneticAlgorithmBuilder<>(problem, crossover, mutation)
            .setPopulationSize(50)
            .setMaxEvaluations(250000)
            .setSelectionOperator(selection)
            .build();

    AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();

    CharSequenceSolution solution = algorithm.result();
    List<CharSequenceSolution> population = new ArrayList<>(1);
    population.add(solution);

    long computingTime = algorithmRunner.getComputingTime();

    JMetalLogger.logger.info(
        "Best found string: '"
            + solution.variables().stream().map(String::valueOf).collect(Collectors.joining())
            + "'");

    new SolutionListOutput(population)
        .setVarFileOutputContext(new DefaultFileOutputContext("VAR.tsv"))
        .setFunFileOutputContext(new DefaultFileOutputContext("FUN.tsv"))
        .print();

    JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");
    JMetalLogger.logger.info("Objectives values have been written to file FUN.tsv");
    JMetalLogger.logger.info("Variables values have been written to file VAR.tsv");
  }
}
