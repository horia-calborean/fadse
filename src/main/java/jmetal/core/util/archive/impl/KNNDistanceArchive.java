package jmetal.core.util.archive.impl;

import java.util.Comparator;
import jmetal.core.solution.Solution;
import jmetal.core.util.SolutionListUtils;
import jmetal.core.util.densityestimator.DensityEstimator;
import jmetal.core.util.densityestimator.impl.KnnDensityEstimator;

/**
 * Created by Antonio J. Nebro on 24/09/14.
 * Modified by Juanjo on 07/04/2015
 */
@SuppressWarnings("serial")
public class KNNDistanceArchive<S extends Solution<?>> extends AbstractBoundedArchive<S> {
  private Comparator<S> knnDistanceComparator;
  private DensityEstimator<S> knnDensityEstimator ;

  public KNNDistanceArchive(int maxSize, int k) {
    super(maxSize);
    knnDensityEstimator = new KnnDensityEstimator<S>(k);
    knnDistanceComparator = Comparator.comparing(knnDensityEstimator::value).reversed() ;
  }

  @Override
  public void prune() {
    if (solutions().size() > maximumSize()) {
      computeDensityEstimator();
      S worst = new SolutionListUtils().findWorstSolution(solutions(), knnDistanceComparator) ;
      solutions().remove(worst);
    }
  }

  @Override
  public Comparator<S> comparator() {
    return knnDistanceComparator ;
  }

  @Override
  public void computeDensityEstimator() {
    knnDensityEstimator.compute(solutions());
  }
}
