package jmetal.algorithm.examples.multiobjective;

import java.io.IOException;
import java.util.List;
import jmetal.algorithm.examples.AlgorithmRunner;
import jmetal.algorithm.multiobjective.espea.ESPEABuilder;
import jmetal.algorithm.multiobjective.espea.util.EnergyArchive.ReplacementStrategy;
import jmetal.core.operator.crossover.impl.SBXCrossover;
import jmetal.core.operator.mutation.impl.PolynomialMutation;
import jmetal.problem.ProblemFactory;
import jmetal.core.qualityindicator.QualityIndicatorUtils;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.AbstractAlgorithmRunner;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.SolutionListUtils;
import jmetal.core.util.VectorUtils;

/**
 * Class to configure and run the ESPEA algorithm
 *
 * @author Marlon Braun <marlon.braun@partner.kit.edu>
 */
public class ESPEARunner extends AbstractAlgorithmRunner {

  /**
   * @param args Command line arguments.
   */
  public static void main(String[] args) throws IOException {

    String problemName = "jmetal.core.problem.multiobjective.zdt.ZDT3";
    String referenceParetoFront = "resources/referenceFrontsCSV/ZDT3.csv";

    var problem = ProblemFactory.<DoubleSolution>loadProblem(problemName);

    double crossoverProbability = 0.9;
    double crossoverDistributionIndex = 20.0;
    var crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);

    double mutationProbability = 1.0 / problem.numberOfVariables();
    double mutationDistributionIndex = 20.0;
    var mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

    ESPEABuilder<DoubleSolution> builder = new ESPEABuilder<>(problem, crossover, mutation);
    builder.setMaxEvaluations(25000);
    builder.setPopulationSize(100);
    builder.setReplacementStrategy(ReplacementStrategy.WORST_IN_ARCHIVE);

//    ScalarizationWrapper wrapper = new ScalarizationWrapper(ScalarizationType.TRADEOFF_UTILITY);
//    builder.setScalarization(wrapper);

    var algorithm = builder.build();

    AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm)
        .execute();

    List<DoubleSolution> population = algorithm.result();
    long computingTime = algorithmRunner.getComputingTime();

    JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");

    printFinalSolutionSet(population);
    QualityIndicatorUtils.printQualityIndicators(
        SolutionListUtils.getMatrixWithObjectiveValues(population),
        VectorUtils.readVectors(referenceParetoFront, ","));
  }
}
