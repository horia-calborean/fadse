package jmetal.auto.parameter.catalogue;

import java.util.List;
import jmetal.auto.parameter.CategoricalParameter;
import jmetal.component.catalogue.pso.inertiaweightcomputingstrategy.InertiaWeightComputingStrategy;
import jmetal.component.catalogue.pso.inertiaweightcomputingstrategy.impl.ConstantValueStrategy;
import jmetal.component.catalogue.pso.inertiaweightcomputingstrategy.impl.LinearDecreasingStrategy;
import jmetal.component.catalogue.pso.inertiaweightcomputingstrategy.impl.LinearIncreasingStrategy;
import jmetal.component.catalogue.pso.inertiaweightcomputingstrategy.impl.RandomSelectedValueStrategy;
import jmetal.core.util.errorchecking.JMetalException;

public class InertiaWeightComputingParameter extends CategoricalParameter {
  public InertiaWeightComputingParameter(List<String> mutationOperators) {
    super("inertiaWeightComputingStrategy", mutationOperators);
  }

  public InertiaWeightComputingStrategy getParameter() {
    InertiaWeightComputingStrategy result;

    switch (value()) {
      case "constantValue":
        Double weight = (Double) findSpecificParameter("weight").value();
        result = new ConstantValueStrategy(weight) ;
        break;
      case "randomSelectedValue":
        Double weightMin = (Double) findSpecificParameter("weightMin").value();
        Double weightMax = (Double) findSpecificParameter("weightMax").value();
        result = new RandomSelectedValueStrategy(weightMin, weightMax) ;
        break;
      case "linearDecreasingValue":
        weightMin = (Double) findSpecificParameter("weightMin").value();
        weightMax = (Double) findSpecificParameter("weightMax").value();
        int iterations = (Integer) getNonConfigurableParameter("maxIterations") ;
        int swarmSize = (Integer) getNonConfigurableParameter("swarmSize") ;
        result = new LinearDecreasingStrategy(weightMin, weightMax, iterations, swarmSize) ;
        break;
      case "linearIncreasingValue":
        weightMin = (Double) findSpecificParameter("weightMin").value();
        weightMax = (Double) findSpecificParameter("weightMax").value();
        iterations = (Integer) getNonConfigurableParameter("maxIterations");
        swarmSize = (Integer) getNonConfigurableParameter("swarmSize");
        result =new LinearIncreasingStrategy(weightMin, weightMax, iterations, swarmSize) ;
        break;
      default:
        throw new JMetalException("Inertia weight computing strategy does not exist: " + name());
    }
    return result;
  }
}
