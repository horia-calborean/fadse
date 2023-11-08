package jmetal.core.util.comparator.constraintcomparator;

import java.util.Comparator;
import jmetal.core.solution.Solution;

/**
 * Interface representing constraint comparators
 *
 * @param <S> Solution
 */
public interface ConstraintComparator <S extends Solution<?>> extends Comparator<S> {
}
