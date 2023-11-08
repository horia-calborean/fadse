package jmetal.core.problem;

import jmetal.core.solution.Solution;
import jmetal.core.util.observable.Observable;
import jmetal.core.util.observer.Observer;

/**
 * Interface representing dynamic problems. Problems implementing this interface act as {@link Observer} entities
 * in the sense that the can register themselves in {@link Observable} objects that may modify some problem
 * properties. A dynamic problem has an internal state about whether it has been updated or not, and there are methods
 * to query, set, and reset that state.
 *
 * @author Antonio J. Nebro <ajnebro@uma.es>
 */
public interface DynamicProblem<S extends Solution<?>, D>
    extends Problem<S> {
  void update(D value) ;
  boolean hasChanged() ;
  void setChanged() ;
  void clearChanged() ;
}