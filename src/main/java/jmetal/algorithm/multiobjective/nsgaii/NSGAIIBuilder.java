package jmetal.algorithm.multiobjective.nsgaii;

import java.util.List;
import jmetal.core.algorithm.AlgorithmBuilder;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.operator.selection.SelectionOperator;
import jmetal.core.operator.selection.impl.BinaryTournamentSelection;
import jmetal.core.problem.Problem;
import jmetal.core.solution.Solution;
import jmetal.core.util.comparator.RankingAndCrowdingDistanceComparator;
import jmetal.core.util.comparator.dominanceComparator.DominanceComparator;
import jmetal.core.util.comparator.dominanceComparator.impl.DefaultDominanceComparator;
import jmetal.core.util.errorchecking.Check;
import jmetal.core.util.errorchecking.JMetalException;
import jmetal.core.util.evaluator.SolutionListEvaluator;
import jmetal.core.util.evaluator.impl.SequentialSolutionListEvaluator;

/**
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class NSGAIIBuilder<S extends Solution<?>> implements AlgorithmBuilder<NSGAII<S>> {
  public enum NSGAIIVariant {NSGAII, SteadyStateNSGAII, Measures, NSGAII45, DNSGAII}

  /**
   * NSGAIIBuilder class
   */
  private final Problem<S> problem;
  private int maxEvaluations;
  private int populationSize;
  protected int matingPoolSize;
  protected int offspringPopulationSize ;

  private CrossoverOperator<S> crossoverOperator;
  private MutationOperator<S> mutationOperator;
  private SelectionOperator<List<S>, S> selectionOperator;
  private SolutionListEvaluator<S> evaluator;
  private DominanceComparator<S> dominanceComparator ;

  private NSGAIIVariant variant;

  /**
   * NSGAIIBuilder constructor
   */
  public NSGAIIBuilder(Problem<S> problem, CrossoverOperator<S> crossoverOperator,
      MutationOperator<S> mutationOperator, int populationSize) {
    this.problem = problem;
    maxEvaluations = 25000;
    this.populationSize = populationSize;
    matingPoolSize = populationSize;
    offspringPopulationSize = populationSize ;
    this.crossoverOperator = crossoverOperator ;
    this.mutationOperator = mutationOperator ;
    selectionOperator = new BinaryTournamentSelection<S>(new RankingAndCrowdingDistanceComparator<S>()) ;
    evaluator = new SequentialSolutionListEvaluator<S>();
    dominanceComparator = new DefaultDominanceComparator<>()  ;

    this.variant = NSGAIIVariant.NSGAII ;
  }

  public NSGAIIBuilder<S> setMaxEvaluations(int maxEvaluations) {
    if (maxEvaluations < 0) {
      throw new JMetalException("maxEvaluations is negative: " + maxEvaluations);
    }
    this.maxEvaluations = maxEvaluations;

    return this;
  }

  public NSGAIIBuilder<S> setMatingPoolSize(int matingPoolSize) {
    if (matingPoolSize < 0) {
      throw new JMetalException("The mating pool size is negative: " + populationSize);
    }

    this.matingPoolSize = matingPoolSize;

    return this;
  }
  public NSGAIIBuilder<S> setOffspringPopulationSize(int offspringPopulationSize) {
    if (offspringPopulationSize < 0) {
      throw new JMetalException("Offspring population size is negative: " + populationSize);
    }

    this.offspringPopulationSize = offspringPopulationSize;

    return this;
  }

  public NSGAIIBuilder<S> setSelectionOperator(SelectionOperator<List<S>, S> selectionOperator) {
    if (selectionOperator == null) {
      throw new JMetalException("selectionOperator is null");
    }
    this.selectionOperator = selectionOperator;

    return this;
  }

  public NSGAIIBuilder<S> setSolutionListEvaluator(SolutionListEvaluator<S> evaluator) {
    if (evaluator == null) {
      throw new JMetalException("evaluator is null");
    }
    this.evaluator = evaluator;

    return this;
  }

  public NSGAIIBuilder<S> setDominanceComparator(DominanceComparator<S> dominanceComparator) {
    Check.notNull(dominanceComparator);
    this.dominanceComparator = dominanceComparator ;

    return this;
  }


  public NSGAIIBuilder<S> setVariant(NSGAIIVariant variant) {
    this.variant = variant;

    return this;
  }

  public NSGAII<S> build() {
    NSGAII<S> algorithm = null ;
    if (variant.equals(NSGAIIVariant.NSGAII)) {
      algorithm = new NSGAII<S>(problem, maxEvaluations, populationSize, matingPoolSize, offspringPopulationSize,
              crossoverOperator,
          mutationOperator, selectionOperator, dominanceComparator, evaluator);
    } else if (variant.equals(NSGAIIVariant.SteadyStateNSGAII)) {
      algorithm = new SteadyStateNSGAII<S>(problem, maxEvaluations, populationSize, crossoverOperator,
          mutationOperator, selectionOperator, dominanceComparator, evaluator);
    } else if (variant.equals(NSGAIIVariant.Measures)) {
      algorithm = new NSGAIIMeasures<S>(problem, maxEvaluations, populationSize, matingPoolSize, offspringPopulationSize,
              crossoverOperator, mutationOperator, selectionOperator, dominanceComparator, evaluator);
    }else if(variant.equals(NSGAIIVariant.DNSGAII)){
      algorithm = new DNSGAII<>(problem, maxEvaluations, populationSize, matingPoolSize, offspringPopulationSize,
              crossoverOperator, mutationOperator, selectionOperator, dominanceComparator, evaluator) ;
    }

    return algorithm ;
  }

  /* Getters */
  public Problem<S> getProblem() {
    return problem;
  }

  public int getMaxIterations() {
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

  public SelectionOperator<List<S>, S> getSelectionOperator() {
    return selectionOperator;
  }

  public SolutionListEvaluator<S> getSolutionListEvaluator() {
    return evaluator;
  }
}
