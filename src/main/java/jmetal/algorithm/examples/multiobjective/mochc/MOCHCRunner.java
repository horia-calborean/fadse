package jmetal.algorithm.examples.multiobjective.mochc;

import java.util.List;
import jmetal.core.algorithm.Algorithm;
import jmetal.algorithm.examples.AlgorithmRunner;
import jmetal.algorithm.multiobjective.mochc.MOCHCBuilder;
import jmetal.core.operator.crossover.impl.HUXCrossover;
import jmetal.core.operator.mutation.impl.BitFlipMutation;
import jmetal.core.operator.selection.SelectionOperator;
import jmetal.core.operator.selection.impl.RandomSelection;
import jmetal.core.operator.selection.impl.RankingAndCrowdingSelection;
import jmetal.problem.ProblemFactory;
import jmetal.core.problem.binaryproblem.BinaryProblem;
import jmetal.core.solution.binarysolution.BinarySolution;
import jmetal.core.util.AbstractAlgorithmRunner;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.evaluator.impl.SequentialSolutionListEvaluator;

/**
 * This class executes the algorithm described in: A.J. Nebro, E. Alba, G. Molina, F. Chicano, F.
 * Luna, J.J. Durillo "Optimal antenna placement using a new multi-objective chc algorithm". GECCO
 * '07: Proceedings of the 9th annual conference on Genetic and evolutionary computation. London,
 * England. July 2007.
 */
public class MOCHCRunner extends AbstractAlgorithmRunner {

  public static void main(String[] args) throws Exception {
    String problemName = "jmetal.core.problem.multiobjective.zdt.ZDT5";
    BinaryProblem problem = (BinaryProblem) ProblemFactory.<BinarySolution>loadProblem(problemName);

    var crossoverOperator = new HUXCrossover(1.0);
    SelectionOperator<List<BinarySolution>, BinarySolution> parentsSelection = new RandomSelection<>();
    SelectionOperator<List<BinarySolution>, List<BinarySolution>> newGenerationSelection = new RankingAndCrowdingSelection<>(
        100);
    var mutationOperator = new BitFlipMutation(0.35);

    Algorithm<List<BinarySolution>> algorithm = new MOCHCBuilder(problem)
        .setInitialConvergenceCount(0.25)
        .setConvergenceValue(3)
        .setPreservedPopulation(0.05)
        .setPopulationSize(100)
        .setMaxEvaluations(25000)
        .setCrossover(crossoverOperator)
        .setNewGenerationSelection(newGenerationSelection)
        .setCataclysmicMutation(mutationOperator)
        .setParentSelection(parentsSelection)
        .setEvaluator(new SequentialSolutionListEvaluator<>())
        .build();

    AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm)
        .execute();

    List<BinarySolution> population = algorithm.result();
    long computingTime = algorithmRunner.getComputingTime();

    JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");

    printFinalSolutionSet(population);
  }
}
