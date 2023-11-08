package jmetal.algorithm.examples.multiobjective.paes;

import java.io.IOException;
import java.util.List;
import jmetal.algorithm.examples.AlgorithmRunner;
import jmetal.algorithm.multiobjective.paes.PAES;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.operator.mutation.impl.PolynomialMutation;
import jmetal.core.problem.Problem;
import jmetal.problem.ProblemFactory;
import jmetal.core.qualityindicator.QualityIndicatorUtils;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.AbstractAlgorithmRunner;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.SolutionListUtils;
import jmetal.core.util.VectorUtils;
import jmetal.core.util.archive.impl.GenericBoundedArchive;
import jmetal.core.util.densityestimator.impl.CrowdingDistanceDensityEstimator;

/**
 * Class for configuring and running the PAES algorithm
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class PAESWithCrowdingDistanceArchiveRunner extends AbstractAlgorithmRunner {

  /**
   * @param args Command line arguments.
   */
  public static void main(String[] args) throws IOException {
    String problemName = "jmetal.core.problem.multiobjective.Kursawe";
    String referenceParetoFront = "resources/referenceFrontsCSV/Kursawe.csv";

    Problem<DoubleSolution> problem = ProblemFactory.loadProblem(problemName);

    MutationOperator<DoubleSolution> mutation =
        new PolynomialMutation(1.0 / problem.numberOfVariables(), 20.0);

    PAES<DoubleSolution> algorithm =
        new PAES<>(
            problem,
            25000,
            new GenericBoundedArchive<>(100, new CrowdingDistanceDensityEstimator<>()),
            mutation);

    AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();

    List<DoubleSolution> population = algorithm.result();
    long computingTime = algorithmRunner.getComputingTime();

    JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");

    printFinalSolutionSet(population);
    QualityIndicatorUtils.printQualityIndicators(
        SolutionListUtils.getMatrixWithObjectiveValues(population),
        VectorUtils.readVectors(referenceParetoFront, ","));  }
}
