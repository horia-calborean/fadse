package jmetal.algorithm.singleobjective.geneticalgorithm;

import java.util.Comparator;
import java.util.List;
import jmetal.core.algorithm.impl.AbstractGeneticAlgorithm;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.operator.selection.SelectionOperator;
import jmetal.core.problem.Problem;
import jmetal.core.solution.Solution;
import jmetal.core.util.comparator.ObjectiveComparator;
import jmetal.core.util.evaluator.SolutionListEvaluator;

/**
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class GenerationalGeneticAlgorithm<S extends Solution<?>> extends AbstractGeneticAlgorithm<S, S> {
  private Comparator<S> comparator;
  private int maxEvaluations;
  private int evaluations;

  private SolutionListEvaluator<S> evaluator;

  /**
   * Constructor
   */
  public GenerationalGeneticAlgorithm(Problem<S> problem, int maxEvaluations, int populationSize,
                                      CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
                                      SelectionOperator<List<S>, S> selectionOperator, SolutionListEvaluator<S> evaluator) {
    super(problem);
    this.maxEvaluations = maxEvaluations;
    this.setMaxPopulationSize(populationSize);

    this.crossoverOperator = crossoverOperator;
    this.mutationOperator = mutationOperator;
    this.selectionOperator = selectionOperator;

    this.evaluator = evaluator;

    comparator = new ObjectiveComparator<S>(0);
  }

  @Override protected boolean isStoppingConditionReached() {
    return (evaluations >= maxEvaluations);
  }

  @Override protected List<S> replacement(List<S> population, List<S> offspringPopulation) {
    population.sort(comparator);
    offspringPopulation.add(population.get(0));
    offspringPopulation.add(population.get(1));
    offspringPopulation.sort(comparator);
    offspringPopulation.remove(offspringPopulation.size() - 1);
    offspringPopulation.remove(offspringPopulation.size() - 1);

    return offspringPopulation;
  }

  @Override protected List<S> evaluatePopulation(List<S> population) {
    population = evaluator.evaluate(population, getProblem());

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
    evaluations += getMaxPopulationSize();
  }

  @Override public String name() {
    return "gGA" ;
  }

  @Override public String description() {
    return "Generational Genetic Algorithm" ;
  }
}
