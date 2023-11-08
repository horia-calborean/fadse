package jmetal.component.catalogue.pso.globalbestinitialization;

import java.util.List;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.archive.BoundedArchive;

public interface GlobalBestInitialization {
  BoundedArchive<DoubleSolution> initialize(List<DoubleSolution> swarm, BoundedArchive<DoubleSolution> globalBest) ;
}
