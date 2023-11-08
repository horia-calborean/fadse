package jmetal.component.catalogue.pso.localbestupdate;

import java.util.List;
import jmetal.core.solution.doublesolution.DoubleSolution;

public interface LocalBestUpdate {
  DoubleSolution[] update(List<DoubleSolution> swarm, DoubleSolution[] localBest) ;
}
