package jmetal.core.util.observable;

/**
 * Interface representing observable entities according to the Observer pattern
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public interface ObservableEntity<T> {
  Observable<T> observable();
}
