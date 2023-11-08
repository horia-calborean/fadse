package jmetal.core.util.archive.impl;

import java.util.List;
import jmetal.core.solution.Solution;
import jmetal.core.util.SolutionListUtils;
import jmetal.core.util.archive.Archive;

/**
 * Archive that select the best solutions of another archive by applying the
 * {@link SolutionListUtils#distanceBasedSubsetSelection(List, int)}} function.
 *
 * @param <S> Existing archive
 */
public class BestSolutionsArchive<S extends Solution<?>> implements Archive<S> {
  private Archive<S> archive ;
  private int numberOfSolutionsToSelect ;

  public BestSolutionsArchive(Archive<S> archive, int numberOfSolutionsToSelect) {
    this.archive = archive ;
    this.numberOfSolutionsToSelect = numberOfSolutionsToSelect ;
  }

  @Override
  public boolean add(S solution) {
    return archive.add(solution);
  }

  @Override
  public S get(int index) {
    return archive.get(index);
  }

  @Override
  public List<S> solutions() {
    return SolutionListUtils.distanceBasedSubsetSelection(archive.solutions(), numberOfSolutionsToSelect);
  }

  @Override
  public int size() {
    return archive.size();
  }
}
