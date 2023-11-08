package jmetal.algorithm.multiobjective.gde3;

import java.util.List;
import jmetal.core.algorithm.AlgorithmBuilder;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.crossover.impl.DifferentialEvolutionCrossover;
import jmetal.core.operator.selection.SelectionOperator;
import jmetal.core.operator.selection.impl.DifferentialEvolutionSelection;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.evaluator.SolutionListEvaluator;
import jmetal.core.util.evaluator.impl.SequentialSolutionListEvaluator;

/**
 * This class implements the GDE3 algorithm
 */
public class GDE3Builder implements AlgorithmBuilder<GDE3> {
  private DoubleProblem problem;
  protected int populationSize;
  protected int maxEvaluations;

  protected DifferentialEvolutionCrossover crossoverOperator;
  protected DifferentialEvolutionSelection selectionOperator;

  protected SolutionListEvaluator<DoubleSolution> evaluator;

  /** Constructor */
  public GDE3Builder(DoubleProblem problem) {
    this.problem = problem;
    maxEvaluations = 25000 ;
    populationSize = 100 ;
    selectionOperator = new DifferentialEvolutionSelection();
    crossoverOperator = new DifferentialEvolutionCrossover() ;
    evaluator = new SequentialSolutionListEvaluator<DoubleSolution>() ;
  }

  /* Setters */
  public GDE3Builder setPopulationSize(int populationSize) {
    this.populationSize = populationSize;

    return this;
  }

  public GDE3Builder setMaxEvaluations(int maxEvaluations) {
    this.maxEvaluations = maxEvaluations;

    return this;
  }

  public GDE3Builder setCrossover(DifferentialEvolutionCrossover crossover) {
    crossoverOperator = crossover;

    return this;
  }

  public GDE3Builder setSelection(DifferentialEvolutionSelection selection) {
    selectionOperator = selection;

    return this;
  }

  public GDE3Builder setSolutionSetEvaluator(SolutionListEvaluator<DoubleSolution> evaluator) {
    this.evaluator = evaluator ;

    return this ;
  }

  public GDE3 build() {
    return new GDE3(problem, populationSize, maxEvaluations, selectionOperator, crossoverOperator, evaluator) ;
  }

  /* Getters */
  public CrossoverOperator<DoubleSolution> getCrossoverOperator() {
    return crossoverOperator;
  }

  public SelectionOperator<List<DoubleSolution>, List<DoubleSolution>> getSelectionOperator() {
    return selectionOperator;
  }

  public int getPopulationSize() {
    return populationSize;
  }

  public int getMaxEvaluations() {
    return maxEvaluations;
  }

}

