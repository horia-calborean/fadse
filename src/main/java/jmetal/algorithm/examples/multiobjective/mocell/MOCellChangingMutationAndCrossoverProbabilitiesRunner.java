package jmetal.algorithm.examples.multiobjective.mocell;

import java.io.IOException;
import java.util.List;
import jmetal.core.algorithm.Algorithm;
import jmetal.algorithm.examples.AlgorithmRunner;
import jmetal.algorithm.multiobjective.mocell.MOCell;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.crossover.impl.SBXCrossover;
import jmetal.core.operator.mutation.MutationOperator;
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
import jmetal.core.util.archive.impl.CrowdingDistanceArchive;
import jmetal.core.util.comparator.RankingAndCrowdingDistanceComparator;
import jmetal.core.util.errorchecking.JMetalException;
import jmetal.core.util.evaluator.SolutionListEvaluator;
import jmetal.core.util.evaluator.impl.SequentialSolutionListEvaluator;
import jmetal.core.util.neighborhood.Neighborhood;
import jmetal.core.util.neighborhood.impl.C9;

/**
 * Class to configure and run the MOCell algorithm
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class MOCellChangingMutationAndCrossoverProbabilitiesRunner extends AbstractAlgorithmRunner {

  /**
   * @param args Command line arguments
   * @throws JMetalException
   */
  public static void main(String[] args) throws JMetalException, IOException {
    Algorithm<List<DoubleSolution>> algorithm;

    String problemName = "jmetal.core.problem.multiobjective.zdt.ZDT4";
    String referenceParetoFront = "resources/referenceFrontsCSV/ZDT4.csv";

    Problem<DoubleSolution> problem = ProblemFactory.<DoubleSolution>loadProblem(problemName);

    double crossoverProbability = 0.9;
    double crossoverDistributionIndex = 20.0;
    var crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);

    double mutationProbability = 1.0 / problem.numberOfVariables();
    double mutationDistributionIndex = 20.0;
    var mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

    SelectionOperator<List<DoubleSolution>, DoubleSolution> selection = new BinaryTournamentSelection<>(
        new RankingAndCrowdingDistanceComparator<DoubleSolution>());

    @SuppressWarnings("serial")
    class MOCellWithChangesInVariationOperator extends MOCell<DoubleSolution> {
      /**
       * Constructor
       *
       * @param problem
       * @param maxEvaluations
       * @param populationSize
       * @param archive
       * @param neighborhood
       * @param crossoverOperator
       * @param mutationOperator
       * @param selectionOperator
       * @param evaluator
       */
      public MOCellWithChangesInVariationOperator(
          Problem<DoubleSolution> problem, int maxEvaluations, int populationSize,
          BoundedArchive<DoubleSolution> archive, Neighborhood<DoubleSolution> neighborhood,
          CrossoverOperator<DoubleSolution> crossoverOperator,
          MutationOperator<DoubleSolution> mutationOperator,
          SelectionOperator<List<DoubleSolution>, DoubleSolution> selectionOperator,
          SolutionListEvaluator<DoubleSolution> evaluator) {
        super(problem, maxEvaluations, populationSize, archive, neighborhood, crossoverOperator,
            mutationOperator,
            selectionOperator, evaluator);
      }

      @Override
      public void updateProgress() {
        super.updateProgress();

        if (evaluations > 10000) {
          crossoverOperator = new SBXCrossover(0.7, 20.0);
          mutationOperator = new PolynomialMutation(0.001, 30.0);
        }
      }
    }

    algorithm = new MOCellWithChangesInVariationOperator(
        problem,
        25000,
        100,
        new CrowdingDistanceArchive<>(100),
        new C9<DoubleSolution>((int) Math.sqrt(100), (int) Math.sqrt(100)),
        crossover,
        mutation,
        selection,
        new SequentialSolutionListEvaluator<DoubleSolution>());

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
