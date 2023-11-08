package jmetal.core.util.comparator.dominanceComparator;

import java.util.Comparator;
import jmetal.core.solution.Solution;

/**
 * Interface representing dominance comparators
 * @param <S>
 */
public interface DominanceComparator <S extends Solution<?>> extends Comparator<S> {
}
