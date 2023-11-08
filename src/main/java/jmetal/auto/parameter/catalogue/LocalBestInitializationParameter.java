package jmetal.auto.parameter.catalogue;

import java.util.List;
import jmetal.auto.parameter.CategoricalParameter;
import jmetal.component.catalogue.pso.localbestinitialization.LocalBestInitialization;
import jmetal.component.catalogue.pso.localbestinitialization.impl.DefaultLocalBestInitialization;
import jmetal.core.util.errorchecking.JMetalException;

public class LocalBestInitializationParameter extends CategoricalParameter {
  public LocalBestInitializationParameter(List<String> localBestInitializationStrategies) {
    super("localBestInitialization", localBestInitializationStrategies);
  }

  public LocalBestInitialization getParameter() {
    LocalBestInitialization result;

    if ("defaultLocalBestInitialization".equals(value())) {
      result = new DefaultLocalBestInitialization();
    } else {
      throw new JMetalException("Local best initialization component unknown: " + value());
    }

    return result;
  }

  @Override
  public String name() {
    return "localBestInitialization";
  }
}
