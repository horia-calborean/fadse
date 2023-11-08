package jmetal.algorithm.examples.singleobjective;

import java.util.ArrayList;
import java.util.List;
import jmetal.core.algorithm.Algorithm;
import jmetal.algorithm.examples.AlgorithmRunner;
import jmetal.algorithm.singleobjective.differentialevolution.DifferentialEvolutionBuilder;
import jmetal.core.operator.crossover.impl.DifferentialEvolutionCrossover;
import jmetal.core.operator.selection.impl.DifferentialEvolutionSelection;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.problem.singleobjective.Sphere;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.evaluator.SolutionListEvaluator;
import jmetal.core.util.evaluator.impl.MultiThreadedSolutionListEvaluator;
import jmetal.core.util.evaluator.impl.SequentialSolutionListEvaluator;
import jmetal.core.util.fileoutput.SolutionListOutput;
import jmetal.core.util.fileoutput.impl.DefaultFileOutputContext;

/**
 * Class to configure and run a differential evolution algorithm. The algorithm can be configured to
 * use threads. The number of cores is specified as an optional parameter. The target problem is
 * Sphere.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class DifferentialEvolutionRunner {
  private static final int DEFAULT_NUMBER_OF_CORES = 1;

  /** Usage: java jmetal.runner.singleobjective.DifferentialEvolutionRunner [cores] */
  public static void main(String[] args) throws Exception {

    DoubleProblem problem;
    Algorithm<DoubleSolution> algorithm;
    DifferentialEvolutionSelection selection;
    DifferentialEvolutionCrossover crossover;
    SolutionListEvaluator<DoubleSolution> evaluator;

    problem = new Sphere(20);

    int numberOfCores;
    if (args.length == 1) {
      numberOfCores = Integer.valueOf(args[0]);
    } else {
      numberOfCores = DEFAULT_NUMBER_OF_CORES;
    }

    if (numberOfCores == 1) {
      evaluator = new SequentialSolutionListEvaluator<DoubleSolution>();
    } else {
      evaluator = new MultiThreadedSolutionListEvaluator<DoubleSolution>(numberOfCores);
    }

    crossover =
        new DifferentialEvolutionCrossover(
            0.5, 0.5, DifferentialEvolutionCrossover.DE_VARIANT.RAND_1_BIN);
    selection = new DifferentialEvolutionSelection();

    algorithm =
        new DifferentialEvolutionBuilder(problem)
            .setCrossover(crossover)
            .setSelection(selection)
            .setSolutionListEvaluator(evaluator)
            .setMaxEvaluations(25000)
            .setPopulationSize(100)
            .build();

    AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();

    DoubleSolution solution = algorithm.result();
    long computingTime = algorithmRunner.getComputingTime();

    List<DoubleSolution> population = new ArrayList<>(1);
    population.add(solution);
    new SolutionListOutput(population)
        .setVarFileOutputContext(new DefaultFileOutputContext("VAR.tsv"))
        .setFunFileOutputContext(new DefaultFileOutputContext("FUN.tsv"))
        .print();

    JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");
    JMetalLogger.logger.info("Objectives values have been written to file FUN.tsv");
    JMetalLogger.logger.info("Variables values have been written to file VAR.tsv");

    JMetalLogger.logger.info("Fitness: " + solution.objectives()[0]);

    evaluator.shutdown();
  }
}
