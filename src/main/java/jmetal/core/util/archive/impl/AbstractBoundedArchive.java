package jmetal.core.util.archive.impl;

import java.util.List;
import jmetal.core.solution.Solution;
import jmetal.core.util.archive.Archive;
import jmetal.core.util.archive.BoundedArchive;
import jmetal.core.util.comparator.dominanceComparator.DominanceComparator;
import jmetal.core.util.comparator.dominanceComparator.impl.DefaultDominanceComparator;

/**
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 * @param <S>
 */
public abstract class AbstractBoundedArchive<S extends Solution<?>> implements BoundedArchive<S> {
  protected NonDominatedSolutionListArchive<S> archive;
  protected int maxSize;

  protected AbstractBoundedArchive(int maxSize, DominanceComparator<S> dominanceComparator) {
    this.maxSize = maxSize;
    this.archive = new NonDominatedSolutionListArchive<>(dominanceComparator);
  }

  protected AbstractBoundedArchive(int maxSize) {
    this(maxSize, new DefaultDominanceComparator<>()) ;
  }

  @Override
  public boolean add(S solution) {
    boolean success = archive.add(solution);
    if (success) {
      prune();
    }

    return success;
  }

  @Override
  public S get(int index) {
    return solutions().get(index);
  }

  @Override
  public List<S> solutions() {
    return archive.solutions();
  }

  @Override
  public int size() {
    return archive.size();
  }

  @Override
  public int maximumSize() {
    return maxSize;
  }

  public abstract void prune();

  public Archive<S> join(Archive<S> archive) {
    archive.solutions().forEach(this::add);

    return archive;
  }
}
