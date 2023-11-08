package jmetal.algorithm.singleobjective.differentialevolution;

import jmetal.core.operator.crossover.impl.DifferentialEvolutionCrossover;
import jmetal.core.operator.selection.impl.DifferentialEvolutionSelection;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.errorchecking.JMetalException;
import jmetal.core.util.evaluator.SolutionListEvaluator;
import jmetal.core.util.evaluator.impl.SequentialSolutionListEvaluator;

/**
 * DifferentialEvolutionBuilder class
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class DifferentialEvolutionBuilder {
  private DoubleProblem problem;
  private int populationSize;
  private int maxEvaluations;
  private DifferentialEvolutionCrossover crossoverOperator;
  private DifferentialEvolutionSelection selectionOperator;
  private SolutionListEvaluator<DoubleSolution> evaluator;

  public DifferentialEvolutionBuilder(DoubleProblem problem) {
    this.problem = problem;
    this.populationSize = 100;
    this.maxEvaluations = 25000;
    this.crossoverOperator = new DifferentialEvolutionCrossover(0.5, 0.5, DifferentialEvolutionCrossover.DE_VARIANT.RAND_1_BIN);
    this.selectionOperator = new DifferentialEvolutionSelection();
    this.evaluator = new SequentialSolutionListEvaluator<DoubleSolution>();
  }

  public DifferentialEvolutionBuilder setPopulationSize(int populationSize) {
    if (populationSize < 0) {
      throw new JMetalException("Population size is negative: " + populationSize);
    }

    this.populationSize = populationSize;

    return this;
  }

  public DifferentialEvolutionBuilder setMaxEvaluations(int maxEvaluations) {
    if (maxEvaluations < 0) {
      throw new JMetalException("MaxEvaluations is negative: " + maxEvaluations);
    }

    this.maxEvaluations = maxEvaluations;

    return this;
  }

  public DifferentialEvolutionBuilder setCrossover(DifferentialEvolutionCrossover crossover) {
    this.crossoverOperator = crossover;

    return this;
  }

  public DifferentialEvolutionBuilder setSelection(DifferentialEvolutionSelection selection) {
    this.selectionOperator = selection;

    return this;
  }

  public DifferentialEvolutionBuilder setSolutionListEvaluator(SolutionListEvaluator<DoubleSolution> evaluator) {
    this.evaluator = evaluator;

    return this;
  }

  public DifferentialEvolution build() {
    return new DifferentialEvolution(problem, maxEvaluations, populationSize, crossoverOperator,
        selectionOperator, evaluator);
  }

  /* Getters */
  public DoubleProblem getProblem() {
    return problem;
  }

  public int getPopulationSize() {
    return populationSize;
  }

  public int getMaxEvaluations() {
    return maxEvaluations;
  }

  public DifferentialEvolutionCrossover getCrossoverOperator() {
    return crossoverOperator;
  }

  public DifferentialEvolutionSelection getSelectionOperator() {
    return selectionOperator;
  }

  public SolutionListEvaluator<DoubleSolution> getSolutionListEvaluator() {
    return evaluator;
  }
}

