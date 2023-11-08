package jmetal.algorithm.examples.singleobjective;

import java.util.ArrayList;
import java.util.List;
import jmetal.core.algorithm.Algorithm;
import jmetal.algorithm.examples.AlgorithmRunner;
import jmetal.algorithm.singleobjective.evolutionstrategy.EvolutionStrategyBuilder;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.operator.mutation.impl.BitFlipMutation;
import jmetal.core.problem.binaryproblem.BinaryProblem;
import jmetal.problem.singleobjective.OneMax;
import jmetal.core.solution.binarysolution.BinarySolution;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.fileoutput.SolutionListOutput;
import jmetal.core.util.fileoutput.impl.DefaultFileOutputContext;

/**
 * Class to configure and run an elitist (mu + lambda) evolution strategy. The target problem is
 * OneMax.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class ElitistEvolutionStrategyRunner {
  /**
   * Usage: java jmetal.runner.singleobjective.ElitistEvolutionStrategyRunner
   */
  public static void main(String[] args) throws Exception {
    Algorithm<BinarySolution> algorithm;
    BinaryProblem problem = new OneMax(512) ;

    MutationOperator<BinarySolution> mutationOperator =
        new BitFlipMutation(1.0 / problem.bitsFromVariable(0)) ;

    algorithm = new EvolutionStrategyBuilder<BinarySolution>(problem, mutationOperator,
        EvolutionStrategyBuilder.EvolutionStrategyVariant.ELITIST)
        .setMaxEvaluations(25000)
        .setMu(1)
        .setLambda(10)
        .build() ;

    AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm)
        .execute() ;

    BinarySolution solution = algorithm.result() ;
    List<BinarySolution> population = new ArrayList<>(1) ;
    population.add(solution) ;

    long computingTime = algorithmRunner.getComputingTime() ;

    new SolutionListOutput(population)
        .setVarFileOutputContext(new DefaultFileOutputContext("VAR.tsv"))
        .setFunFileOutputContext(new DefaultFileOutputContext("FUN.tsv"))
        .print();

    JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");
    JMetalLogger.logger.info("Objectives values have been written to file FUN.tsv");
    JMetalLogger.logger.info("Variables values have been written to file VAR.tsv");
  }
}
