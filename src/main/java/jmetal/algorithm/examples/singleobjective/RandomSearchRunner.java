package jmetal.algorithm.examples.singleobjective;

import java.io.FileNotFoundException;
import java.util.List;
import jmetal.core.algorithm.Algorithm;
import jmetal.algorithm.examples.AlgorithmRunner;
import jmetal.algorithm.multiobjective.randomsearch.RandomSearchBuilder;
import jmetal.core.problem.Problem;
import jmetal.problem.singleobjective.OneMax;
import jmetal.core.solution.binarysolution.BinarySolution;
import jmetal.core.util.AbstractAlgorithmRunner;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.errorchecking.JMetalException;

/**
 * Class for configuring and running the random search algorithm
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */

public class RandomSearchRunner extends AbstractAlgorithmRunner {
  /**
   * @param args Command line arguments.
   * @throws SecurityException
   * Invoking command:
  java jmetal.runner.multiobjective.RandomSearchRunner problemName [referenceFront]
   */
  public static void main(String[] args) throws JMetalException, FileNotFoundException {
    Problem<BinarySolution> problem;
    Algorithm<List<BinarySolution>> algorithm;

    problem = new OneMax(1024) ;

    algorithm = new RandomSearchBuilder<>(problem)
            .setMaxEvaluations(25000)
            .build() ;

    AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm)
            .execute() ;

    List<BinarySolution> population = algorithm.result() ;
    long computingTime = algorithmRunner.getComputingTime() ;

    JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");

    JMetalLogger.logger.info("Fitness: " + population.get(0).objectives()[0]) ;
    JMetalLogger.logger.info("Solution: " + population.get(0).variables().get(0)) ;
  }
}
