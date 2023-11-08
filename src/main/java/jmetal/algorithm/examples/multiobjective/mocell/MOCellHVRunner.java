package jmetal.algorithm.examples.multiobjective.mocell;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import jmetal.algorithm.examples.AlgorithmRunner;
import jmetal.algorithm.multiobjective.mocell.MOCellBuilder;
import jmetal.core.operator.crossover.impl.SBXCrossover;
import jmetal.core.operator.mutation.impl.PolynomialMutation;
import jmetal.core.operator.selection.SelectionOperator;
import jmetal.core.operator.selection.impl.BinaryTournamentSelection;
import jmetal.core.problem.Problem;
import jmetal.problem.ProblemFactory;
import jmetal.core.qualityindicator.QualityIndicatorUtils;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.AbstractAlgorithmRunner;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.SolutionListUtils;
import jmetal.core.util.VectorUtils;
import jmetal.core.util.archive.BoundedArchive;
import jmetal.core.util.archive.impl.HypervolumeArchive;
import jmetal.core.util.comparator.RankingAndCrowdingDistanceComparator;
import jmetal.core.util.errorchecking.JMetalException;
import jmetal.core.util.legacy.qualityindicator.impl.hypervolume.impl.PISAHypervolume;

/**
 * Class to configure and run the MOCell algorithm
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class MOCellHVRunner extends AbstractAlgorithmRunner {

  /**
   * @param args Command line arguments.
   * @throws JMetalException
   * @throws FileNotFoundException Invoking command: java
   *                               jmetal.runner.multiobjective.MOCellRunner problemName
   *                               [referenceFront]
   */
  public static void main(String[] args) throws JMetalException, IOException {
    var problemName = "jmetal.core.problem.multiobjective.zdt.ZDT4";
    var referenceParetoFront = "resources/referenceFrontsCSV/ZDT4.csv";

    Problem<DoubleSolution> problem = ProblemFactory.<DoubleSolution>loadProblem(problemName);

    double crossoverProbability = 0.9;
    double crossoverDistributionIndex = 20.0;
    var crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);

    double mutationProbability = 1.0 / problem.numberOfVariables();
    double mutationDistributionIndex = 20.0;
    var mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

    SelectionOperator<List<DoubleSolution>, DoubleSolution> selection = new BinaryTournamentSelection<>(
        new RankingAndCrowdingDistanceComparator<>());

    BoundedArchive<DoubleSolution> archive =
        new HypervolumeArchive<DoubleSolution>(100, new PISAHypervolume<DoubleSolution>());

    var algorithm = new MOCellBuilder<DoubleSolution>(problem, crossover, mutation)
        .setSelectionOperator(selection)
        .setMaxEvaluations(25000)
        .setPopulationSize(100)
        .setArchive(archive)
        .build();

    AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm)
        .execute();

    List<DoubleSolution> population = algorithm.result();
    long computingTime = algorithmRunner.getComputingTime();

    JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");

    QualityIndicatorUtils.printQualityIndicators(
        SolutionListUtils.getMatrixWithObjectiveValues(population),
        VectorUtils.readVectors(referenceParetoFront, ","));
  }
}
