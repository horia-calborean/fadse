package jmetal.algorithm.examples.multiobjective.wasfga;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import jmetal.algorithm.examples.AlgorithmRunner;
import jmetal.algorithm.multiobjective.wasfga.WASFGA;
import jmetal.core.operator.crossover.impl.PMXCrossover;
import jmetal.core.operator.mutation.impl.PermutationSwapMutation;
import jmetal.core.operator.selection.impl.BinaryTournamentSelection;
import jmetal.problem.multiobjective.MultiobjectiveTSP;
import jmetal.core.solution.permutationsolution.PermutationSolution;
import jmetal.core.util.AbstractAlgorithmRunner;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.comparator.RankingAndCrowdingDistanceComparator;
import jmetal.core.util.evaluator.impl.SequentialSolutionListEvaluator;

public class WASFGARunner extends AbstractAlgorithmRunner {

  /**
   * @param args Command line arguments.
   */
  public static void main(String[] args) throws IOException {
    var crossover = new PMXCrossover(0.9);

    double mutationProbability = 0.2;
    var mutation = new PermutationSwapMutation<Integer>(mutationProbability);

    var selection =
        new BinaryTournamentSelection<PermutationSolution<Integer>>(
            new RankingAndCrowdingDistanceComparator<PermutationSolution<Integer>>());

    var problem = new MultiobjectiveTSP("resources/tspInstances/kroA100.tsp",
        "resources/tspInstances/kroB100.tsp");

    List<Double> referencePoint = new ArrayList<>();
    referencePoint.add(0.0);
    referencePoint.add(0.0);

    double epsilon = 0.01;
    var algorithm =
        new WASFGA<>(
            problem,
            100,
            250,
            crossover,
            mutation,
            selection,
            new SequentialSolutionListEvaluator<>(),
            epsilon,
            referencePoint);

    AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();

    List<PermutationSolution<Integer>> population = algorithm.result();
    long computingTime = algorithmRunner.getComputingTime();

    JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");

    printFinalSolutionSet(population);
  }
}
