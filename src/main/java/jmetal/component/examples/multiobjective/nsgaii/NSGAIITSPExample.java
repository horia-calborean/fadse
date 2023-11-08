package jmetal.component.examples.multiobjective.nsgaii;

import java.io.IOException;
import java.util.List;
import jmetal.component.algorithm.EvolutionaryAlgorithm;
import jmetal.component.algorithm.multiobjective.NSGAIIBuilder;
import jmetal.component.catalogue.common.termination.Termination;
import jmetal.component.catalogue.common.termination.impl.TerminationByEvaluations;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.crossover.impl.PMXCrossover;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.operator.mutation.impl.PermutationSwapMutation;
import jmetal.problem.multiobjective.MultiobjectiveTSP;
import jmetal.core.problem.permutationproblem.PermutationProblem;
import jmetal.core.solution.permutationsolution.PermutationSolution;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.errorchecking.JMetalException;
import jmetal.core.util.fileoutput.SolutionListOutput;
import jmetal.core.util.fileoutput.impl.DefaultFileOutputContext;
import jmetal.core.util.pseudorandom.JMetalRandom;

/**
 * Class to configure and run the NSGA-II algorithm to solve a bi-objective TSP.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class NSGAIITSPExample {
  public static void main(String[] args) throws JMetalException, IOException {

    PermutationProblem<PermutationSolution<Integer>> problem =
        new MultiobjectiveTSP(
            "resources/tspInstances/kroA100.tsp", "resources/tspInstances/kroB100.tsp");

    CrossoverOperator<PermutationSolution<Integer>> crossover = new PMXCrossover(0.9);

    double mutationProbability = 0.2;
    MutationOperator<PermutationSolution<Integer>> mutation = new PermutationSwapMutation<>(mutationProbability);

    int populationSize = 100;
    int offspringPopulationSize = 100;

    Termination termination = new TerminationByEvaluations(125000);

    EvolutionaryAlgorithm<PermutationSolution<Integer>> nsgaii = new NSGAIIBuilder<>(
                    problem,
                    populationSize,
                    offspringPopulationSize,
                    crossover,
                    mutation)
        .setTermination(termination)
        .build();

    nsgaii.run();

    List<PermutationSolution<Integer>> population = nsgaii.result();
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
