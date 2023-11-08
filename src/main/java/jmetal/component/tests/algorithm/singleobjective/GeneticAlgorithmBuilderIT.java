package jmetal.component.tests.algorithm.singleobjective;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jmetal.component.algorithm.singleobjective.GeneticAlgorithmBuilder;
import org.junit.jupiter.api.Test;
import jmetal.component.algorithm.EvolutionaryAlgorithm;
import jmetal.component.catalogue.common.termination.Termination;
import jmetal.component.catalogue.common.termination.impl.TerminationByEvaluations;
import jmetal.core.operator.crossover.impl.SinglePointCrossover;
import jmetal.core.operator.mutation.impl.BitFlipMutation;
import jmetal.core.problem.binaryproblem.BinaryProblem;
import jmetal.problem.singleobjective.OneMax;
import jmetal.core.solution.binarysolution.BinarySolution;

class GeneticAlgorithmBuilderIT {

  @Test
  void AGenerationalGeneticAlgorithmReturnTheCorrectSolutionWhenSolvingProblemOneMax() {
    int number_of_bits = 246 ;
    BinaryProblem problem = new OneMax(number_of_bits) ;

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


    geneticAlgorithm.run();


    BinarySolution solution = geneticAlgorithm.result().get(0) ;
    assertEquals(number_of_bits, -1 * (int)solution.objectives()[0]) ;
  }

  @Test
  void ASteadyStateGeneticAlgorithmReturnTheCorrectSolutionWhenSolvingProblemOneMax() {
    int number_of_bits = 256 ;
    BinaryProblem problem = new OneMax(number_of_bits) ;

    double crossoverProbability = 0.9;
    var crossover = new SinglePointCrossover(crossoverProbability);

    double mutationProbability = 1.0 / problem.totalNumberOfBits() ;
    var mutation = new BitFlipMutation(mutationProbability);

    int populationSize = 100;
    int offspringPopulationSize = 1;

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


    geneticAlgorithm.run();


    BinarySolution solution = geneticAlgorithm.result().get(0) ;
    assertEquals(number_of_bits, -1 * (int)solution.objectives()[0]) ;
  }
}