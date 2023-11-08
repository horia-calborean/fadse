package jmetal.auto.parameter.catalogue;

import java.util.List;
import jmetal.auto.parameter.CategoricalParameter;
import jmetal.core.solution.Solution;
import jmetal.core.util.errorchecking.JMetalException;
import jmetal.core.util.ranking.Ranking;
import jmetal.core.util.ranking.impl.FastNonDominatedSortRanking;
import jmetal.core.util.ranking.impl.StrengthRanking;

public class RankingParameter <S extends Solution<?>> extends CategoricalParameter {
  public RankingParameter(String name,List<String> validRankings) {
    super(name, validRankings);
  }

  public Ranking<S> getParameter() {
    Ranking<S> result ;
    switch (value()) {
      case "dominanceRanking":
        result = new FastNonDominatedSortRanking<>() ;
        break;
      case "strengthRanking":
        result = new StrengthRanking<>() ;
        break;
      default:
        throw new JMetalException("Ranking does not exist: " + name());
    }
    return result;
  }
}
