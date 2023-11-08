package jmetal.core.util.extremevalues.impl;

import java.util.ArrayList;
import java.util.List;
import jmetal.core.util.errorchecking.JMetalException;
import jmetal.core.util.extremevalues.ExtremeValuesFinder;
import jmetal.core.util.legacy.front.Front;

/**
 * Class for finding the extreme values of front objects
 *
 * @author Antonio J. Nebro
 */
public class FrontExtremeValues implements ExtremeValuesFinder <Front, List<Double>> {

  @Override public List<Double> findLowestValues(Front front) {
    List<Double> minimumValue = new ArrayList<>() ;

    if (front == null) {
      throw new JMetalException("The front is null") ;
    } else if (front.getNumberOfPoints() == 0) {
      throw new JMetalException("The front is empty") ;
    }

    int numberOfObjectives = front.getPointDimensions() ;

    for (int i = 0; i < numberOfObjectives; i++) {
      minimumValue.add(Double.POSITIVE_INFINITY);
    }

    for (int i = 0 ; i < front.getNumberOfPoints(); i++) {
      for (int j = 0; j < numberOfObjectives; j++) {
        if (front.getPoint(i).value(j) < minimumValue.get(j)) {
          minimumValue.set(j, front.getPoint(i).value(j));
        }
      }
    }

    return minimumValue;
  }

  @Override public List<Double> findHighestValues(Front front) {
    List<Double> maximumValue = new ArrayList<>() ;

    if (front == null) {
      throw new JMetalException("The front is null") ;
    } else if (front.getNumberOfPoints() == 0) {
      throw new JMetalException("The front is empty") ;
    }

    int numberOfObjectives = front.getPointDimensions() ;

    for (int i = 0; i < numberOfObjectives; i++) {
      maximumValue.add(Double.NEGATIVE_INFINITY);
    }

    for (int i = 0 ; i < front.getNumberOfPoints(); i++) {
      for (int j = 0; j < numberOfObjectives; j++) {
        if (front.getPoint(i).value(j) > maximumValue.get(j)) {
          maximumValue.set(j, front.getPoint(i).value(j));
        }
      }
    }

    return maximumValue;
  }
}
