package jmetal.algorithm.examples.multiobjective.mosa;

import java.io.IOException;
import java.util.List;
import jmetal.algorithm.examples.AlgorithmRunner;
import jmetal.algorithm.multiobjective.mosa.MOSA;
import jmetal.algorithm.multiobjective.mosa.cooling.impl.Exponential;
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
import jmetal.core.util.archive.BoundedArchive;
import jmetal.core.util.archive.impl.GenericBoundedArchive;
import jmetal.core.util.densityestimator.impl.CrowdingDistanceDensityEstimator;
import jmetal.core.util.errorchecking.JMetalException;

/**
 * Class for configuring and running the {@link MOSA} algorithm
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class MOSARunner extends AbstractAlgorithmRunner {

  public static void main(String[] args) throws JMetalException, IOException {
    String problemName = "jmetal.core.problem.multiobjective.zdt.ZDT2";
    String referenceParetoFront = "resources/referenceFrontsCSV/ZDT2.csv";

    Problem<DoubleSolution> problem = ProblemFactory.loadProblem(problemName);

    MutationOperator<DoubleSolution> mutation =
        new PolynomialMutation(1.0 / problem.numberOfVariables(), 20.0);

    BoundedArchive<DoubleSolution> archive = new GenericBoundedArchive<>(100, new CrowdingDistanceDensityEstimator<>());

    DoubleSolution initialSolution = problem.createSolution() ;
    problem.evaluate(initialSolution) ;
    MOSA<DoubleSolution> algorithm =
        new MOSA<>(initialSolution, problem, 25000, archive, mutation, 1.0, new Exponential(0.95));

    AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();

    List<DoubleSolution> population = algorithm.result();
    long computingTime = algorithmRunner.getComputingTime();

    JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");
    JMetalLogger.logger.info(
        "Number of non-accepted solutions: " + algorithm.getNumberOfWorstAcceptedSolutions());

    printFinalSolutionSet(population);
    QualityIndicatorUtils.printQualityIndicators(
        SolutionListUtils.getMatrixWithObjectiveValues(population),
        VectorUtils.readVectors(referenceParetoFront, ","));
  }
}
