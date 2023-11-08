package jmetal.component.examples.singleobjective.geneticalgorithm;

import java.io.IOException;
import java.util.List;
import jmetal.component.algorithm.EvolutionaryAlgorithm;
import jmetal.component.algorithm.singleobjective.GeneticAlgorithmBuilder;
import jmetal.component.catalogue.common.termination.Termination;
import jmetal.component.catalogue.common.termination.impl.TerminationByEvaluations;
import jmetal.core.operator.crossover.impl.SinglePointCrossover;
import jmetal.core.operator.mutation.impl.BitFlipMutation;
import jmetal.core.problem.binaryproblem.BinaryProblem;
import jmetal.problem.singleobjective.OneMax;
import jmetal.core.solution.binarysolution.BinarySolution;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.errorchecking.JMetalException;
import jmetal.core.util.fileoutput.SolutionListOutput;
import jmetal.core.util.fileoutput.impl.DefaultFileOutputContext;
import jmetal.core.util.observer.impl.FitnessPlotObserver;
import jmetal.core.util.pseudorandom.JMetalRandom;

/**
 * Class to configure and run a generational genetic algorithm to solve a {@link BinaryProblem}
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class GenerationalGeneticAlgorithmBinaryExample {
  public static void main(String[] args) throws JMetalException, IOException {
    BinaryProblem problem = new OneMax(512) ;

    double crossoverProbability = 0.9;
    var crossover = new SinglePointCrossover(crossoverProbability);

    double mutationProbability = 1.0 / problem.totalNumberOfBits() ;
    var mutation = new BitFlipMutation(mutationProbability);

    int populationSize = 100;
    int offspringPopulationSize = populationSize;

    Termination termination = new TerminationByEvaluations(25000);

    EvolutionaryAlgorithm<BinarySolution> geneticAlgorithm = new GeneticAlgorithmBuilder<>(
        "GGA",
                    problem,
                    populationSize,
                    offspringPopulationSize,
                    crossover,
                    mutation)
        .setTermination(termination)
        .build();

    geneticAlgorithm.observable().register(new FitnessPlotObserver("Genetic algorithm",
        "Evaluations", "Fitness", "Fitness", 500));

    geneticAlgorithm.run();

    List<BinarySolution> population = geneticAlgorithm.result();
    JMetalLogger.logger.info("Total execution time : " + geneticAlgorithm.totalComputingTime() + "ms");
    JMetalLogger.logger.info("Number of evaluations: " + geneticAlgorithm.numberOfEvaluations());
    JMetalLogger.logger.info("Best fitness: " + geneticAlgorithm.result().get(0).objectives()[0]);

    new SolutionListOutput(population)
            .setVarFileOutputContext(new DefaultFileOutputContext("VAR.csv", ","))
            .setFunFileOutputContext(new DefaultFileOutputContext("FUN.csv", ","))
            .print();

    JMetalLogger.logger.info("Random seed: " + JMetalRandom.getInstance().getSeed());
    JMetalLogger.logger.info("Objectives values have been written to file FUN.csv");
    JMetalLogger.logger.info("Variables values have been written to file VAR.csv");
  }
}
