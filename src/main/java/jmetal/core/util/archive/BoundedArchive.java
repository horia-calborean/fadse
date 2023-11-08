package jmetal.core.util.archive;

import java.util.Comparator;

/**
 * Interface representing a bounded archive of solutions
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public interface BoundedArchive<S> extends Archive<S> {
  int maximumSize() ;
  Comparator<S> comparator() ;
  void computeDensityEstimator() ;
}
