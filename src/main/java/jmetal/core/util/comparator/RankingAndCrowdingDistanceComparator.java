package jmetal.core.util.comparator;

import java.io.Serializable;
import java.util.Comparator;
import jmetal.core.solution.Solution;
import jmetal.core.util.densityestimator.impl.CrowdingDistanceDensityEstimator;
import jmetal.core.util.ranking.Ranking;
import jmetal.core.util.ranking.impl.FastNonDominatedSortRanking;

/**
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 * @version 1.0
 *     <p>This class implements a comparator based on the rank of the solutions; if the rank is the
 *     same then the crowding distance is used.
 */
public class RankingAndCrowdingDistanceComparator<S extends Solution<?>>
    implements Comparator<S>, Serializable {
  private final Comparator<S> rankComparator;
  private final Comparator<S> crowdingDistanceComparator;

  public RankingAndCrowdingDistanceComparator(Ranking<S> ranking) {
    rankComparator = Comparator.comparing(ranking::getRank);
    CrowdingDistanceDensityEstimator<S> crowdingDistanceDensityEstimator =
        new CrowdingDistanceDensityEstimator<>();
    crowdingDistanceComparator = crowdingDistanceDensityEstimator.comparator();
  }

  public RankingAndCrowdingDistanceComparator() {
    this(new FastNonDominatedSortRanking<>());
  }
  /**
   * Compares two solutions.
   *
   * @param solution1 Object representing the first solution
   * @param solution2 Object representing the second solution.
   * @return -1, or 0, or 1 if solution1 is less than, equal, or greater than solution2,
   *     respectively.
   */
  @Override
  public int compare(S solution1, S solution2) {
    int result = rankComparator.compare(solution1, solution2);
    if (result == 0) {
      result = crowdingDistanceComparator.compare(solution1, solution2);
    }

    return result;
  }
}
