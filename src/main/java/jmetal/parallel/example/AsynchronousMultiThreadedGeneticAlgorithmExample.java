package jmetal.parallel.example;

import static java.lang.Math.sin;

import java.util.List;
import jmetal.component.catalogue.common.termination.impl.TerminationByEvaluations;
import jmetal.component.catalogue.ea.replacement.Replacement;
import jmetal.component.catalogue.ea.replacement.impl.MuPlusLambdaReplacement;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.crossover.impl.UniformCrossover;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.operator.mutation.impl.BitFlipMutation;
import jmetal.core.operator.selection.SelectionOperator;
import jmetal.core.operator.selection.impl.BinaryTournamentSelection;
import jmetal.parallel.asynchronous.algorithm.impl.AsynchronousMultiThreadedGeneticAlgorithm;
import jmetal.problem.singleobjective.OneMax;
import jmetal.core.solution.binarysolution.BinarySolution;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.comparator.ObjectiveComparator;
import jmetal.core.util.fileoutput.SolutionListOutput;
import jmetal.core.util.fileoutput.impl.DefaultFileOutputContext;
import jmetal.core.util.observer.impl.FitnessObserver;

public class AsynchronousMultiThreadedGeneticAlgorithmExample {
  public static void main(String[] args) {
    CrossoverOperator<BinarySolution> crossover;
    MutationOperator<BinarySolution> mutation;
    SelectionOperator<List<BinarySolution>, BinarySolution> selection ;
    Replacement<BinarySolution> replacement ;

    int populationSize = 100;
    int maxEvaluations = 25000;
    int numberOfCores = 16 ;

    OneMax problem = new OneMax(1024) {
      @Override
      public BinarySolution evaluate (BinarySolution solution) {
        super.evaluate(solution) ;
        computingDelay();

        return solution ;
      }

      private void computingDelay() {
        for (long i = 0 ; i < 10000; i++)
          for (long j = 0; j < 100; j++) {
            double a = sin(i)*Math.cos(j) ;
          }
      }
    } ;

    double crossoverProbability = 0.9;
    crossover = new UniformCrossover(crossoverProbability);

    double mutationProbability = 1.0 / 1024;
    mutation = new BitFlipMutation(mutationProbability);

    selection = new BinaryTournamentSelection<>(new ObjectiveComparator<>(0)) ;

    replacement = new MuPlusLambdaReplacement<>(new ObjectiveComparator<>(0)) ;

    long initTime = System.currentTimeMillis();
    AsynchronousMultiThreadedGeneticAlgorithm<BinarySolution> geneticAlgorithm =
        new AsynchronousMultiThreadedGeneticAlgorithm<>(
            numberOfCores, problem, populationSize, crossover, mutation, selection, replacement, new TerminationByEvaluations(maxEvaluations));

    FitnessObserver printObjectivesObserver = new FitnessObserver(100) ;
    geneticAlgorithm.getObservable().register(printObjectivesObserver);

    geneticAlgorithm.run();

    long endTime = System.currentTimeMillis();

    List<BinarySolution> resultList = geneticAlgorithm.getResult();

    JMetalLogger.logger.info("Computing time: " + (endTime - initTime));
    new SolutionListOutput(resultList)
            .setVarFileOutputContext(new DefaultFileOutputContext("VAR.csv", ","))
            .setFunFileOutputContext(new DefaultFileOutputContext("FUN.csv", ","))
            .print();
    System.exit(0);
  }
}
