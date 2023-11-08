package jmetal.core.qualityindicator;

import java.util.ArrayList;
import java.util.List;
import jmetal.core.qualityindicator.impl.Epsilon;
import jmetal.core.qualityindicator.impl.GeneralizedSpread;
import jmetal.core.qualityindicator.impl.GenerationalDistance;
import jmetal.core.qualityindicator.impl.InvertedGenerationalDistance;
import jmetal.core.qualityindicator.impl.InvertedGenerationalDistancePlus;
import jmetal.core.qualityindicator.impl.NormalizedHypervolume;
import jmetal.core.qualityindicator.impl.Spread;
import jmetal.core.qualityindicator.impl.hypervolume.impl.PISAHypervolume;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.NormalizeUtils;
import jmetal.core.util.errorchecking.Check;

public class QualityIndicatorUtils {

  /**
   * Creates a list with the available indicators (but setCoverage)
   *
   * @param referenceFront
   * @return
   */
  public static List<QualityIndicator> getAvailableIndicators(double[][] referenceFront) {
    List<QualityIndicator> list = new ArrayList<>();
    list.add(new Epsilon(referenceFront));
    list.add(new PISAHypervolume(referenceFront));
    list.add(new NormalizedHypervolume(referenceFront));
    list.add(new GenerationalDistance(referenceFront));
    list.add(new InvertedGenerationalDistance(referenceFront));
    list.add(new InvertedGenerationalDistancePlus(referenceFront));
    list.add(new Spread(referenceFront));
    list.add(new GeneralizedSpread(referenceFront));

    return list;
  }

  /**
   * Given an indicator name, finds the indicator in the list of indicator
   *
   * @param indicatorName
   * @param indicatorList
   * @return
   */
  public static QualityIndicator getIndicatorFromName(String indicatorName,
      List<QualityIndicator> indicatorList) {
    QualityIndicator result = null;

    for (QualityIndicator indicator : indicatorList) {
      if (indicator.name().equals(indicatorName)) {
        result = indicator;
        break;
      }
    }

    Check.notNull(result);

    return result;
  }

  public static void printQualityIndicators(double[][] front, double[][] referenceFront) {
    double[][] normalizedReferenceFront = NormalizeUtils.normalize(referenceFront);
    double[][] normalizedFront =
        NormalizeUtils.normalize(
            front,
            NormalizeUtils.getMinValuesOfTheColumnsOfAMatrix(referenceFront),
            NormalizeUtils.getMaxValuesOfTheColumnsOfAMatrix(referenceFront));

    List<QualityIndicator> qualityIndicators = getAvailableIndicators(normalizedReferenceFront);
    for (QualityIndicator indicator : qualityIndicators) {
      JMetalLogger.logger.info(
          () -> indicator.name() + ": " + indicator.compute(normalizedFront));
    }
  }
}
