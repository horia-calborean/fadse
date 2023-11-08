package jmetal.component.catalogue.pso.localbestinitialization.impl;

import java.util.List;
import jmetal.component.catalogue.pso.localbestinitialization.LocalBestInitialization;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.errorchecking.Check;

/**
 * @author Antonio J. Nebro
 * @author Daniel Doblas
 */
public class DefaultLocalBestInitialization implements LocalBestInitialization {
  /**
   * Initialize the local best solutions.
   * @param swarm: List of possible solutions
   * @return A list of the best local solutions.
   */
  public DoubleSolution[] initialize(List<DoubleSolution> swarm) {
    Check.notNull(swarm);
    Check.that(swarm.size() > 0, "The swarm size is empty: " + swarm.size());

    DoubleSolution[] localBest = new DoubleSolution[swarm.size()] ;
    for (int i = 0; i < swarm.size(); i++) {
      localBest[i] = (DoubleSolution) swarm.get(i).copy() ;
    }

    return localBest ;
  }
}
