package jmetal.core.util.archive.impl;

import java.util.Comparator;
import jmetal.core.solution.Solution;
import jmetal.core.util.SolutionListUtils;
import jmetal.core.util.densityestimator.DensityEstimator;

/**
 * This class implements a generic bound archive.
 *
 * @author Antonio J. Nebro
 */
public class GenericBoundedArchive<S extends Solution<?>> extends AbstractBoundedArchive<S> {
  private Comparator<S> comparator;
  private DensityEstimator<S> densityEstimator ;

  public GenericBoundedArchive(int maxSize, DensityEstimator<S> densityEstimator) {
    super(maxSize);
    this.densityEstimator = densityEstimator ;
    comparator = densityEstimator.comparator() ;
  }

  @Override
  public void prune() {
    if (solutions().size() > maximumSize()) {
      computeDensityEstimator();
      S worst = new SolutionListUtils().findWorstSolution(solutions(), comparator) ;
      solutions().remove(worst);
    }
  }

  @Override
  public Comparator<S> comparator() {
    return comparator ;
  }

  @Override
  public void computeDensityEstimator() {
    densityEstimator.compute(solutions());
  }
}
