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

package jmetal.core.util.archivewithreferencepoint.impl;

import java.util.Comparator;
import java.util.List;
import jmetal.core.solution.Solution;
import jmetal.core.util.archivewithreferencepoint.ArchiveWithReferencePoint;
import jmetal.core.util.densityestimator.DensityEstimator;
import jmetal.core.util.densityestimator.impl.CrowdingDistanceDensityEstimator;

/**
 * Class representing a {@link ArchiveWithReferencePoint} archive using a crowding distance based density estimator
 * @author Antonio J. Nebro
 */
@SuppressWarnings("serial")
public class CrowdingDistanceArchiveWithReferencePoint<S extends Solution<?>> extends ArchiveWithReferencePoint<S> {
  private final DensityEstimator<S> densityEstimator ;

  public CrowdingDistanceArchiveWithReferencePoint(int maxSize, List<Double> refPointDM) {
    super(maxSize, refPointDM, new CrowdingDistanceDensityEstimator<S>().comparator());

    densityEstimator = new CrowdingDistanceDensityEstimator<>() ;
  }

  @Override
  public Comparator<S> comparator() {
    return comparator;
  }

  @Override
  public void computeDensityEstimator() {
    densityEstimator.compute(solutions());
  }
}
