package jmetal.algorithm.examples.multiobjective;

import java.util.List;
import jmetal.algorithm.examples.AlgorithmRunner;
import jmetal.algorithm.multiobjective.omopso.OMOPSOBuilder;
import jmetal.core.operator.mutation.impl.NonUniformMutation;
import jmetal.core.operator.mutation.impl.UniformMutation;
import jmetal.problem.ProblemFactory;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.core.qualityindicator.QualityIndicatorUtils;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.AbstractAlgorithmRunner;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.SolutionListUtils;
import jmetal.core.util.VectorUtils;
import jmetal.core.util.evaluator.impl.SequentialSolutionListEvaluator;

/**
 * Class for configuring and running the OMOPSO algorithm
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */

public class OMOPSORunner extends AbstractAlgorithmRunner {
  /**
   * @param args Command line arguments.
   */
  public static void main(String[] args) throws Exception {
    String problemName = "jmetal.core.problem.multiobjective.zdt.ZDT1";
    String referenceParetoFront = "resources/referenceFrontsCSV/ZDT1.csv" ;

    var problem = (DoubleProblem) ProblemFactory.<DoubleSolution> loadProblem(problemName);

    double mutationProbability = 1.0 / problem.numberOfVariables() ;

    var algorithm = new OMOPSOBuilder(problem, new SequentialSolutionListEvaluator<>())
        .setMaxIterations(250)
        .setSwarmSize(100)
        .setEta(0.0075)
        .setUniformMutation(new UniformMutation(mutationProbability, 0.5))
        .setNonUniformMutation(new NonUniformMutation(mutationProbability, 0.5, 250))
        .build();

    AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm)
        .execute();

    List<DoubleSolution> population = SolutionListUtils.distanceBasedSubsetSelection(algorithm.result(), 100);
    long computingTime = algorithmRunner.getComputingTime();

    JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");

    printFinalSolutionSet(population);
    QualityIndicatorUtils.printQualityIndicators(
        SolutionListUtils.getMatrixWithObjectiveValues(population),
        VectorUtils.readVectors(referenceParetoFront, ","));
  }
}
