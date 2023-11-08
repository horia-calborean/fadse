package jmetal.component.examples.singleobjective.geneticalgorithm;

import java.io.IOException;
import java.util.List;
import jmetal.component.algorithm.EvolutionaryAlgorithm;
import jmetal.component.algorithm.singleobjective.GeneticAlgorithmBuilder;
import jmetal.component.catalogue.common.termination.Termination;
import jmetal.component.catalogue.common.termination.impl.TerminationByEvaluations;
import jmetal.core.operator.crossover.impl.PMXCrossover;
import jmetal.core.operator.mutation.impl.PermutationSwapMutation;
import jmetal.core.problem.permutationproblem.PermutationProblem;
import jmetal.problem.singleobjective.TSP;
import jmetal.core.solution.permutationsolution.PermutationSolution;
import jmetal.core.util.AbstractAlgorithmRunner;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.errorchecking.JMetalException;
import jmetal.core.util.fileoutput.SolutionListOutput;
import jmetal.core.util.fileoutput.impl.DefaultFileOutputContext;
import jmetal.core.util.observer.impl.FitnessObserver;
import jmetal.core.util.observer.impl.FitnessPlotObserver;
import jmetal.core.util.pseudorandom.JMetalRandom;

/**
 * Class to configure and run a genetic algorithm to solve an instance of the TSP
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class GeneticAlgorithmTSPExample extends AbstractAlgorithmRunner {
  public static void main(String[] args) throws JMetalException, IOException {
    PermutationProblem<PermutationSolution<Integer>> problem;

    problem = new TSP("resources/tspInstances/kroA100.tsp") ;

    int populationSize = 100;
    int offspringPopulationSize = populationSize;

    var crossover = new PMXCrossover(0.9) ;

    double mutationProbability = 1.0 / problem.numberOfVariables() ;
    var mutation = new PermutationSwapMutation<Integer>(mutationProbability) ;

    Termination termination = new TerminationByEvaluations(1500000);

    EvolutionaryAlgorithm<PermutationSolution<Integer>> geneticAlgorithm = new GeneticAlgorithmBuilder<>(
        "GGA",
        problem,
        populationSize,
        offspringPopulationSize,
        crossover,
        mutation)
        .setTermination(termination)
        .build();

    geneticAlgorithm.observable().register(new FitnessObserver(20000));
    var chartObserver = new FitnessPlotObserver("Genetic algorithm", "Evaluations", "Fitness",
        "fitness", 100) ;
    geneticAlgorithm.observable().register(chartObserver);

    geneticAlgorithm.run();

    List<PermutationSolution<Integer>> population = geneticAlgorithm.result();
    JMetalLogger.logger.info("Total execution time : " + geneticAlgorithm.totalComputingTime() + "ms");
    JMetalLogger.logger.info("Number of evaluations: " + geneticAlgorithm.numberOfEvaluations());
    JMetalLogger.logger.info("Best found solution: " + population.get(0).objectives()[0]) ;

    new SolutionListOutput(population)
            .setVarFileOutputContext(new DefaultFileOutputContext("VAR.csv", ","))
            .setFunFileOutputContext(new DefaultFileOutputContext("FUN.csv", ","))
            .print();

    JMetalLogger.logger.info("Random seed: " + JMetalRandom.getInstance().getSeed());
    JMetalLogger.logger.info("Objectives values have been written to file FUN.csv");
    JMetalLogger.logger.info("Variables values have been written to file VAR.csv");
  }
}
