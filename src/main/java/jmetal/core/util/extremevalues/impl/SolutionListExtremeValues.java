package jmetal.core.util.extremevalues.impl;

import java.util.List;
import jmetal.core.solution.Solution;
import jmetal.core.util.extremevalues.ExtremeValuesFinder;
import jmetal.core.util.legacy.front.impl.ArrayFront;

/**
 * Class for finding the extreme values of a list of objects
 *
 * @author Antonio J. Nebro
 */
public class SolutionListExtremeValues implements ExtremeValuesFinder <List<Solution<?>>, List<Double>> {

  @Override public List<Double> findLowestValues(List<Solution<?>> solutionList) {
	  return new FrontExtremeValues().findLowestValues(new ArrayFront(solutionList));
  }

  @Override public List<Double> findHighestValues(List<Solution<?>> solutionList) {
	  return new FrontExtremeValues().findHighestValues(new ArrayFront(solutionList));
  }
}
