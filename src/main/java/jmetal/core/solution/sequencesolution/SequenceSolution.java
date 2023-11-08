package jmetal.core.solution.sequencesolution;

import jmetal.core.solution.Solution;

/**
 * Interface representing a sequence of values of the same type
 *
 * @param <T>
 */
public interface SequenceSolution<T> extends Solution<T> {
  int getLength();
}
