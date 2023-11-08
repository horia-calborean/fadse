package jmetal.algorithm.multiobjective.mocell;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import jmetal.core.algorithm.impl.AbstractGeneticAlgorithm;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.operator.selection.SelectionOperator;
import jmetal.core.problem.Problem;
import jmetal.core.solution.Solution;
import jmetal.core.util.archive.BoundedArchive;
import jmetal.core.util.comparator.RankingAndCrowdingDistanceComparator;
import jmetal.core.util.comparator.dominanceComparator.impl.DominanceWithConstraintsComparator;
import jmetal.core.util.densityestimator.DensityEstimator;
import jmetal.core.util.densityestimator.impl.CrowdingDistanceDensityEstimator;
import jmetal.core.util.evaluator.SolutionListEvaluator;
import jmetal.core.util.neighborhood.Neighborhood;
import jmetal.core.util.ranking.Ranking;
import jmetal.core.util.ranking.impl.FastNonDominatedSortRanking;

/**
 * @param <S>
 * @author JuanJo Durillo
 */
@SuppressWarnings("serial")
public class MOCell<S extends Solution<?>> extends AbstractGeneticAlgorithm<S, List<S>> {
  protected int evaluations;
  protected int maxEvaluations;
  protected final SolutionListEvaluator<S> evaluator;

  protected Neighborhood<S> neighborhood;
  protected int currentIndividual;
  protected List<S> currentNeighbors;

  protected BoundedArchive<S> archive;

  protected Comparator<S> dominanceComparator;

  /**
   * Constructor
   *
   * @param problem
   * @param maxEvaluations
   * @param populationSize
   * @param neighborhood
   * @param crossoverOperator
   * @param mutationOperator
   * @param selectionOperator
   * @param evaluator
   */
  public MOCell(
      Problem<S> problem,
      int maxEvaluations,
      int populationSize,
      BoundedArchive<S> archive,
      Neighborhood<S> neighborhood,
      CrossoverOperator<S> crossoverOperator,
      MutationOperator<S> mutationOperator,
      SelectionOperator<List<S>, S> selectionOperator,
      SolutionListEvaluator<S> evaluator) {
    super(problem);
    this.maxEvaluations = maxEvaluations;
    setMaxPopulationSize(populationSize);
    this.archive = archive;
    this.neighborhood = neighborhood;
    this.crossoverOperator = crossoverOperator;
    this.mutationOperator = mutationOperator;
    this.selectionOperator = selectionOperator;
    this.dominanceComparator = new DominanceWithConstraintsComparator<S>();

    this.evaluator = evaluator;
  }

  @Override
  protected void initProgress() {
    evaluations = 0;
    currentIndividual = 0;
    for (S solution : population) {
      archive.add((S) solution.copy());
    }
  }

  @Override
  protected void updateProgress() {
    evaluations++;
    currentIndividual = (currentIndividual + 1) % getMaxPopulationSize();
  }

  @Override
  protected boolean isStoppingConditionReached() {
    return (evaluations == maxEvaluations);
  }

  @Override
  protected List<S> evaluatePopulation(List<S> population) {
    population = evaluator.evaluate(population, getProblem());

    return population;
  }

  @Override
  protected List<S> selection(List<S> population) {
    List<S> parents = new ArrayList<>(2);
    currentNeighbors = neighborhood.getNeighbors(population, currentIndividual);
    currentNeighbors.add(population.get(currentIndividual));

    parents.add(selectionOperator.execute(currentNeighbors));
    if (archive.size() > 1) {
      parents.add(selectionOperator.execute(archive.solutions()));
    } else {
      parents.add(selectionOperator.execute(currentNeighbors));
    }
    return parents;
  }

  @Override
  protected List<S> reproduction(List<S> population) {
    List<S> result = new ArrayList<>(1);
    List<S> offspring = crossoverOperator.execute(population);
    mutationOperator.execute(offspring.get(0));
    result.add(offspring.get(0));
    return result;
  }

  @Override
  protected List<S> replacement(List<S> population, List<S> offspringPopulation) {
    int flag =
        dominanceComparator.compare(population.get(currentIndividual), offspringPopulation.get(0));

    if (flag == 1) { // The new individual dominates
      population = insertNewIndividualWhenItDominatesTheCurrentOne(population, offspringPopulation);
    } else if (flag == 0) { // The new individual is non-dominated
      population =
          insertNewIndividualWhenItAndTheCurrentOneAreNonDominated(population, offspringPopulation);
    }
    return population;
  }

  @Override
  public List<S> result() {
    return archive.solutions();
  }

  private List<S> insertNewIndividualWhenItDominatesTheCurrentOne(
      List<S> population, List<S> offspringPopulation) {
    population.set(currentIndividual, offspringPopulation.get(0));
    archive.add(offspringPopulation.get(0));
    return population;
  }

  private List<S> insertNewIndividualWhenItAndTheCurrentOneAreNonDominated(
      List<S> population, List<S> offspringPopulation) {
    currentNeighbors.add(offspringPopulation.get(0));

    Ranking<S> rank = new FastNonDominatedSortRanking<S>();
    rank.compute(currentNeighbors);

    DensityEstimator<S> crowdingDistance = new CrowdingDistanceDensityEstimator<>();
    for (int j = 0; j < rank.getNumberOfSubFronts(); j++) {
      crowdingDistance.compute(rank.getSubFront(j));
    }

    this.currentNeighbors.sort(new RankingAndCrowdingDistanceComparator<S>());
    S worst = this.currentNeighbors.get(this.currentNeighbors.size() - 1);

    archive.add(offspringPopulation.get(0));

    if (worst != offspringPopulation.get(0)) {
      population.set(currentIndividual, offspringPopulation.get(0));
    }
    return population;
  }

  @Override
  public String name() {
    return "MOCell";
  }

  @Override
  public String description() {
    return "Multi-Objective Cellular evolutionary algorithm";
  }
}
