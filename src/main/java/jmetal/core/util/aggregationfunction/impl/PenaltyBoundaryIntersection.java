package jmetal.core.util.aggregationfunction.impl;

import jmetal.core.util.aggregationfunction.AggregationFunction;
import jmetal.core.util.point.impl.IdealPoint;
import jmetal.core.util.point.impl.NadirPoint;

public class PenaltyBoundaryIntersection implements AggregationFunction {
  private double epsilon = 0.000001 ;
  private boolean normalizeObjectives ;

  private final double theta ;

  public PenaltyBoundaryIntersection() {
    this(5.0, false) ;
  }

  public PenaltyBoundaryIntersection(double theta, boolean normalizeObjectives) {
    this.theta = theta ;
    this.normalizeObjectives = normalizeObjectives ;
  }

  @Override
  public double compute(double[] vector, double[] weightVector, IdealPoint idealPoint, NadirPoint nadirPoint) {
    double d1, d2, nl;

    d1 = d2 = nl = 0.0;

    for (int i = 0; i < vector.length; i++) {
      double value ;
      if (normalizeObjectives) {
        value = (vector[i] - idealPoint.value(i))/(nadirPoint.value(i)-idealPoint.value(i)+epsilon) ;
      } else {
        value = vector[i] - idealPoint.value(i) ;
      }
      d1 += value * weightVector[i];
      nl += Math.pow(weightVector[i], 2.0);
    }
    nl = Math.sqrt(nl);
    d1 = Math.abs(d1) / nl;

    for (int i = 0; i < vector.length; i++) {
      double value ;
      if (normalizeObjectives) {
        value = (vector[i] - idealPoint.value(i))/(nadirPoint.value(i)-idealPoint.value(i)) ;
      } else {
        value = vector[i] - idealPoint.value(i);
      }
      d2 += Math.pow(value - d1 * (weightVector[i] / nl), 2.0);
    }
    d2 = Math.sqrt(d2);

    return (d1 + theta * d2) ;
  }

  @Override
  public void epsilon(double epsilon) {
    this.epsilon = epsilon ;
  }

  @Override
  public boolean normalizeObjectives() {
    return this.normalizeObjectives ;
  }
}
