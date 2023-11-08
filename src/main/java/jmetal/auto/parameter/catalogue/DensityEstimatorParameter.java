package jmetal.auto.parameter.catalogue;

import java.util.List;
import jmetal.auto.parameter.CategoricalParameter;
import jmetal.core.solution.Solution;
import jmetal.core.util.densityestimator.DensityEstimator;
import jmetal.core.util.densityestimator.impl.CrowdingDistanceDensityEstimator;
import jmetal.core.util.densityestimator.impl.KnnDensityEstimator;
import jmetal.core.util.errorchecking.JMetalException;

public class DensityEstimatorParameter<S extends Solution<?>> extends CategoricalParameter {

  public DensityEstimatorParameter(String name, List<String> validDensityEstimators) {
    super(name, validDensityEstimators);
  }

  public DensityEstimator<S> getParameter() {
    DensityEstimator<S> result;
    switch (value()) {
      case "crowdingDistance":
        result = new CrowdingDistanceDensityEstimator<>();
        break;
      case "knn":
        result = new KnnDensityEstimator<>(1);
        break;
      default:
        throw new JMetalException("Density estimator does not exist: " + name());
    }
    return result;
  }
}
