package jmetal.component.catalogue.pso.globalbestselection;

import java.util.List;
import jmetal.core.solution.doublesolution.DoubleSolution;

public interface GlobalBestSelection {
  DoubleSolution select(List<DoubleSolution> globalBestList) ;
}
