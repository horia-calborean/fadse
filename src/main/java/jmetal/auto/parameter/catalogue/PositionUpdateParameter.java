package jmetal.auto.parameter.catalogue;

import java.util.List;
import jmetal.auto.parameter.CategoricalParameter;
import jmetal.component.catalogue.pso.positionupdate.PositionUpdate;
import jmetal.component.catalogue.pso.positionupdate.impl.DefaultPositionUpdate;
import jmetal.core.util.bounds.Bounds;
import jmetal.core.util.errorchecking.JMetalException;

public class PositionUpdateParameter extends CategoricalParameter {

  public PositionUpdateParameter(List<String> positionUpdateStrategies) {
    super("positionUpdate", positionUpdateStrategies);
  }

  public PositionUpdate getParameter() {
    PositionUpdate result;
    switch (value()) {
      case "defaultPositionUpdate":
        List<Bounds<Double>> positionBounds = (List<Bounds<Double>>) getNonConfigurableParameter(
            "positionBounds");
        double velocityChangeWhenLowerLimitIsReached = (double) findSpecificParameter(
            "velocityChangeWhenLowerLimitIsReached").value();
        double velocityChangeWhenUpperLimitIsReached = (double) findSpecificParameter(
            "velocityChangeWhenUpperLimitIsReached").value();

        result = new DefaultPositionUpdate(velocityChangeWhenLowerLimitIsReached,
            velocityChangeWhenUpperLimitIsReached, positionBounds);
        break;
      default:
        throw new JMetalException("Position update component unknown: " + value());
    }
    return result;
  }
}
