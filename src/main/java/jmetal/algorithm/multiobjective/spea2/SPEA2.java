package jmetal.algorithm.multiobjective.spea2;

import java.util.ArrayList;
import java.util.List;
import jmetal.core.algorithm.impl.AbstractGeneticAlgorithm;
import jmetal.algorithm.multiobjective.spea2.util.EnvironmentalSelection;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.operator.selection.SelectionOperator;
import jmetal.core.problem.Problem;
import jmetal.core.solution.Solution;
import jmetal.core.util.densityestimator.impl.StrenghtRawFitnessDensityEstimator;
import jmetal.core.util.evaluator.SolutionListEvaluator;

/**
 * @author Juan J. Durillo
 **/
public class SPEA2<S extends Solution<?>> extends AbstractGeneticAlgorithm<S, List<S>> {
  protected final int maxIterations;
  protected final SolutionListEvaluator<S> evaluator;
  protected int iterations;
  protected List<S> archive;
  protected final StrenghtRawFitnessDensityEstimator<S> densityEstimator = new StrenghtRawFitnessDensityEstimator<S>(1);
  protected final EnvironmentalSelection<S> environmentalSelection;
  protected final int k ;

  public SPEA2(Problem<S> problem, int maxIterations, int populationSize,
               CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
               SelectionOperator<List<S>, S> selectionOperator, SolutionListEvaluator<S> evaluator,
               int k) {
    super(problem);
    this.maxIterations = maxIterations;
    this.setMaxPopulationSize(populationSize);

    this.k = k ;
    this.crossoverOperator = crossoverOperator;
    this.mutationOperator = mutationOperator;
    this.selectionOperator = selectionOperator;
    this.environmentalSelection = new EnvironmentalSelection<S>(populationSize, k);

    this.archive = new ArrayList<>(populationSize);

    this.evaluator = evaluator;
  }

  @Override
  protected void initProgress() {
    iterations = 1;
  }

  @Override
  protected void updateProgress() {
    iterations++;
  }

  @Override
  protected boolean isStoppingConditionReached() {
    return iterations >= maxIterations;
  }

  @Override
  protected List<S> evaluatePopulation(List<S> population) {
    population = evaluator.evaluate(population, getProblem());
    return population;
  }

  @Override
  protected List<S> selection(List<S> population) {
    List<S> union = new ArrayList<>(2*getMaxPopulationSize());
    union.addAll(archive);
    union.addAll(population);
    densityEstimator.compute(union);
    archive = environmentalSelection.execute(union);
    return archive;
  }

  @Override
  protected List<S> reproduction(List<S> population) {
    List<S> offSpringPopulation= new ArrayList<>(getMaxPopulationSize());

    while (offSpringPopulation.size() < getMaxPopulationSize()){
      List<S> parents = new ArrayList<>(2);
      S candidateFirstParent = selectionOperator.execute(population);
      parents.add(candidateFirstParent);
      S candidateSecondParent;
      candidateSecondParent = selectionOperator.execute(population);
      parents.add(candidateSecondParent);

      List<S> offspring = crossoverOperator.execute(parents);
      mutationOperator.execute(offspring.get(0));
      offSpringPopulation.add(offspring.get(0));
    }
    return offSpringPopulation;
  }

  @Override
  protected List<S> replacement(List<S> population,
      List<S> offspringPopulation) {
    return offspringPopulation;
  }

  @Override
  public List<S> result() {
    return archive;
  }

  @Override public String name() {
    return "SPEA2" ;
  }

  @Override public String description() {
    return "Strength Pareto. Evolutionary Algorithm" ;
  }
}
