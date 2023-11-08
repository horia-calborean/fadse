package jmetal.core.util.archive.impl;

import java.util.Comparator;
import jmetal.core.solution.Solution;
import jmetal.core.util.SolutionListUtils;
import jmetal.core.util.comparator.dominanceComparator.DominanceComparator;
import jmetal.core.util.comparator.dominanceComparator.impl.DefaultDominanceComparator;
import jmetal.core.util.densityestimator.DensityEstimator;
import jmetal.core.util.densityestimator.impl.CrowdingDistanceDensityEstimator;

/**
 * Created by Antonio J. Nebro on 24/09/14.
 * Modified by Juanjo on 07/04/2015
 */
@SuppressWarnings("serial")
public class CrowdingDistanceArchive<S extends Solution<?>> extends AbstractBoundedArchive<S> {
  private Comparator<S> crowdingDistanceComparator;
  private DensityEstimator<S> crowdingDistance ;

  public CrowdingDistanceArchive(int maxSize, DominanceComparator<S> dominanceComparator) {
    super(maxSize, dominanceComparator);
    crowdingDistance = new CrowdingDistanceDensityEstimator<>();
    crowdingDistanceComparator = Comparator.comparing(crowdingDistance::value).reversed() ;
  }

  public CrowdingDistanceArchive(int maxSize) {
    this(maxSize, new DefaultDominanceComparator<>()) ;
  }

  @Override
  public void prune() {
    if (solutions().size() > maximumSize()) {
      computeDensityEstimator();
      S worst = new SolutionListUtils().findWorstSolution(solutions(), crowdingDistanceComparator) ;
      solutions().remove(worst);
    }
  }

  @Override
  public Comparator<S> comparator() {
    return crowdingDistanceComparator ;
  }

  @Override
  public void computeDensityEstimator() {
    crowdingDistance.compute(solutions());
  }
}
