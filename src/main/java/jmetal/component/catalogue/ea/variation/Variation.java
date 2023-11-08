package jmetal.component.catalogue.ea.variation;

import java.util.List;
import jmetal.core.solution.Solution;

public interface Variation<S extends Solution<?>> {
  List<S> variate(List<S> solutionList, List<S> matingPool) ;
  int getMatingPoolSize() ;
  int getOffspringPopulationSize() ;
}
