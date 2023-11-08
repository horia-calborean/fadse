package jmetal.algorithm.multiobjective.pesa2;

import jmetal.core.algorithm.AlgorithmBuilder;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.problem.Problem;
import jmetal.core.solution.Solution;
import jmetal.core.util.errorchecking.JMetalException;
import jmetal.core.util.evaluator.SolutionListEvaluator;
import jmetal.core.util.evaluator.impl.SequentialSolutionListEvaluator;

/**
 * Created by Antonio J. Nebro
 */
public class PESA2Builder<S extends Solution<?>> implements AlgorithmBuilder<PESA2<S>> {
  private final Problem<S> problem;
  private int maxEvaluations ;
  private int archiveSize ;
  private int populationSize ;
  private int biSections ;
  private CrossoverOperator<S> crossoverOperator;
  private MutationOperator<S> mutationOperator;
  private SolutionListEvaluator<S> evaluator;

  /**
   * Constructor
   */
  public PESA2Builder(Problem<S> problem, CrossoverOperator<S> crossoverOperator,
      MutationOperator<S> mutationOperator) {
    this.problem = problem;
    maxEvaluations = 25000;
    populationSize = 100;
    archiveSize = 100 ;
    biSections = 5 ;
    this.crossoverOperator = crossoverOperator ;
    this.mutationOperator = mutationOperator ;

    evaluator = new SequentialSolutionListEvaluator<S>();
  }

  public PESA2Builder<S> setMaxEvaluations(int maxEvaluations) {
    if (maxEvaluations < 0) {
      throw new JMetalException("maxEvaluations is negative: " + maxEvaluations);
    }
    this.maxEvaluations = maxEvaluations;

    return this;
  }

  public PESA2Builder<S> setArchiveSize(int archiveSize) {
    if (archiveSize < 0) {
      throw new JMetalException("archiveSize is negative: " + maxEvaluations);
    }
    this.archiveSize = archiveSize;

    return this;
  }

  public PESA2Builder<S> setBisections(int biSections) {
    if (biSections < 0) {
      throw new JMetalException("biSections is negative: " + maxEvaluations);
    }
    this.biSections = biSections;

    return this;
  }

  public PESA2Builder<S> setPopulationSize(int populationSize) {
    if (populationSize < 0) {
      throw new JMetalException("Population size is negative: " + populationSize);
    }

    this.populationSize = populationSize;

    return this;
  }

  public PESA2Builder<S> setSolutionListEvaluator(SolutionListEvaluator<S> evaluator) {
    if (evaluator == null) {
      throw new JMetalException("evaluator is null");
    }
    this.evaluator = evaluator;

    return this;
  }

  public PESA2<S> build() {
    PESA2<S> algorithm  ;
    algorithm = new PESA2<S>(problem, maxEvaluations, populationSize, archiveSize, biSections,
        crossoverOperator, mutationOperator, evaluator);
    
    return algorithm ;
  }

  /* Getters */
  public Problem<S> getProblem() {
    return problem;
  }

  public int getMaxEvaluations() {
    return maxEvaluations;
  }

  public int getPopulationSize() {
    return populationSize;
  }

  public CrossoverOperator<S> getCrossoverOperator() {
    return crossoverOperator;
  }

  public MutationOperator<S> getMutationOperator() {
    return mutationOperator;
  }

  public SolutionListEvaluator<S> getSolutionListEvaluator() {
    return evaluator;
  }

  public int getBiSections() {
    return biSections ;
  }

  public int getArchiveSize() {
    return archiveSize ;
  }
}
