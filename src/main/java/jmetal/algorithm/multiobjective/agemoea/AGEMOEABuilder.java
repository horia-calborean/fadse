package jmetal.algorithm.multiobjective.agemoea;

import java.util.List;
import jmetal.core.algorithm.AlgorithmBuilder;
import jmetal.algorithm.multiobjective.agemoea.util.SurvivalScoreComparator;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.operator.selection.SelectionOperator;
import jmetal.core.operator.selection.impl.BinaryTournamentSelection;
import jmetal.core.problem.Problem;
import jmetal.core.solution.Solution;
import jmetal.core.util.evaluator.SolutionListEvaluator;
import jmetal.core.util.evaluator.impl.SequentialSolutionListEvaluator;


/**
 * Builder class for AGE-MOEA
 *
 * @author Annibale Panichella
 * @version 1.0
 */
public class AGEMOEABuilder<S extends Solution<?>> implements AlgorithmBuilder<AGEMOEA<S>>{
  // no access modifier means access from classes within the same package
  protected Problem<S> problem ;
  protected int maxIterations ;
  protected int populationSize ;
  protected CrossoverOperator<S> crossoverOperator ;
  protected MutationOperator<S> mutationOperator ;
  protected SelectionOperator<List<S>, S> selectionOperator ;

  protected SolutionListEvaluator<S> evaluator ;

  /** Builder constructor */
  public AGEMOEABuilder(Problem<S> problem) {
    this.problem = problem ;
    maxIterations = 250 ;
    populationSize = 100 ;
    evaluator = new SequentialSolutionListEvaluator<S>() ;
    selectionOperator = new BinaryTournamentSelection<>(new SurvivalScoreComparator<>());
  }

  public AGEMOEABuilder<S> setMaxIterations(int maxIterations) {
    this.maxIterations = maxIterations ;

    return this ;
  }

  public AGEMOEABuilder<S> setPopulationSize(int populationSize) {
    this.populationSize = populationSize ;

    return this ;
  }

  public AGEMOEABuilder<S> setCrossoverOperator(CrossoverOperator<S> crossoverOperator) {
    this.crossoverOperator = crossoverOperator ;

    return this ;
  }

  public AGEMOEABuilder<S> setMutationOperator(MutationOperator<S> mutationOperator) {
    this.mutationOperator = mutationOperator ;

    return this ;
  }

  public AGEMOEABuilder<S> setSolutionListEvaluator(SolutionListEvaluator<S> evaluator) {
    this.evaluator = evaluator ;

    return this ;
  }

  public SolutionListEvaluator<S> getEvaluator() {
    return evaluator;
  }

  public Problem<S> getProblem() {
    return problem;
  }

  public int getMaxIterations() {
    return maxIterations;
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

  public AGEMOEA<S> build() {
    return new AGEMOEA<>(this) ;
  }
}
