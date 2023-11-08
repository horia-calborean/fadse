package jmetal.algorithm.examples.multiobjective;

import java.io.FileNotFoundException;
import java.util.List;
import jmetal.algorithm.examples.AlgorithmRunner;
import jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import jmetal.core.operator.crossover.impl.IntegerSBXCrossover;
import jmetal.core.operator.mutation.impl.IntegerPolynomialMutation;
import jmetal.core.operator.selection.SelectionOperator;
import jmetal.core.operator.selection.impl.BinaryTournamentSelection;
import jmetal.core.problem.Problem;
import jmetal.problem.ProblemFactory;
import jmetal.core.solution.integersolution.IntegerSolution;
import jmetal.core.util.AbstractAlgorithmRunner;
import jmetal.core.util.JMetalLogger;

/**
 * Class for configuring and running the NSGA-II algorithm (integer encoding)
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */

public class NSGAIIIntegerRunner extends AbstractAlgorithmRunner {

  /**
   * @param args Command line arguments.
   */
  public static void main(String[] args) throws FileNotFoundException {

    Problem<IntegerSolution> problem = ProblemFactory.<IntegerSolution>loadProblem(
        "jmetal.core.problem.multiobjective.NMMin");

    double crossoverProbability = 0.9;
    double crossoverDistributionIndex = 20.0;
    var crossover = new IntegerSBXCrossover(crossoverProbability, crossoverDistributionIndex);

    double mutationProbability = 1.0 / problem.numberOfVariables();
    double mutationDistributionIndex = 20.0;
    var mutation = new IntegerPolynomialMutation(mutationProbability, mutationDistributionIndex);

    SelectionOperator<List<IntegerSolution>, IntegerSolution> selection = new BinaryTournamentSelection<IntegerSolution>();

    int populationSize = 100;
    var algorithm = new NSGAIIBuilder<IntegerSolution>(problem, crossover, mutation, populationSize)
        .setSelectionOperator(selection)
        .setMaxEvaluations(25000)
        .build();

    AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm)
        .execute();

    List<IntegerSolution> population = algorithm.result();
    long computingTime = algorithmRunner.getComputingTime();

    JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");

    printFinalSolutionSet(population);
  }
}
