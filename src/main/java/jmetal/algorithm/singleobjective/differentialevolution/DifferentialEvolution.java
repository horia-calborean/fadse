package jmetal.algorithm.singleobjective.differentialevolution;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import jmetal.core.algorithm.impl.AbstractDifferentialEvolution;
import jmetal.core.operator.crossover.impl.DifferentialEvolutionCrossover;
import jmetal.core.operator.selection.impl.DifferentialEvolutionSelection;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.comparator.ObjectiveComparator;
import jmetal.core.util.evaluator.SolutionListEvaluator;

/**
 * This class implements a differential evolution algorithm.
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class DifferentialEvolution extends AbstractDifferentialEvolution<DoubleSolution> {
  protected DifferentialEvolutionCrossover crossoverOperator ;
  protected DifferentialEvolutionSelection selectionOperator ;

  private int populationSize;
  private int maxEvaluations;
  private SolutionListEvaluator<DoubleSolution> evaluator;
  private Comparator<DoubleSolution> comparator;

  private int evaluations;

  /**
   * Constructor
   *
   * @param problem Problem to solve
   * @param maxEvaluations Maximum number of evaluations to perform
   * @param populationSize
   * @param crossoverOperator
   * @param selectionOperator
   * @param evaluator
   */
  public DifferentialEvolution(DoubleProblem problem, int maxEvaluations, int populationSize,
                               DifferentialEvolutionCrossover crossoverOperator,
                               DifferentialEvolutionSelection selectionOperator, SolutionListEvaluator<DoubleSolution> evaluator) {
    setProblem(problem); ;
    this.maxEvaluations = maxEvaluations;
    this.populationSize = populationSize;
    this.crossoverOperator = crossoverOperator;
    this.selectionOperator = selectionOperator;
    this.evaluator = evaluator;

    comparator = new ObjectiveComparator<DoubleSolution>(0);
  }
  
  public int getEvaluations() {
    return evaluations;
  }

  public void setEvaluations(int evaluations) {
    this.evaluations = evaluations;
  }

  @Override protected void initProgress() {
    evaluations = populationSize;
  }

  @Override protected void updateProgress() {
    evaluations += populationSize;
  }

  @Override protected boolean isStoppingConditionReached() {
    return evaluations >= maxEvaluations;
  }

  @Override protected List<DoubleSolution> createInitialPopulation() {
    List<DoubleSolution> population = new ArrayList<>(populationSize);
    for (int i = 0; i < populationSize; i++) {
      DoubleSolution newIndividual = getProblem().createSolution();
      population.add(newIndividual);
    }
    return population;
  }

  @Override protected List<DoubleSolution> evaluatePopulation(List<DoubleSolution> population) {
    return evaluator.evaluate(population, getProblem());
  }

  @Override protected List<DoubleSolution> selection(List<DoubleSolution> population) {
    return population;
  }

  @Override protected List<DoubleSolution> reproduction(List<DoubleSolution> matingPopulation) {
    List<DoubleSolution> offspringPopulation = new ArrayList<>();

    for (int i = 0; i < populationSize; i++) {
      selectionOperator.setIndex(i);
      List<DoubleSolution> parents = selectionOperator.execute(matingPopulation);

      crossoverOperator.setCurrentSolution(matingPopulation.get(i));
      List<DoubleSolution> children = crossoverOperator.execute(parents);

      offspringPopulation.add(children.get(0));
    }

    return offspringPopulation;
  }

  @Override protected List<DoubleSolution> replacement(List<DoubleSolution> population,
      List<DoubleSolution> offspringPopulation) {
    List<DoubleSolution> pop = new ArrayList<>();

    for (int i = 0; i < populationSize; i++) {
      if (comparator.compare(population.get(i), offspringPopulation.get(i)) < 0) {
        pop.add(population.get(i));
      } else {
        pop.add(offspringPopulation.get(i));
      }
    }

    pop.sort(comparator);
    return pop;
  }

  /**
   * Returns the best individual
   */
  @Override public DoubleSolution result() {
    getPopulation().sort(comparator);

    return getPopulation().get(0);
  }

  @Override public String name() {
    return "DE" ;
  }

  @Override public String description() {
    return "Differential Evolution Algorithm" ;
  }

  @Override
  public DifferentialEvolutionCrossover getCrossoverOperator() {
    return crossoverOperator ;
  }

  @Override
  public  DifferentialEvolutionSelection getSelectionOperator() {
    return selectionOperator ;
  }
}
