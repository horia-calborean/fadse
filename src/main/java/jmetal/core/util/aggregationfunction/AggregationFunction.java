package jmetal.core.util.aggregationfunction;

import jmetal.core.util.point.impl.IdealPoint;
import jmetal.core.util.point.impl.NadirPoint;

public interface AggregationFunction {
  double compute(double[] vector, double[] weightVector, IdealPoint idealPoint, NadirPoint nadirPoint) ;
  boolean normalizeObjectives() ;
  void epsilon(double value) ;
}
