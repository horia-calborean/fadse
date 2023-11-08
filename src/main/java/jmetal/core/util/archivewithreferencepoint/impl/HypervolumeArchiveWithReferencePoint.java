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
import jmetal.core.util.comparator.HypervolumeContributionComparator;
import jmetal.core.util.legacy.qualityindicator.impl.hypervolume.Hypervolume;
import jmetal.core.util.legacy.qualityindicator.impl.hypervolume.impl.PISAHypervolume;


/**
 * Class representing a {@link ArchiveWithReferencePoint} archive using a hypervolume contribution based
 * density estimator.
 *
 * @author Antonio J. Nebro
 */
@SuppressWarnings("serial")
public class HypervolumeArchiveWithReferencePoint<S extends Solution<?>> extends ArchiveWithReferencePoint<S> {
  private Hypervolume hypervolume ;

  public HypervolumeArchiveWithReferencePoint(int maxSize, List<Double> refPointDM) {
    super(maxSize, refPointDM, new HypervolumeContributionComparator<>());

    hypervolume = new PISAHypervolume<>() ;
  }

  @Override
  public Comparator<S> comparator() {
    return comparator;
  }

  @Override
  public void computeDensityEstimator() {
    if (archive.size() > 3) {
      hypervolume
          .computeHypervolumeContribution(archive.solutions(), archive.solutions());
    }
  }
}
