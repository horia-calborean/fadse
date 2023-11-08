package jmetal.algorithm.multiobjective.nsgaii;

import java.util.Comparator;
import java.util.List;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.operator.selection.SelectionOperator;
import jmetal.core.problem.Problem;
import jmetal.core.solution.Solution;
import jmetal.core.util.SolutionListUtils;
import jmetal.core.util.evaluator.SolutionListEvaluator;
import jmetal.core.util.legacy.front.Front;
import jmetal.core.util.legacy.front.impl.ArrayFront;
import jmetal.core.util.legacy.qualityindicator.impl.hypervolume.impl.PISAHypervolume;
import jmetal.core.util.measure.Measurable;
import jmetal.core.util.measure.MeasureManager;
import jmetal.core.util.measure.impl.BasicMeasure;
import jmetal.core.util.measure.impl.CountingMeasure;
import jmetal.core.util.measure.impl.DurationMeasure;
import jmetal.core.util.measure.impl.SimpleMeasureManager;
import jmetal.core.util.ranking.Ranking;
import jmetal.core.util.ranking.impl.FastNonDominatedSortRanking;

/**
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class NSGAIIMeasures<S extends Solution<?>> extends NSGAII<S> implements Measurable {
  protected CountingMeasure evaluations ;
  protected DurationMeasure durationMeasure ;
  protected SimpleMeasureManager measureManager ;

  protected BasicMeasure<List<S>> solutionListMeasure ;
  protected BasicMeasure<Integer> numberOfNonDominatedSolutionsInPopulation ;
  protected BasicMeasure<Double> hypervolumeValue ;

  protected Front referenceFront ;

  /**
   * Constructor
   */
  public NSGAIIMeasures(Problem<S> problem, int maxIterations, int populationSize,
                        int matingPoolSize, int offspringPopulationSize,
                        CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
                        SelectionOperator<List<S>, S> selectionOperator, Comparator<S> dominanceComparator, SolutionListEvaluator<S> evaluator) {
    super(problem, maxIterations, populationSize, matingPoolSize, offspringPopulationSize,
            crossoverOperator, mutationOperator, selectionOperator, dominanceComparator, evaluator) ;

    referenceFront = new ArrayFront() ;

    initMeasures() ;
  }

  @Override protected void initProgress() {
    evaluations.reset(getMaxPopulationSize());
  }

  @Override protected void updateProgress() {
    evaluations.increment(getMaxPopulationSize());

    solutionListMeasure.push(getPopulation());

    if (referenceFront.getNumberOfPoints() > 0) {
      hypervolumeValue.push(
              new PISAHypervolume<S>(referenceFront).evaluate(
                  SolutionListUtils.getNonDominatedSolutions(getPopulation())));
    }
  }

  @Override protected boolean isStoppingConditionReached() {
    return evaluations.get() >= maxEvaluations;
  }

  @Override
  public void run() {
    durationMeasure.reset();
    durationMeasure.start();
    super.run();
    durationMeasure.stop();
  }

  /* Measures code */
  private void initMeasures() {
    durationMeasure = new DurationMeasure() ;
    evaluations = new CountingMeasure(0) ;
    numberOfNonDominatedSolutionsInPopulation = new BasicMeasure<>() ;
    solutionListMeasure = new BasicMeasure<>() ;
    hypervolumeValue = new BasicMeasure<>() ;

    measureManager = new SimpleMeasureManager() ;
    measureManager.setPullMeasure("currentExecutionTime", durationMeasure);
    measureManager.setPullMeasure("currentEvaluation", evaluations);
    measureManager.setPullMeasure("numberOfNonDominatedSolutionsInPopulation",
        numberOfNonDominatedSolutionsInPopulation);

    measureManager.setPushMeasure("currentPopulation", solutionListMeasure);
    measureManager.setPushMeasure("currentEvaluation", evaluations);
    measureManager.setPushMeasure("hypervolume", hypervolumeValue);
  }

  @Override
  public MeasureManager getMeasureManager() {
    return measureManager ;
  }

  @Override protected List<S> replacement(List<S> population,
      List<S> offspringPopulation) {
    List<S> pop = super.replacement(population, offspringPopulation) ;

    Ranking<S> ranking = new FastNonDominatedSortRanking<>(dominanceComparator);
    ranking.compute(population);

    numberOfNonDominatedSolutionsInPopulation.set(ranking.getSubFront(0).size());

    return pop;
  }

  public CountingMeasure getEvaluations() {
    return evaluations;
  }

  @Override public String name() {
    return "NSGAIIM" ;
  }

  @Override public String description() {
    return "Nondominated Sorting Genetic Algorithm version II. Version using measures" ;
  }

  public void setReferenceFront(Front referenceFront) {
    this.referenceFront = referenceFront ;
  }
}
