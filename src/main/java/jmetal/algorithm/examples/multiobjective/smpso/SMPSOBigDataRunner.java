package jmetal.algorithm.examples.multiobjective.smpso;

import java.util.List;
import jmetal.algorithm.examples.AlgorithmRunner;
import jmetal.algorithm.multiobjective.smpso.SMPSO;
import jmetal.algorithm.multiobjective.smpso.SMPSOBuilder;
import jmetal.core.operator.mutation.impl.PolynomialMutation;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.problem.multiobjective.cec2015OptBigDataCompetition.BigOpt2015;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.AbstractAlgorithmRunner;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.archive.BoundedArchive;
import jmetal.core.util.archive.impl.CrowdingDistanceArchive;
import jmetal.core.util.fileoutput.SolutionListOutput;
import jmetal.core.util.fileoutput.impl.DefaultFileOutputContext;
import jmetal.core.util.pseudorandom.JMetalRandom;

/**
 * Class for configuring and running the SMPSO algorithm to solve a problem of the CEC2015 Big
 * Optimization competition
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */

public class SMPSOBigDataRunner extends AbstractAlgorithmRunner {

  /**
   * @param args Command line arguments.
   */
  public static void main(String[] args) {

    String instanceName = "D12";
    DoubleProblem problem = new BigOpt2015(instanceName);

    BoundedArchive<DoubleSolution> archive = new CrowdingDistanceArchive<DoubleSolution>(20);

    double mutationProbability = 1.0 / problem.numberOfVariables();
    double mutationDistributionIndex = 20.0;
    var mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

    var algorithm = new SMPSOBuilder(problem, archive)
        .setMutation(mutation)
        .setMaxIterations(250)
        .setSwarmSize(20)
        .build();

    AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm)
        .execute();

    List<DoubleSolution> population = ((SMPSO) algorithm).result();
    long computingTime = algorithmRunner.getComputingTime();

    new SolutionListOutput(population)
        .setVarFileOutputContext(new DefaultFileOutputContext("VAR.tsv"))
        .setFunFileOutputContext(new DefaultFileOutputContext("FUN.tsv"))
        .print();

    JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");
    JMetalLogger.logger.info("Objectives values have been written to file FUN.tsv");
    JMetalLogger.logger.info("Variables values have been written to file VAR.tsv");
    JMetalLogger.logger.info("Random seed: " + JMetalRandom.getInstance().getSeed());
  }
}
