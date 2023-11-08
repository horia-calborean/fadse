package jmetal.component.catalogue.pso.localbestinitialization;

import java.util.List;
import jmetal.core.solution.doublesolution.DoubleSolution;

/**
 * TODO: comment the interface
 *
 * @author Antonio J. Nebro
 * @author Daniel Doblas
 */
public interface LocalBestInitialization {
  DoubleSolution[] initialize(List<DoubleSolution> swarm) ;
}
