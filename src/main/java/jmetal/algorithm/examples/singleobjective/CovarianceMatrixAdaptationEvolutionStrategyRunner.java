package jmetal.algorithm.examples.singleobjective;

import java.util.ArrayList;
import java.util.List;
import jmetal.core.algorithm.Algorithm;
import jmetal.algorithm.examples.AlgorithmRunner;
import jmetal.algorithm.singleobjective.evolutionstrategy.CovarianceMatrixAdaptationEvolutionStrategy;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.problem.singleobjective.Sphere;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.fileoutput.SolutionListOutput;
import jmetal.core.util.fileoutput.impl.DefaultFileOutputContext;

/**
 */
public class CovarianceMatrixAdaptationEvolutionStrategyRunner {
  /**
   */
  public static void main(String[] args) throws Exception {

    Algorithm<DoubleSolution> algorithm;
    DoubleProblem problem = new Sphere() ;

    algorithm = new CovarianceMatrixAdaptationEvolutionStrategy.Builder(problem)
            .build() ;


    AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm)
            .execute() ;

    DoubleSolution solution = algorithm.result() ;
    List<DoubleSolution> population = new ArrayList<>(1) ;
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
