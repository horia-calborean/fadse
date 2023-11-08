package jmetal.algorithm.multiobjective.smsemoa;

import java.util.Comparator;
import java.util.List;
import jmetal.core.algorithm.AlgorithmBuilder;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.operator.selection.SelectionOperator;
import jmetal.core.operator.selection.impl.RandomSelection;
import jmetal.core.problem.Problem;
import jmetal.core.solution.Solution;
import jmetal.core.util.comparator.dominanceComparator.impl.DominanceWithConstraintsComparator;
import jmetal.core.util.errorchecking.JMetalException;
import jmetal.core.util.legacy.qualityindicator.impl.hypervolume.Hypervolume;
import jmetal.core.util.legacy.qualityindicator.impl.hypervolume.impl.PISAHypervolume;

/**
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class SMSEMOABuilder<S extends Solution<?>> implements AlgorithmBuilder<SMSEMOA<S>> {
  private static final double DEFAULT_OFFSET = 100.0 ;

  protected Problem<S> problem;

  protected int populationSize;
  protected int maxEvaluations;

  protected CrossoverOperator<S> crossoverOperator;
  protected MutationOperator<S> mutationOperator;
  protected SelectionOperator<List<S>, S> selectionOperator;

  protected double offset ;

  protected Hypervolume<S> hypervolumeImplementation;
  protected Comparator<S> dominanceComparator ;

  public SMSEMOABuilder(Problem<S> problem, CrossoverOperator<S> crossoverOperator,
      MutationOperator<S> mutationOperator) {
    this.problem = problem ;
    this.offset = DEFAULT_OFFSET ;
    populationSize = 100 ;
    maxEvaluations = 25000 ;
    this.hypervolumeImplementation = new PISAHypervolume<>() ;
    hypervolumeImplementation.setOffset(offset);

    this.crossoverOperator = crossoverOperator ;
    this.mutationOperator = mutationOperator ;
    this.selectionOperator = new RandomSelection<S>() ;
    this.dominanceComparator = new DominanceWithConstraintsComparator<>()  ;
  }

  public SMSEMOABuilder<S> setPopulationSize(int populationSize) {
    this.populationSize = populationSize ;

    return this ;
  }

  public SMSEMOABuilder<S> setMaxEvaluations(int maxEvaluations) {
    this.maxEvaluations = maxEvaluations ;

    return this ;
  }

  public SMSEMOABuilder<S> setCrossoverOperator(CrossoverOperator<S> crossover) {
    crossoverOperator = crossover ;

    return this ;
  }

  public SMSEMOABuilder<S> setMutationOperator(MutationOperator<S> mutation) {
    mutationOperator = mutation ;

    return this ;
  }

  public SMSEMOABuilder<S> setSelectionOperator(SelectionOperator<List<S>, S> selection) {
    selectionOperator = selection ;

    return this ;
  }

  public SMSEMOABuilder<S> setHypervolumeImplementation(Hypervolume<S> hypervolumeImplementation) {
    this.hypervolumeImplementation = hypervolumeImplementation;

    return this ;
  }


  public SMSEMOABuilder<S> setOffset(double offset) {
    this.offset = offset ;

    return this ;
  }

  public SMSEMOABuilder<S> setDominanceComparator(Comparator<S> dominanceComparator) {
    if (dominanceComparator == null) {
      throw new JMetalException("dominanceComparator is null");
    }
    this.dominanceComparator = dominanceComparator ;

    return this;
  }

  @Override public SMSEMOA<S> build() {
    return new SMSEMOA<S>(problem, maxEvaluations, populationSize, offset,
        crossoverOperator, mutationOperator, selectionOperator, dominanceComparator,
        hypervolumeImplementation);
  }

  /*
   * Getters
   */

  public Problem<S> getProblem() {
    return problem;
  }

  public int getPopulationSize() {
    return populationSize;
  }

  public int getMaxEvaluations() {
    return maxEvaluations;
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

  public double getOffset() {
    return offset;
  }
}