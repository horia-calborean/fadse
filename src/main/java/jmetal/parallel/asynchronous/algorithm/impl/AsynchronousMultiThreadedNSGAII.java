package jmetal.parallel.asynchronous.algorithm.impl;

import jmetal.component.catalogue.common.termination.Termination;
import jmetal.component.catalogue.ea.replacement.Replacement;
import jmetal.component.catalogue.ea.replacement.impl.RankingAndDensityEstimatorReplacement;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.operator.selection.impl.BinaryTournamentSelection;
import jmetal.core.problem.Problem;
import jmetal.core.solution.Solution;
import jmetal.core.util.comparator.RankingAndCrowdingDistanceComparator;
import jmetal.core.util.densityestimator.impl.CrowdingDistanceDensityEstimator;
import jmetal.core.util.ranking.impl.MergeNonDominatedSortRanking;

public class AsynchronousMultiThreadedNSGAII<S extends Solution<?>>
    extends AsynchronousMultiThreadedGeneticAlgorithm<S> {

  public AsynchronousMultiThreadedNSGAII(
      int numberOfCores,
      Problem<S> problem,
      int populationSize,
      CrossoverOperator<S> crossover,
      MutationOperator<S> mutation,
      Termination termination) {
    super(numberOfCores,problem, populationSize, crossover,mutation, new BinaryTournamentSelection<>(new RankingAndCrowdingDistanceComparator<>()),
            new RankingAndDensityEstimatorReplacement<>(
                    new MergeNonDominatedSortRanking<>(),
                    new CrowdingDistanceDensityEstimator<>(),
                    Replacement.RemovalPolicy.ONE_SHOT),termination);
  }
}
