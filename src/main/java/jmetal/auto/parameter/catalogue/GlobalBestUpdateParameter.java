package jmetal.auto.parameter.catalogue;

import java.util.List;
import jmetal.auto.parameter.CategoricalParameter;
import jmetal.component.catalogue.pso.globalbestupdate.GlobalBestUpdate;
import jmetal.component.catalogue.pso.globalbestupdate.impl.DefaultGlobalBestUpdate;
import jmetal.core.util.errorchecking.JMetalException;

public class GlobalBestUpdateParameter extends CategoricalParameter {
  public GlobalBestUpdateParameter(List<String> updateStrategies) {
    super("globalBestUpdate", updateStrategies);
  }

  public GlobalBestUpdate getParameter() {
    GlobalBestUpdate result;
    if ("defaultGlobalBestUpdate".equals(value())) {
      result = new DefaultGlobalBestUpdate();
    } else {
      throw new JMetalException("Global Best Update component unknown: " + value());
    }
    return result;
  }
}
