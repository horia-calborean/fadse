//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jmetal.core.util.archive.impl;

import java.util.Comparator;
import jmetal.core.solution.Solution;
import jmetal.core.util.SolutionListUtils;
import jmetal.core.util.comparator.SpatialSpreadDeviationComparator;
import jmetal.core.util.solutionattribute.DensityEstimator;
import jmetal.core.util.solutionattribute.impl.SpatialSpreadDeviation;

/**
 * @author Alejandro Santiago <aurelio.santiago@upalt.edu.mx>
 */
@SuppressWarnings("serial")
public class SpatialSpreadDeviationArchive<S extends Solution<?>> extends
    AbstractBoundedArchive<S> {
  private Comparator<S> crowdingDistanceComparator;
  private DensityEstimator<S> crowdingDistance ;

  public SpatialSpreadDeviationArchive(int maxSize) {
    super(maxSize);
    crowdingDistanceComparator = new SpatialSpreadDeviationComparator<S>() ;
    crowdingDistance = new SpatialSpreadDeviation<S>() ;
  }

  @Override
  public void prune() {
    if (solutions().size() > maximumSize()) {
      computeDensityEstimator();
      S worst = new SolutionListUtils().findWorstSolution(solutions(), crowdingDistanceComparator) ;
      solutions().remove(worst);
    }
  }

  @Override
  public Comparator<S> comparator() {
    return crowdingDistanceComparator ;
  }

  @Override
  public void computeDensityEstimator() {
    crowdingDistance.computeDensityEstimator(solutions());
  }
}
