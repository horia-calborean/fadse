package jmetal.algorithm.multiobjective.agemoeaii;

import java.util.List;
import jmetal.algorithm.multiobjective.agemoea.AGEMOEABuilder;
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
public class AGEMOEAIIBuilder<S extends Solution<?>> extends AGEMOEABuilder<S> {

  protected SolutionListEvaluator<S> evaluator ;

  /** Builder constructor */
  public AGEMOEAIIBuilder(Problem<S> problem) {
    super(problem);
    this.problem = problem ;
    maxIterations = 250 ;
    populationSize = 100 ;
    evaluator = new SequentialSolutionListEvaluator<S>() ;
    selectionOperator = new BinaryTournamentSelection<>(new SurvivalScoreComparator<>());
  }

  public AGEMOEAIIBuilder<S> setMaxIterations(int maxIterations) {
    this.maxIterations = maxIterations ;

    return this ;
  }

  public AGEMOEAIIBuilder<S> setPopulationSize(int populationSize) {
    this.populationSize = populationSize ;

    return this ;
  }

  public AGEMOEAIIBuilder<S> setCrossoverOperator(CrossoverOperator<S> crossoverOperator) {
    this.crossoverOperator = crossoverOperator ;

    return this ;
  }

  public AGEMOEAIIBuilder<S> setMutationOperator(MutationOperator<S> mutationOperator) {
    this.mutationOperator = mutationOperator ;

    return this ;
  }

  public AGEMOEAIIBuilder<S> setSolutionListEvaluator(SolutionListEvaluator<S> evaluator) {
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

  public AGEMOEAII<S> build() {
    return new AGEMOEAII(this) ;
  }
}
