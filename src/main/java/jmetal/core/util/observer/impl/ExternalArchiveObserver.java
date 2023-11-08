package jmetal.core.util.observer.impl;

import java.util.List;
import java.util.Map;
import jmetal.core.solution.Solution;
import jmetal.core.util.archive.Archive;
import jmetal.core.util.observable.Observable;
import jmetal.core.util.observer.Observer;

/**
 * This observer add the solutions of population to an archive. It expects a pair
 * (EVALUATIONS, int) in the map used in the update() method.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class ExternalArchiveObserver<S extends Solution<?>> implements Observer<Map<String, Object>> {

  private Archive<S> archive ;

  public ExternalArchiveObserver(Archive<S> archive) {
    this.archive = archive ;
  }

  @Override
  public void update(Observable<Map<String, Object>> observable, Map<String, Object> data) {
    List<S> population = (List<S>) data.get("POPULATION");
    population.stream().forEach(solution -> archive.add((S) solution.copy()));
  }

  public Archive<S> getArchive() {
    return archive;
  }

  public String getName() {
    return "External archive observer";
  }
}
