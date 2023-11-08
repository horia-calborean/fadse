package jmetal.auto.parameter.catalogue;

import java.util.List;
import jmetal.auto.parameter.CategoricalParameter;
import jmetal.component.catalogue.pso.velocityinitialization.VelocityInitialization;
import jmetal.component.catalogue.pso.velocityinitialization.impl.DefaultVelocityInitialization;
import jmetal.component.catalogue.pso.velocityinitialization.impl.SPSO2007VelocityInitialization;
import jmetal.component.catalogue.pso.velocityinitialization.impl.SPSO2011VelocityInitialization;
import jmetal.core.util.errorchecking.JMetalException;

public class VelocityInitializationParameter extends CategoricalParameter {

  public VelocityInitializationParameter(List<String> variationStrategies) {
    super("velocityInitialization", variationStrategies);
  }

  public VelocityInitialization getParameter() {
    VelocityInitialization result;

    if ("defaultVelocityInitialization".equals(value())) {
      result = new DefaultVelocityInitialization();
    } else if ("SPSO2007VelocityInitialization".equals(value())) {
      result = new SPSO2007VelocityInitialization();
    } else if ("SPSO2011VelocityInitialization".equals(value())) {
      result = new SPSO2011VelocityInitialization();
    } else {
      throw new JMetalException("Velocity initialization component unknown: " + value());
    }

    return result;
  }

  @Override
  public String name() {
    return "velocityInitialization";
  }
}
