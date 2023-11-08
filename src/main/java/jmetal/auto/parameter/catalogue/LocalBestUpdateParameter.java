package jmetal.auto.parameter.catalogue;

import java.util.List;
import jmetal.auto.parameter.CategoricalParameter;
import jmetal.component.catalogue.pso.localbestupdate.LocalBestUpdate;
import jmetal.component.catalogue.pso.localbestupdate.impl.DefaultLocalBestUpdate;
import jmetal.core.util.comparator.dominanceComparator.DominanceComparator;
import jmetal.core.util.errorchecking.JMetalException;

public class LocalBestUpdateParameter extends CategoricalParameter {
  public LocalBestUpdateParameter(List<String> localBestUpdateStrategies) {
    super("localBestUpdate", localBestUpdateStrategies);
  }

  public LocalBestUpdate getParameter(DominanceComparator comparator) {
    LocalBestUpdate result;

    if ("defaultLocalBestUpdate".equals(value())) {
      result = new DefaultLocalBestUpdate(comparator);
    } else {
      throw new JMetalException("Local best update component unknown: " + value());
    }

    return result;
  }
}
