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

package jmetal.core.operator.selection.impl;

import java.util.Comparator;
import java.util.List;
import jmetal.core.operator.selection.SelectionOperator;
import jmetal.core.solution.Solution;
import jmetal.core.util.ListUtils;
import jmetal.core.util.SolutionUtils;
import jmetal.core.util.comparator.RankingAndSSDComparator;
import jmetal.core.util.errorchecking.Check;

/**
 * Spatial Spread Deviation selection operator
 *
 * @author Alejandro Santiago <aurelio.santiago@upalt.edu.mx>
 */
@SuppressWarnings("serial")
public class SpatialSpreadDeviationSelection<S extends Solution<?>>
    implements SelectionOperator<List<S>, S> {
  private Comparator<S> comparator;

  private final int numberOfTournaments;

  /** Constructor */
  public SpatialSpreadDeviationSelection(int numberOfTournaments) {
    this(new RankingAndSSDComparator<>(), numberOfTournaments) ;
  }

  /** Constructor */
  public SpatialSpreadDeviationSelection(Comparator<S> comparator, int numberOfTournaments) {
    this.numberOfTournaments = numberOfTournaments;
    this.comparator = comparator ;
  }

  @Override
  /** Execute() method */
  public S execute(List<S> solutionList) {
    Check.notNull(solutionList) ;
    Check.collectionIsNotEmpty(solutionList);

    S result;
    if (solutionList.size() == 1) {
      result = solutionList.get(0);
    } else {
      result = ListUtils.randomSelectionWithoutReplacement(1, solutionList).get(0);
      int count = 1; // at least 2 solutions are compared
      do {
        S candidate = ListUtils.randomSelectionWithoutReplacement(1, solutionList).get(0);
        result = SolutionUtils.getBestSolution(result, candidate, comparator) ;
      } while (++count < this.numberOfTournaments);
    }

    return result;
  }
}
