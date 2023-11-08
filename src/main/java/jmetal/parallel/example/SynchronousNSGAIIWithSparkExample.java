package jmetal.parallel.example;

import static java.lang.Math.sin;

import java.io.FileNotFoundException;
import java.util.List;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import jmetal.core.algorithm.Algorithm;
import jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.crossover.impl.SBXCrossover;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.operator.mutation.impl.PolynomialMutation;
import jmetal.core.operator.selection.SelectionOperator;
import jmetal.core.operator.selection.impl.BinaryTournamentSelection;
import jmetal.parallel.synchronous.SparkSolutionListEvaluator;
import jmetal.core.problem.Problem;
import jmetal.problem.multiobjective.zdt.ZDT2;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.AbstractAlgorithmRunner;
import jmetal.core.util.comparator.RankingAndCrowdingDistanceComparator;
import jmetal.core.util.errorchecking.JMetalException;
import jmetal.core.util.evaluator.SolutionListEvaluator;

/**
 * Class for configuring and running the NSGA-II algorithm (parallel version)
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class SynchronousNSGAIIWithSparkExample extends AbstractAlgorithmRunner {
  /**
   * @param args Command line arguments.
   * @throws SecurityException Invoking command: java
   *     jmetal.runner.multiobjective.nsgaii.ParallelNSGAIIRunner problemName [referenceFront]
   */
  public static void main(String[] args) throws JMetalException, FileNotFoundException {
    Problem<DoubleSolution> problem;
    Algorithm<List<DoubleSolution>> algorithm;
    CrossoverOperator<DoubleSolution> crossover;
    MutationOperator<DoubleSolution> mutation;
    SelectionOperator<List<DoubleSolution>, DoubleSolution> selection;

    problem = new ZDT2() {
      @Override
      public DoubleSolution evaluate(DoubleSolution solution) {
        super.evaluate(solution);
        computingDelay();

        return solution;
      }

      private void computingDelay() {
        for (long i = 0; i < 1000; i++)
          for (long j = 0; j < 1000; j++) {
            double a = sin(i) * Math.cos(j);
          }
      }
    };

    double crossoverProbability = 0.9;
    double crossoverDistributionIndex = 20.0;
    crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);

    double mutationProbability = 1.0 / problem.numberOfVariables();
    double mutationDistributionIndex = 20.0;
    mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

    selection = new BinaryTournamentSelection<>(new RankingAndCrowdingDistanceComparator<>());

    int maxEvaluations = 25000;
    int populationSize = 100;

    Logger.getLogger("org").setLevel(Level.OFF) ;

    SparkConf sparkConf = new SparkConf()
            .setMaster("local[8]")
            .setAppName("NSGA-II with Spark");

    JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
    SolutionListEvaluator<DoubleSolution> evaluator = new SparkSolutionListEvaluator<>(sparkContext) ;

    algorithm = new NSGAIIBuilder<>(problem, crossover, mutation, populationSize)
            .setSelectionOperator(selection)
            .setMaxEvaluations(maxEvaluations)
            .setSolutionListEvaluator(new SparkSolutionListEvaluator<>(sparkContext))
            .build();

    algorithm.run();
    List<DoubleSolution> population = algorithm.result();

    evaluator.shutdown();

    printFinalSolutionSet(population);
  }
}
