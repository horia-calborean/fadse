package jmetal.core.util.point.comparator;

import java.util.Comparator;
import jmetal.core.util.errorchecking.Check;
import jmetal.core.util.point.Point;

/**
 * This class implements the Comparator interface for comparing two points.
 * The order used is lexicographical order.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 * @author Juan J. Durillo

 */
public class LexicographicalPointComparator implements Comparator<Point> {

  /**
   * The compare method compare the objects o1 and o2.
   *
   * @param pointOne An object that reference a double[]
   * @param pointTwo An object that reference a double[]
   * @return The following value: -1 if point1 < point2, 1 if point1 > point2 or 0 in other case.
   */
  @Override
  public int compare(Point pointOne, Point pointTwo) {
    Check.notNull(pointOne);
    Check.notNull(pointTwo);

    int index = 0;
    while ((index < pointOne.dimension())
        && (index < pointTwo.dimension())
        && pointOne.value(index) == pointTwo.value(index)) {
      index++;
    }

    int result = 0 ;
    if ((index >= pointOne.dimension()) || (index >= pointTwo.dimension())) {
      result = 0;
    } else if (pointOne.value(index) < pointTwo.value(index)) {
      result = -1;
    } else if (pointOne.value(index) > pointTwo.value(index)) {
      result = 1;
    }
    return result ;
  }
}
