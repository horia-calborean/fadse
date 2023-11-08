package jmetal.core.util.densityestimator.impl;

import java.util.Comparator;
import java.util.List;
import jmetal.core.solution.Solution;
import jmetal.core.util.densityestimator.DensityEstimator;
import jmetal.core.util.errorchecking.Check;
import jmetal.core.util.legacy.front.impl.ArrayFront;
import jmetal.core.util.legacy.qualityindicator.impl.hypervolume.Hypervolume;
import jmetal.core.util.legacy.qualityindicator.impl.hypervolume.impl.PISAHypervolume;

/**
 * This class implements a density estimator based on the hypervolume contribution
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class HypervolumeContributionDensityEstimator<S extends Solution<?>> implements DensityEstimator<S> {

  private String attributeId = getClass().getName();
  private Hypervolume<S> hypervolume ;

  public HypervolumeContributionDensityEstimator(List<S> referenceFront) {
    hypervolume = new PISAHypervolume<>(new ArrayFront(referenceFront)) ;
  }

  public HypervolumeContributionDensityEstimator(double[] referencePoint) {
    hypervolume = new PISAHypervolume<>(referencePoint) ;
  }

  /**
   * Assigns the hv contribution to all population in a <code>SolutionSet</code>.
   *
   * @param solutionList The <code>SolutionSet</code>.
   */

  @Override
  public void compute(List<S> solutionList) {
    int size = solutionList.size();

    if (size == 0) {
      return;
    }

    hypervolume.computeHypervolumeContribution(solutionList) ;
  }

  @Override
  public Double value(S solution) {
    Check.notNull(solution);

    Double result = 0.0 ;
    if (solution.attributes().get(attributeId) != null) {
      result = (Double) solution.attributes().get(attributeId) ;
    }
    return result ;
  }

  @Override
  public Comparator<S> comparator() {
    return Comparator.comparing(this::value) ;
  }
}

