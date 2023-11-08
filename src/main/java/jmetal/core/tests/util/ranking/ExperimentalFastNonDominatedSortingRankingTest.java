package jmetal.core.tests.util.ranking;

import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.ranking.Ranking;
import jmetal.core.util.ranking.impl.ExperimentalFastNonDominanceRanking;

public class ExperimentalFastNonDominatedSortingRankingTest extends NonDominanceRankingTestCases<Ranking<DoubleSolution>> {
  public ExperimentalFastNonDominatedSortingRankingTest() {
    setRanking(new ExperimentalFastNonDominanceRanking<DoubleSolution>()) ;
  }
}

