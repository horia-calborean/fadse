package jmetal.core.operator.selection.impl;

import java.util.Comparator;
import jmetal.core.solution.Solution;
import jmetal.core.util.comparator.dominanceComparator.impl.DominanceWithConstraintsComparator;

/**
 * Applies a binary tournament selection to return the best solution between two that have been
 * chosen at random from a solution list.
 * Modified by Juanjo in 13.03.2015. A binary tournament is now a {@link NaryTournamentSelection}
 * with tournament size equals to 2
 *
 * @author Antonio J. Nebro, Juan J. Durillo
 */
@SuppressWarnings("serial")
public class BinaryTournamentSelection<S extends Solution<?>> extends NaryTournamentSelection<S> {
    /** Constructor */
  public BinaryTournamentSelection() {
    super(2, new DominanceWithConstraintsComparator<S>()) ;
  }

  /** Constructor */
  public BinaryTournamentSelection(Comparator<S> comparator) {
    super(2, comparator);
  }
}
