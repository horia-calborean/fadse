package jmetal.auto.parameter.catalogue;

import java.util.List;
import jmetal.auto.parameter.CategoricalParameter;
import jmetal.component.catalogue.ea.replacement.Replacement;
import jmetal.component.catalogue.ea.replacement.impl.RankingAndDensityEstimatorReplacement;
import jmetal.core.solution.Solution;
import jmetal.core.util.densityestimator.DensityEstimator;
import jmetal.core.util.densityestimator.impl.CrowdingDistanceDensityEstimator;
import jmetal.core.util.densityestimator.impl.KnnDensityEstimator;
import jmetal.core.util.errorchecking.JMetalException;
import jmetal.core.util.ranking.Ranking;
import jmetal.core.util.ranking.impl.FastNonDominatedSortRanking;
import jmetal.core.util.ranking.impl.StrengthRanking;

public class ReplacementParameter extends CategoricalParameter {
  public ReplacementParameter(List<String> selectionStrategies) {
    super("replacement", selectionStrategies);
  }

  public Replacement<?> getParameter() {
    String removalPolicy = (String) findGlobalParameter("removalPolicy").value();
    Replacement<?> result;
    switch (value()) {
      case "rankingAndDensityEstimatorReplacement":
        String rankingName = (String) findSpecificParameter("rankingForReplacement").value();
        String densityEstimatorName =
            (String) findSpecificParameter("densityEstimatorForReplacement").value();

        Ranking<Solution<?>> ranking;
        if (rankingName.equals("dominanceRanking")) {
          ranking = new FastNonDominatedSortRanking<>();
        } else {
          ranking = new StrengthRanking<>();
        }

        DensityEstimator<Solution<?>> densityEstimator;
        if (densityEstimatorName.equals("crowdingDistance")) {
          densityEstimator = new CrowdingDistanceDensityEstimator<>();
        } else {
          densityEstimator = new KnnDensityEstimator<>(1);
        }

        if (removalPolicy.equals("oneShot")) {
          result =
              new RankingAndDensityEstimatorReplacement<>(
                  ranking, densityEstimator, Replacement.RemovalPolicy.ONE_SHOT);
        } else {
          result =
              new RankingAndDensityEstimatorReplacement<>(
                  ranking, densityEstimator, Replacement.RemovalPolicy.SEQUENTIAL);
        }

        break;
      default:
        throw new JMetalException("Replacement component unknown: " + value());
    }

    return result;
  }
}
