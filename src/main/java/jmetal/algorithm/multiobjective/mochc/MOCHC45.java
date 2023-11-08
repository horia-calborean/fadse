package jmetal.algorithm.multiobjective.mochc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import jmetal.core.algorithm.Algorithm;
import jmetal.core.algorithm.impl.AbstractGeneticAlgorithm;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.operator.selection.SelectionOperator;
import jmetal.core.problem.binaryproblem.BinaryProblem;
import jmetal.core.solution.binarysolution.BinarySolution;
import jmetal.core.util.ListUtils;
import jmetal.core.util.archive.impl.NonDominatedSolutionListArchive;
import jmetal.core.util.binarySet.BinarySet;
import jmetal.core.util.densityestimator.impl.CrowdingDistanceDensityEstimator;
import jmetal.core.util.errorchecking.JMetalException;
import jmetal.core.util.evaluator.SolutionListEvaluator;

/**
 * This class executes the MOCHC algorithm described in: A.J. Nebro, E. Alba, G. Molina, F. Chicano,
 * F. Luna, J.J. Durillo "Optimal antenna placement using a new multi-objective chc algorithm".
 * GECCO '07: Proceedings of the 9th annual conference on Genetic and evolutionary computation.
 * London, England. July 2007.
 * <p>
 * Implementation of MOCHC following the scheme used in jMetal4.5 and former versions, i.e, without
 * implementing the {@link AbstractGeneticAlgorithm} interface.
 */
@SuppressWarnings("serial")
public class MOCHC45 implements Algorithm<List<BinarySolution>> {

  private BinaryProblem problem;
  private List<BinarySolution> population;
  private int populationSize;
  private int maxEvaluations;
  private int convergenceValue;
  private double preservedPopulation;
  private double initialConvergenceCount;
  private CrossoverOperator<BinarySolution> crossover;
  private MutationOperator<BinarySolution> cataclysmicMutation;
  private SelectionOperator<List<BinarySolution>, List<BinarySolution>> newGenerationSelection;
  private SelectionOperator<List<BinarySolution>, BinarySolution> parentSelection;
  private int evaluations;
  private int minimumDistance;
  private int size;
  private Comparator<BinarySolution> comparator;

  /**
   * Constructor
   */
  public MOCHC45(BinaryProblem problem, int populationSize, int maxEvaluations,
      int convergenceValue,
      double preservedPopulation, double initialConvergenceCount,
      CrossoverOperator<BinarySolution> crossoverOperator,
      MutationOperator<BinarySolution> cataclysmicMutation,
      SelectionOperator<List<BinarySolution>, List<BinarySolution>> newGenerationSelection,
      SelectionOperator<List<BinarySolution>, BinarySolution> parentSelection,
      SolutionListEvaluator<BinarySolution> evaluator) {
    super();
    this.problem = problem;
    this.populationSize = populationSize;
    this.maxEvaluations = maxEvaluations;
    this.convergenceValue = convergenceValue;
    this.preservedPopulation = preservedPopulation;
    this.initialConvergenceCount = initialConvergenceCount;
    this.crossover = crossoverOperator;
    this.cataclysmicMutation = cataclysmicMutation;
    this.newGenerationSelection = newGenerationSelection;
    this.parentSelection = parentSelection;
  }

  @Override
  public String name() {
    return "MOCHC45";
  }

  @Override
  public String description() {
    return "Multiobjective CHC algorithm";
  }

  @Override
  public void run() {
    for (int i = 0; i < problem.numberOfVariables(); i++) {
      size += problem.bitsFromVariable(i);
    }
    minimumDistance = (int) Math.floor(this.initialConvergenceCount * size);

    comparator = new CrowdingDistanceDensityEstimator<BinarySolution>().comparator();

    evaluations = 0;
    population = new ArrayList<>();
    for (int i = 0; i < populationSize; i++) {
      BinarySolution newIndividual = problem.createSolution();
      problem.evaluate(newIndividual);
      population.add(newIndividual);
      evaluations++;
    }

    boolean finishCondition = false;

    while (!finishCondition) {
      List<BinarySolution> offspringPopulation = new ArrayList<>(populationSize);
      for (int i = 0; i < population.size() / 2; i++) {
        List<BinarySolution> parents = new ArrayList<>(2);
        parents.add(parentSelection.execute(population));
        parents.add(parentSelection.execute(population));

        if (hammingDistance(parents.get(0), parents.get(1)) >= minimumDistance) {
          List<BinarySolution> offspring = crossover.execute(parents);
          problem.evaluate(offspring.get(0));
          problem.evaluate(offspring.get(1));
          offspringPopulation.add(offspring.get(0));
          offspringPopulation.add(offspring.get(1));

          evaluations += 2;
        }
      }

      List<BinarySolution> union = new ArrayList<>();
      union.addAll(population);
      union.addAll(offspringPopulation);

      List<BinarySolution> newPopulation = newGenerationSelection.execute(union);

      if (ListUtils.listAreEquals(population, newPopulation)) {
        minimumDistance--;
      }

      if (minimumDistance <= -convergenceValue) {
        minimumDistance = (int) (1.0 / size * (1 - 1.0 / size) * size);

        int preserve = (int) Math.floor(preservedPopulation * population.size());
        newPopulation = new ArrayList<>(populationSize);
        Collections.sort(population, comparator);
        for (int i = 0; i < preserve; i++) {
          newPopulation.add((BinarySolution) population.get(i).copy());
        }
        for (int i = preserve; i < populationSize; i++) {
          BinarySolution solution = (BinarySolution) population.get(i).copy();
          cataclysmicMutation.execute(solution);
          problem.evaluate(solution);
          //problem.evaluateConstraints(solution);
          newPopulation.add(solution);
          evaluations++;
        }
      }

      population = newPopulation;
      if (evaluations >= maxEvaluations) {
        finishCondition = true;
      }
    }
  }

  @Override
  public List<BinarySolution> result() {
    NonDominatedSolutionListArchive<BinarySolution> archive = new NonDominatedSolutionListArchive<>();
    for (BinarySolution solution : population) {
      archive.add(solution);
    }

    return archive.solutions();
  }

  /**
   * Calculate the hamming distance between two solutions
   *
   * @param solutionOne A <code>Solution</code>
   * @param solutionTwo A <code>Solution</code>
   * @return the hamming distance between solutions
   */

  private int hammingDistance(BinarySolution solutionOne, BinarySolution solutionTwo) {
    int distance = 0;
    for (int i = 0; i < problem.numberOfVariables(); i++) {
      distance += hammingDistance(solutionOne.variables().get(i), solutionTwo.variables().get(i));
    }

    return distance;
  }

  private int hammingDistance(BinarySet bitSet1, BinarySet bitSet2) {
    if (bitSet1.getBinarySetLength() != bitSet2.getBinarySetLength()) {
      throw new JMetalException("The bitsets have different length: "
          + bitSet1.getBinarySetLength() + ", " + bitSet2.getBinarySetLength());
    }
    int distance = 0;
    int i = 0;
    while (i < bitSet1.getBinarySetLength()) {
      if (bitSet1.get(i) != bitSet2.get(i)) {
        distance++;
      }
      i++;
    }

    return distance;
  }

}
