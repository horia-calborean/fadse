package jmetal.component.examples.singleobjective.cellulargeneticalgorithm;

import java.io.IOException;
import java.util.List;
import jmetal.component.algorithm.EvolutionaryAlgorithm;
import jmetal.component.algorithm.singleobjective.GeneticAlgorithmBuilder;
import jmetal.component.catalogue.common.termination.Termination;
import jmetal.component.catalogue.common.termination.impl.TerminationByEvaluations;
import jmetal.component.catalogue.ea.selection.impl.NeighborhoodSelection;
import jmetal.component.catalogue.ea.variation.impl.CrossoverAndMutationVariation;
import jmetal.core.operator.crossover.impl.SBXCrossover;
import jmetal.core.operator.mutation.impl.PolynomialMutation;
import jmetal.core.operator.selection.impl.NaryTournamentSelection;
import jmetal.core.problem.Problem;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.problem.singleobjective.Sphere;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.comparator.ObjectiveComparator;
import jmetal.core.util.errorchecking.JMetalException;
import jmetal.core.util.fileoutput.SolutionListOutput;
import jmetal.core.util.fileoutput.impl.DefaultFileOutputContext;
import jmetal.core.util.neighborhood.Neighborhood;
import jmetal.core.util.neighborhood.impl.C9;
import jmetal.core.util.observer.impl.FitnessObserver;
import jmetal.core.util.pseudorandom.JMetalRandom;
import jmetal.core.util.sequencegenerator.SequenceGenerator;
import jmetal.core.util.sequencegenerator.impl.IntegerBoundedSequenceGenerator;

/**
 * Class to configure and run a synchronous cellular genetic algorithm to solve a {@link DoubleProblem}
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class SynchronousCellularGeneticAlgorithmExample {
  public static void main(String[] args) throws JMetalException, IOException {
    Problem<DoubleSolution> problem = new Sphere(20) ;

    double crossoverProbability = 0.9;
    double crossoverDistributionIndex = 20.0;
    var crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);

    double mutationProbability = 1.0 / problem.numberOfVariables();
    double mutationDistributionIndex = 20.0;
    var mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

    int populationSize = 100;
    int offspringPopulationSize = populationSize;

    int rows = 10 ;
    int columns = 10 ;
    Neighborhood<DoubleSolution> neighborhood = new C9<>(rows, columns) ;

    SequenceGenerator<Integer> solutionIndexGenerator = new IntegerBoundedSequenceGenerator(populationSize);

    var variation = new CrossoverAndMutationVariation<>(offspringPopulationSize, crossover, mutation) ;

    var selection =
        new NeighborhoodSelection<>(
            variation.getMatingPoolSize(),
            solutionIndexGenerator,
            neighborhood,
            new NaryTournamentSelection<>(2, new ObjectiveComparator<>(0)),
            true);

    Termination termination = new TerminationByEvaluations(500000);

    EvolutionaryAlgorithm<DoubleSolution> geneticAlgorithm = new GeneticAlgorithmBuilder<>(
        "scGA",
                    problem,
                    populationSize,
                    offspringPopulationSize,
                    crossover,
                    mutation)
        .setTermination(termination)
        .setVariation(variation)
        .setSelection(selection)
        .build();

    geneticAlgorithm.observable().register(new FitnessObserver(5000));

    geneticAlgorithm.run();

    List<DoubleSolution> population = geneticAlgorithm.result();
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
