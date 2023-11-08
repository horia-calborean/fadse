package jmetal.algorithm.singleobjective.geneticalgorithm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import jmetal.core.algorithm.impl.AbstractGeneticAlgorithm;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.operator.selection.SelectionOperator;
import jmetal.core.problem.Problem;
import jmetal.core.solution.Solution;
import jmetal.core.util.comparator.ObjectiveComparator;

/**
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class SteadyStateGeneticAlgorithm<S extends Solution<?>> extends AbstractGeneticAlgorithm<S, S> {
  private Comparator<S> comparator;
  private int maxEvaluations;
  private int evaluations;

  /**
   * Constructor
   */
  public SteadyStateGeneticAlgorithm(Problem<S> problem, int maxEvaluations, int populationSize,
                                     CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
                                     SelectionOperator<List<S>, S> selectionOperator) {
    super(problem);
    setMaxPopulationSize(populationSize);
    this.maxEvaluations = maxEvaluations;

    this.crossoverOperator = crossoverOperator;
    this.mutationOperator = mutationOperator;
    this.selectionOperator = selectionOperator;

    comparator = new ObjectiveComparator<S>(0);
  }

  @Override protected boolean isStoppingConditionReached() {
    return (evaluations >= maxEvaluations);
  }

  @Override protected List<S> replacement(List<S> population, List<S> offspringPopulation) {
    population.sort(comparator);
    int worstSolutionIndex = population.size() - 1;
    if (comparator.compare(population.get(worstSolutionIndex), offspringPopulation.get(0)) > 0) {
      population.remove(worstSolutionIndex);
      population.add(offspringPopulation.get(0));
    }

    return population;
  }

  @Override protected List<S> reproduction(List<S> matingPopulation) {
    List<S> offspringPopulation = new ArrayList<>(1);

    List<S> parents = new ArrayList<>(2);
    parents.add(matingPopulation.get(0));
    parents.add(matingPopulation.get(1));

    List<S> offspring = crossoverOperator.execute(parents);
    mutationOperator.execute(offspring.get(0));

    offspringPopulation.add(offspring.get(0));
    return offspringPopulation;
  }

  @Override protected List<S> selection(List<S> population) {
    List<S> matingPopulation = new ArrayList<>(2);
    for (int i = 0; i < 2; i++) {
      S solution = selectionOperator.execute(population);
      matingPopulation.add(solution);
    }

    return matingPopulation;
  }

  @Override protected List<S> evaluatePopulation(List<S> population) {
    for (S solution : population) {
      getProblem().evaluate(solution);
    }

    return population;
  }

  @Override public S result() {
    getPopulation().sort(comparator);
    return getPopulation().get(0);
  }

  @Override public void initProgress() {
    evaluations = getMaxPopulationSize();
  }

  @Override public void updateProgress() {
    evaluations++;
  }

  @Override public String name() {
    return "ssGA" ;
  }

  @Override public String description() {
    return "Steady-State Genetic Algorithm" ;
  }
}
