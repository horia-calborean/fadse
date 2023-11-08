package jmetal.algorithm.multiobjective.nsgaii;

import java.util.ArrayList;
import java.util.List;
import jmetal.core.algorithm.Algorithm;
import jmetal.core.algorithm.impl.AbstractGeneticAlgorithm;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.operator.selection.SelectionOperator;
import jmetal.core.operator.selection.impl.RankingAndCrowdingSelection;
import jmetal.core.problem.Problem;
import jmetal.core.solution.Solution;
import jmetal.core.util.SolutionListUtils;
import jmetal.core.util.evaluator.SolutionListEvaluator;
import jmetal.core.util.ranking.Ranking;
import jmetal.core.util.ranking.impl.FastNonDominatedSortRanking;

/**
 * Implementation of NSGA-II following the scheme used in jMetal4.5 and former versions, i.e,
 * without implementing the {@link AbstractGeneticAlgorithm} interface.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class NSGAII45<S extends Solution<?>> implements Algorithm<List<S>> {
  protected List<S> population;
  protected final int maxEvaluations;
  protected final int populationSize;

  protected final Problem<S> problem;

  protected final SolutionListEvaluator<S> evaluator;

  protected int evaluations;

  protected SelectionOperator<List<S>, S> selectionOperator;
  protected CrossoverOperator<S> crossoverOperator;
  protected MutationOperator<S> mutationOperator;

  /** Constructor */
  public NSGAII45(
      Problem<S> problem,
      int maxEvaluations,
      int populationSize,
      CrossoverOperator<S> crossoverOperator,
      MutationOperator<S> mutationOperator,
      SelectionOperator<List<S>, S> selectionOperator,
      SolutionListEvaluator<S> evaluator) {
    super();
    this.problem = problem;
    this.maxEvaluations = maxEvaluations;
    this.populationSize = populationSize;

    this.crossoverOperator = crossoverOperator;
    this.mutationOperator = mutationOperator;
    this.selectionOperator = selectionOperator;

    this.evaluator = evaluator;
  }

  /** Run method */
  @Override
  public void run() {
    population = createInitialPopulation();
    evaluatePopulation(population);

    evaluations = populationSize;

    while (evaluations < maxEvaluations) {
      List<S> offspringPopulation = new ArrayList<>(populationSize);
      for (int i = 0; i < populationSize; i += 2) {
        List<S> parents = new ArrayList<>(2);
        parents.add(selectionOperator.execute(population));
        parents.add(selectionOperator.execute(population));

        List<S> offspring = crossoverOperator.execute(parents);

        mutationOperator.execute(offspring.get(0));
        mutationOperator.execute(offspring.get(1));

        offspringPopulation.add(offspring.get(0));
        offspringPopulation.add(offspring.get(1));
      }

      evaluatePopulation(offspringPopulation);

      List<S> jointPopulation = new ArrayList<>();
      jointPopulation.addAll(population);
      jointPopulation.addAll(offspringPopulation);

      Ranking<S> ranking = new FastNonDominatedSortRanking<>();
      ranking.compute(jointPopulation);

      RankingAndCrowdingSelection<S> rankingAndCrowdingSelection;
      rankingAndCrowdingSelection = new RankingAndCrowdingSelection<>(populationSize);

      population = rankingAndCrowdingSelection.execute(jointPopulation);

      evaluations += populationSize;
    }
  }

  @Override
  public List<S> result() {
    return getNonDominatedSolutions(population);
  }

  protected List<S> createInitialPopulation() {
    List<S> population = new ArrayList<>(populationSize);
    for (int i = 0; i < populationSize; i++) {
      S newIndividual = problem.createSolution();
      population.add(newIndividual);
    }
    return population;
  }

  protected List<S> evaluatePopulation(List<S> population) {
    population = evaluator.evaluate(population, problem);

    return population;
  }

  protected Ranking<S> computeRanking(List<S> solutionList) {
    Ranking<S> ranking = new FastNonDominatedSortRanking<>();
    ranking.compute(solutionList);

    return ranking;
  }

  protected List<S> getNonDominatedSolutions(List<S> solutionList) {
    return SolutionListUtils.getNonDominatedSolutions(solutionList);
  }

  @Override
  public String name() {
    return "NSGAII45";
  }

  @Override
  public String description() {
    return "Nondominated Sorting Genetic Algorithm version II. Version not using the AbstractGeneticAlgorithm template";
  }
}
