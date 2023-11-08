package jmetal.component.examples.multiobjective.nsgaii;

import java.io.IOException;
import java.util.List;
import jmetal.component.algorithm.EvolutionaryAlgorithm;
import jmetal.component.algorithm.multiobjective.NSGAIIBuilder;
import jmetal.component.catalogue.common.termination.Termination;
import jmetal.component.catalogue.common.termination.impl.TerminationByEvaluations;
import jmetal.core.operator.crossover.impl.SinglePointCrossover;
import jmetal.core.operator.mutation.impl.BitFlipMutation;
import jmetal.core.problem.binaryproblem.BinaryProblem;
import jmetal.problem.multiobjective.OneZeroMax;
import jmetal.core.solution.binarysolution.BinarySolution;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.errorchecking.JMetalException;
import jmetal.core.util.fileoutput.SolutionListOutput;
import jmetal.core.util.fileoutput.impl.DefaultFileOutputContext;
import jmetal.core.util.pseudorandom.JMetalRandom;

/**
 * Class to configure and run the NSGA-II algorithm configured with standard settings for solving
 * a binary problem ({@link OneZeroMax} is a multi-objective variant of OneMax).
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class NSGAIIBinaryProblemExample {
  public static void main(String[] args) throws JMetalException, IOException {

    BinaryProblem problem = new OneZeroMax(256) ;

    var  crossover = new SinglePointCrossover(0.9);
    var mutation = new BitFlipMutation(1.0 / problem.totalNumberOfBits());

    int populationSize = 100;
    int offspringPopulationSize = 100;

    Termination termination = new TerminationByEvaluations(25000);

    EvolutionaryAlgorithm<BinarySolution> nsgaii = new NSGAIIBuilder<>(
                    problem,
                    populationSize,
                    offspringPopulationSize,
                    crossover,
                    mutation)
        .setTermination(termination)
        .build();

    nsgaii.run();

    List<BinarySolution> population = nsgaii.result();
    JMetalLogger.logger.info("Total execution time : " + nsgaii.totalComputingTime() + "ms");
    JMetalLogger.logger.info("Number of evaluations: " + nsgaii.numberOfEvaluations());

    new SolutionListOutput(population)
            .setVarFileOutputContext(new DefaultFileOutputContext("VAR.csv", ","))
            .setFunFileOutputContext(new DefaultFileOutputContext("FUN.csv", ","))
            .print();

    JMetalLogger.logger.info("Random seed: " + JMetalRandom.getInstance().getSeed());
    JMetalLogger.logger.info("Objectives values have been written to file FUN.csv");
    JMetalLogger.logger.info("Variables values have been written to file VAR.csv");
  }
}
