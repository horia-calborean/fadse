package jmetal.algorithm.multiobjective.pesa2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jmetal.core.algorithm.impl.AbstractGeneticAlgorithm;
import jmetal.algorithm.multiobjective.pesa2.util.AdaptiveGridArchive;
import jmetal.algorithm.multiobjective.pesa2.util.PESA2Selection;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.operator.selection.SelectionOperator;
import jmetal.core.problem.Problem;
import jmetal.core.solution.Solution;
import jmetal.core.util.evaluator.SolutionListEvaluator;

/**
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class PESA2<S extends Solution<?>> extends AbstractGeneticAlgorithm<S, List<S>> {
  private int maxEvaluations ;
  private int archiveSize ;
  private int biSections ;

  private int evaluations ;

  protected SelectionOperator<AdaptiveGridArchive<S>, S> selectionOperator ;

  private AdaptiveGridArchive<S> archive;
  protected final SolutionListEvaluator<S> evaluator;

  public PESA2(Problem<S> problem, int maxEvaluations, int populationSize, int archiveSize,
               int biSections, CrossoverOperator<S> crossoverOperator,
               MutationOperator<S> mutationOperator, SolutionListEvaluator<S> evaluator) {
    super(problem) ;
    this.maxEvaluations = maxEvaluations ;
    setMaxPopulationSize(populationSize); ;
    this.archiveSize = archiveSize ;
    this.biSections = biSections ;

    this.crossoverOperator = crossoverOperator;
    this.mutationOperator = mutationOperator;
    this.selectionOperator = new PESA2Selection<S>();

    this.evaluator = evaluator ;

    archive = new AdaptiveGridArchive<>(this.archiveSize, this.biSections,problem.numberOfObjectives()) ;
  }

  @Override protected void initProgress() {
    evaluations = getMaxPopulationSize() ;
  }

  @Override protected void updateProgress() {
    evaluations += getMaxPopulationSize() ;
  }

  @Override protected boolean isStoppingConditionReached() {
    return evaluations >= maxEvaluations ;
  }

  @Override protected List<S> evaluatePopulation(List<S> population) {
    population = evaluator.evaluate(population, getProblem());

    return population;
  }

  @Override protected List<S> selection(List<S> population) {
    List<S> matingPopulation = new ArrayList<>(getMaxPopulationSize()) ;

    for (S solution : population) {
      archive.add(solution) ;
    }

    while (matingPopulation.size() < getMaxPopulationSize()) {
      S solution = selectionOperator.execute(archive) ;

      matingPopulation.add(solution);
    }

    return matingPopulation ;
  }

  @Override protected List<S> reproduction(List<S> population) {
    List<S> offspringPopulation = new ArrayList<>(getMaxPopulationSize());
    for (int i = 0; i < getMaxPopulationSize(); i+=2) {
      List<S> parents = new ArrayList<>(2);
      parents.add(population.get(i));
      parents.add(population.get(i + 1));

      List<S> offspring = crossoverOperator.execute(parents);

      mutationOperator.execute(offspring.get(0));

      offspringPopulation.add(offspring.get(0));
    }
    return offspringPopulation;
  }

  @Override protected List<S> replacement(List<S> population, List<S> offspringPopulation) {
    for (S solution : offspringPopulation) {
      archive.add(solution) ;
    }

    return Collections.emptyList();
  }

  @Override public List<S> result() {
    return archive.solutions();
  }

  @Override public String name() {
    return "PESA2" ;
  }

  @Override public String description() {
    return "Pareto Envelope-based Selection Algorithm " ;
  }
}
