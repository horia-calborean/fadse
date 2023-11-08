package jmetal.component.examples.singleobjective.cellulargeneticalgorithm;

import java.util.List;
import jmetal.component.algorithm.EvolutionaryAlgorithm;
import jmetal.component.algorithm.singleobjective.GeneticAlgorithmBuilder;
import jmetal.component.catalogue.common.termination.Termination;
import jmetal.component.catalogue.common.termination.impl.TerminationByEvaluations;
import jmetal.component.catalogue.ea.selection.impl.NeighborhoodSelection;
import jmetal.component.catalogue.ea.variation.impl.CrossoverAndMutationVariation;
import jmetal.core.operator.crossover.impl.SinglePointCrossover;
import jmetal.core.operator.mutation.impl.BitFlipMutation;
import jmetal.core.operator.selection.impl.NaryTournamentSelection;
import jmetal.core.problem.binaryproblem.BinaryProblem;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.problem.singleobjective.OneMax;
import jmetal.core.solution.binarysolution.BinarySolution;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.comparator.ObjectiveComparator;
import jmetal.core.util.errorchecking.JMetalException;
import jmetal.core.util.fileoutput.SolutionListOutput;
import jmetal.core.util.fileoutput.impl.DefaultFileOutputContext;
import jmetal.core.util.neighborhood.Neighborhood;
import jmetal.core.util.neighborhood.impl.C25;
import jmetal.core.util.observer.impl.FitnessObserver;
import jmetal.core.util.pseudorandom.JMetalRandom;
import jmetal.core.util.sequencegenerator.SequenceGenerator;
import jmetal.core.util.sequencegenerator.impl.IntegerBoundedSequenceGenerator;


/**
 * Class to configure and run an asynchronous cellular genetic algorithm to solve a {@link DoubleProblem}
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class AsynchronousCellularGeneticAlgorithmBinaryExample {
  public static void main(String[] args) throws JMetalException {
    BinaryProblem problem = new OneMax(1024) ;

    double crossoverProbability = 0.9;
    var crossover = new SinglePointCrossover(crossoverProbability);

    double mutationProbability = 1.0 / problem.totalNumberOfBits() ;
    var mutation = new BitFlipMutation(mutationProbability);

    int populationSize = 100;
    int offspringPopulationSize = 1;

    int rows = 10 ;
    int columns = 10 ;
    Neighborhood<BinarySolution> neighborhood = new C25<>(rows, columns) ;

    SequenceGenerator<Integer> solutionIndexGenerator = new IntegerBoundedSequenceGenerator(populationSize);

    var variation = new CrossoverAndMutationVariation<BinarySolution>(offspringPopulationSize, crossover, mutation) ;

    var selection =
        new NeighborhoodSelection<BinarySolution>(
            variation.getMatingPoolSize(),
            solutionIndexGenerator,
            neighborhood,
            new NaryTournamentSelection<>(2, new ObjectiveComparator<>(0)),
            true);

    Termination termination = new TerminationByEvaluations(40000);

    EvolutionaryAlgorithm<BinarySolution> geneticAlgorithm = new GeneticAlgorithmBuilder<>(
        "acGA",
                    problem,
                    populationSize,
                    offspringPopulationSize,
                    crossover,
                    mutation)
        .setTermination(termination)
        .setVariation(variation)
        .setSelection(selection)
        .build();

    geneticAlgorithm.observable().register(new FitnessObserver(1000));

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
