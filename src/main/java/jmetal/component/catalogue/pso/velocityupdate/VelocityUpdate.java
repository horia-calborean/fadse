package jmetal.component.catalogue.pso.velocityupdate;

import java.util.List;
import jmetal.component.catalogue.pso.globalbestselection.GlobalBestSelection;
import jmetal.component.catalogue.pso.inertiaweightcomputingstrategy.InertiaWeightComputingStrategy;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.archive.BoundedArchive;

/**
 * Interface representing velocity update strategies
 *
 * @author Antonio J. Nebro
 */
public interface VelocityUpdate {
  double[][] update(
      List<DoubleSolution> swarm,
      double[][] speed, DoubleSolution[] localBest,
      BoundedArchive<DoubleSolution> globalBest,
      GlobalBestSelection globalBestSelection,
      InertiaWeightComputingStrategy inertiaWeightComputingStrategy);
}
