package jmetal.component.catalogue.pso.globalbestupdate;

import java.util.List;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.archive.BoundedArchive;

public interface GlobalBestUpdate {
  BoundedArchive<DoubleSolution> update(List<DoubleSolution> swarm, BoundedArchive<DoubleSolution> globalBest) ;
}
