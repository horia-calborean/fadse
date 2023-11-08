package jmetal.component.catalogue.common.solutionscreation;

import java.util.List;
import jmetal.core.solution.Solution;

/**
 * Interface representing entities that create a list of solutions applying some strategy (e.g, random)
 *
 * @author Antonio J. Nebro (ajnebro@uma.es)
 *
 * @param <S> Solution
 */
@FunctionalInterface
public interface SolutionsCreation<S extends Solution<?>> {
  List<S> create() ;
}
