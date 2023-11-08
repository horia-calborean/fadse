package jmetal.component.algorithm.multiobjective;

import jmetal.component.algorithm.EvolutionaryAlgorithm;
import jmetal.component.catalogue.common.evaluation.Evaluation;
import jmetal.component.catalogue.common.evaluation.impl.SequentialEvaluation;
import jmetal.component.catalogue.common.solutionscreation.SolutionsCreation;
import jmetal.component.catalogue.common.solutionscreation.impl.RandomSolutionsCreation;
import jmetal.component.catalogue.common.termination.Termination;
import jmetal.component.catalogue.common.termination.impl.TerminationByEvaluations;
import jmetal.component.catalogue.ea.replacement.Replacement;
import jmetal.component.catalogue.ea.replacement.impl.RankingAndDensityEstimatorReplacement;
import jmetal.component.catalogue.ea.replacement.impl.SMSEMOAReplacement;
import jmetal.component.catalogue.ea.selection.Selection;
import jmetal.component.catalogue.ea.selection.impl.RandomSelection;
import jmetal.component.catalogue.ea.variation.Variation;
import jmetal.component.catalogue.ea.variation.impl.CrossoverAndMutationVariation;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.problem.Problem;
import jmetal.core.solution.Solution;
import jmetal.core.util.densityestimator.DensityEstimator;
import jmetal.core.util.densityestimator.impl.CrowdingDistanceDensityEstimator;
import jmetal.core.util.legacy.qualityindicator.impl.hypervolume.Hypervolume;
import jmetal.core.util.legacy.qualityindicator.impl.hypervolume.impl.PISAHypervolume;
import jmetal.core.util.ranking.Ranking;
import jmetal.core.util.ranking.impl.FastNonDominatedSortRanking;

/**
 * Class to configure and build an instance of the SMS-EMOA algorithm
 *
 * @param <S>
 */
public class SMSEMOABuilder<S extends Solution<?>> {
  private String name;
  private Ranking<S> ranking;
  private DensityEstimator<S> densityEstimator;
  private Evaluation<S> evaluation;
  private SolutionsCreation<S> createInitialPopulation;
  private Termination termination;
  private Selection<S> selection;
  private Variation<S> variation;
  private Replacement<S> replacement;

  public SMSEMOABuilder(Problem<S> problem, int populationSize,
      CrossoverOperator<S> crossover, MutationOperator<S> mutation) {
    name = "SMS-EMOA";

    densityEstimator = new CrowdingDistanceDensityEstimator<>();
    ranking = new FastNonDominatedSortRanking<>();

    this.createInitialPopulation = new RandomSolutionsCreation<>(problem, populationSize);

    Hypervolume<S> hypervolume = new PISAHypervolume<>();
    this.replacement = new SMSEMOAReplacement<>(ranking, hypervolume);

    this.variation =
        new CrossoverAndMutationVariation<>(1, crossover, mutation);

    this.selection = new RandomSelection<>(variation.getMatingPoolSize());

    this.termination = new TerminationByEvaluations(25000);

    this.evaluation = new SequentialEvaluation<>(problem);
  }

  public SMSEMOABuilder<S> setTermination(Termination termination) {
    this.termination = termination;

    return this;
  }

  public SMSEMOABuilder<S> setRanking(Ranking<S> ranking) {
    this.ranking = ranking;
    this.replacement =
        new RankingAndDensityEstimatorReplacement<>(
            ranking, densityEstimator, Replacement.RemovalPolicy.ONE_SHOT);

    return this;
  }

  public SMSEMOABuilder<S> setEvaluation(Evaluation<S> evaluation) {
    this.evaluation = evaluation;

    return this;
  }

  public EvolutionaryAlgorithm<S> build() {
    return new EvolutionaryAlgorithm<>(name, createInitialPopulation, evaluation, termination,
        selection, variation, replacement);
  }
}
