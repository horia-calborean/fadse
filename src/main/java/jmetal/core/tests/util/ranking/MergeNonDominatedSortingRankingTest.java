package jmetal.core.tests.util.ranking;

import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.ranking.Ranking;
import jmetal.core.util.ranking.impl.MergeNonDominatedSortRanking;

public class MergeNonDominatedSortingRankingTest extends NonDominanceRankingTestCases<Ranking<DoubleSolution>> {
  public MergeNonDominatedSortingRankingTest() {
    setRanking(new MergeNonDominatedSortRanking<>());
  }
}
