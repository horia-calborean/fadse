package jmetal.component.catalogue.pso.positionupdate;

import java.util.List;
import jmetal.core.solution.doublesolution.DoubleSolution;

public interface PositionUpdate {
  List<DoubleSolution> update(List<DoubleSolution> swarm, double[][]speed) ;
}
