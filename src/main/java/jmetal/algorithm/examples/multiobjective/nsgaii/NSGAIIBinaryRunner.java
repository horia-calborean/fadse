package jmetal.algorithm.examples.multiobjective.nsgaii;

import java.util.List;
import jmetal.core.algorithm.Algorithm;
import jmetal.algorithm.examples.AlgorithmRunner;
import jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.crossover.impl.SinglePointCrossover;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.operator.mutation.impl.BitFlipMutation;
import jmetal.core.operator.selection.SelectionOperator;
import jmetal.core.operator.selection.impl.BinaryTournamentSelection;
import jmetal.problem.ProblemFactory;
import jmetal.core.problem.binaryproblem.BinaryProblem;
import jmetal.core.solution.binarysolution.BinarySolution;
import jmetal.core.util.AbstractAlgorithmRunner;
import jmetal.core.util.JMetalLogger;

/**
 * Class for configuring and running the NSGA-II algorithm (binary encoding)
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */

public class NSGAIIBinaryRunner extends AbstractAlgorithmRunner {

  /**
   * @param args Command line arguments.
   */
  public static void main(String[] args) {

    String problemName = "jmetal.core.problem.multiobjective.zdt.ZDT5";

    BinaryProblem problem = (BinaryProblem) ProblemFactory.<BinarySolution>loadProblem(problemName);

    double crossoverProbability = 0.9;
    CrossoverOperator<BinarySolution> crossover = new SinglePointCrossover(crossoverProbability);

    double mutationProbability = 1.0 / problem.bitsFromVariable(0);
    MutationOperator<BinarySolution> mutation = new BitFlipMutation(mutationProbability);

    SelectionOperator<List<BinarySolution>, BinarySolution> selection = new BinaryTournamentSelection<>();

    int populationSize = 100;
    Algorithm<List<BinarySolution>> algorithm = new NSGAIIBuilder<>(problem, crossover, mutation,
        populationSize)
        .setSelectionOperator(selection)
        .setMaxEvaluations(25000)
        .build();

    AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm)
        .execute();

    List<BinarySolution> population = algorithm.result();
    long computingTime = algorithmRunner.getComputingTime();

    JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");

    printFinalSolutionSet(population);
  }
}
