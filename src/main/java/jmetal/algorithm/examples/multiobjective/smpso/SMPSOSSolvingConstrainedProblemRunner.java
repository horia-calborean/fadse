package jmetal.algorithm.examples.multiobjective.smpso;

import java.io.IOException;
import java.util.List;
import jmetal.algorithm.examples.AlgorithmRunner;
import jmetal.algorithm.multiobjective.smpso.SMPSOBuilder;
import jmetal.core.operator.mutation.impl.PolynomialMutation;
import jmetal.problem.ProblemFactory;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.core.qualityindicator.QualityIndicatorUtils;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.AbstractAlgorithmRunner;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.SolutionListUtils;
import jmetal.core.util.VectorUtils;
import jmetal.core.util.archive.BoundedArchive;
import jmetal.core.util.archive.impl.CrowdingDistanceArchive;
import jmetal.core.util.comparator.constraintcomparator.impl.OverallConstraintViolationDegreeComparator;
import jmetal.core.util.comparator.dominanceComparator.impl.DominanceWithConstraintsComparator;
import jmetal.core.util.evaluator.impl.SequentialSolutionListEvaluator;

/**
 * Class for configuring and running the SMPSO algorithm
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class SMPSOSSolvingConstrainedProblemRunner extends AbstractAlgorithmRunner {

  /**
   * @param args Command line arguments.
   */
  public static void main(String[] args) throws IOException {
    String problemName = "jmetal.core.problem.multiobjective.Golinski";
    String referenceParetoFront = "resources/referenceFrontsCSV/Golinski.csv";

    DoubleProblem problem = (DoubleProblem) ProblemFactory.<DoubleSolution>loadProblem(problemName);

    BoundedArchive<DoubleSolution> archive = new CrowdingDistanceArchive<DoubleSolution>(100);

    double mutationProbability = 1.0 / problem.numberOfVariables();
    double mutationDistributionIndex = 20.0;
    var mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

    var algorithm = new SMPSOBuilder(problem, archive)
        .setMutation(mutation)
        .setMaxIterations(250)
        .setSwarmSize(100)
        .setSolutionListEvaluator(new SequentialSolutionListEvaluator<>())
        .setDominanceComparator(new DominanceWithConstraintsComparator<>(
            new OverallConstraintViolationDegreeComparator<>()))
        .build();

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
