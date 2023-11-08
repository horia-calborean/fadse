package jmetal.core.util.comparator;

import java.io.Serializable;
import java.util.Comparator;
import jmetal.core.solution.Solution;
import jmetal.core.util.ranking.Ranking;
import jmetal.core.util.ranking.impl.FastNonDominatedSortRanking;

/**
 * created at 10:29 pm, 2019/1/28 Comparator combining dominance-ranking comparator and DIR-score
 * comparator
 *
 * @see DirScoreComparator
 * @author sunhaoran <nuaa_sunhr@yeah.net>
 */
public class RankingAndDirScoreDistanceComparator<S extends Solution<?>>
    implements Comparator<S>, Serializable {
  private final Comparator<S> rankComparator;
  private final Comparator<S> dirScoreComparator;

  public RankingAndDirScoreDistanceComparator() {
    this(new FastNonDominatedSortRanking<>());
  }

  public RankingAndDirScoreDistanceComparator(Ranking<S> ranking) {
    rankComparator = Comparator.comparing(ranking::getRank);
    dirScoreComparator = new DirScoreComparator<>();
  }

  @Override
  public int compare(S o1, S o2) {
    int result = rankComparator.compare(o1, o2);
    if (result == 0) {
      return dirScoreComparator.compare(o1, o2);
    }
    return result;
  }
}
