package jmetal.component.catalogue.pso.perturbation;

import java.util.List;
import jmetal.core.solution.doublesolution.DoubleSolution;

public interface Perturbation {
  List<DoubleSolution> perturb(List<DoubleSolution> swarm) ;
}
