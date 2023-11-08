package jmetal.core.util.archive;

import java.io.Serializable;
import java.util.List;

/**
 * Interface representing an archive of solutions
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public interface Archive<S> extends Serializable {
  boolean add(S solution) ;
  S get(int index) ;
  List<S> solutions() ;
  int size() ;
}
