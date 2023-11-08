package jmetal.algorithm.examples.multiobjective.spea2;

import java.util.List;
import jmetal.algorithm.examples.AlgorithmRunner;
import jmetal.algorithm.multiobjective.spea2.SPEA2Builder;
import jmetal.core.operator.crossover.impl.SinglePointCrossover;
import jmetal.core.operator.mutation.impl.BitFlipMutation;
import jmetal.core.operator.selection.SelectionOperator;
import jmetal.core.operator.selection.impl.BinaryTournamentSelection;
import jmetal.problem.ProblemFactory;
import jmetal.core.problem.binaryproblem.BinaryProblem;
import jmetal.core.solution.binarysolution.BinarySolution;
import jmetal.core.util.AbstractAlgorithmRunner;

/**
 * Class for configuring and running the SPEA2 algorithm (binary encoding)
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */

public class SPEA2BinaryRunner extends AbstractAlgorithmRunner {

  /**
   * @param args Command line arguments.
   */
  public static void main(String[] args) {

    String problemName = "jmetal.core.problem.multiobjective.OneZeroMax";

    var problem = (BinaryProblem) ProblemFactory.<BinarySolution>loadProblem(problemName);

    double crossoverProbability = 0.9;
    var crossover = new SinglePointCrossover(crossoverProbability);

    double mutationProbability = 1.0 / problem.totalNumberOfBits();
    var mutation = new BitFlipMutation(mutationProbability);

    SelectionOperator<List<BinarySolution>, BinarySolution> selection = new BinaryTournamentSelection<BinarySolution>();

    var algorithm = new SPEA2Builder<>(problem, crossover, mutation)
        .setSelectionOperator(selection)
        .setMaxIterations(250)
        .setPopulationSize(100)
        .build();

    new AlgorithmRunner.Executor(algorithm).execute();

    List<BinarySolution> population = algorithm.result();

    printFinalSolutionSet(population);
  }
}
