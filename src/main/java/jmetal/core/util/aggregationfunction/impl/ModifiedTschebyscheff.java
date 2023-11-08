package jmetal.core.util.aggregationfunction.impl;

import jmetal.core.util.aggregationfunction.AggregationFunction;
import jmetal.core.util.point.impl.IdealPoint;
import jmetal.core.util.point.impl.NadirPoint;

public class ModifiedTschebyscheff implements AggregationFunction {
  private boolean normalizeObjectives ;
  private double epsilon = 0.000001 ;


  public ModifiedTschebyscheff(boolean normalizeObjectives) {
    this.normalizeObjectives = normalizeObjectives ;
  }

  @Override
  public double compute(double[] vector, double[] weightVector, IdealPoint idealPoint, NadirPoint nadirPoint) {
    double maxFun = -1.0e+30;

    for (int n = 0; n < vector.length; n++) {
      double diff ;
      if (normalizeObjectives) {
        diff = Math.abs((vector[n] - idealPoint.value(n))/(nadirPoint.value(n)-idealPoint.value(n)+epsilon));
      } else {
        diff = Math.abs(vector[n] - idealPoint.value(n));
      }

      double feval;
      if (weightVector[n] == 0) {
        feval = 0.0000001 * diff;
      } else {
        feval = diff * 1.0/weightVector[n];
      }
      if (feval > maxFun) {
        maxFun = feval;
      }
    }

    return maxFun;
  }

  @Override
  public void epsilon(double epsilon) {
    this.epsilon = epsilon ;
  }

  @Override
  public boolean normalizeObjectives() {
    return this.normalizeObjectives;
  }
}
