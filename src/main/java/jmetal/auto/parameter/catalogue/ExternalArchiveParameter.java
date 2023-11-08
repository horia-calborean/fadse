package jmetal.auto.parameter.catalogue;

import java.util.List;
import jmetal.auto.parameter.CategoricalParameter;
import jmetal.core.solution.Solution;
import jmetal.core.util.archive.Archive;
import jmetal.core.util.archive.impl.BestSolutionsArchive;
import jmetal.core.util.archive.impl.CrowdingDistanceArchive;
import jmetal.core.util.archive.impl.HypervolumeArchive;
import jmetal.core.util.archive.impl.NonDominatedSolutionListArchive;
import jmetal.core.util.archive.impl.SpatialSpreadDeviationArchive;
import jmetal.core.util.errorchecking.JMetalException;
import jmetal.core.util.legacy.qualityindicator.impl.hypervolume.impl.WFGHypervolume;

public class ExternalArchiveParameter<S extends Solution<?>> extends CategoricalParameter {
  private int size ;
  public ExternalArchiveParameter(String parameterName, List<String> archiveTypes) {
    super(parameterName, archiveTypes);
  }

  public ExternalArchiveParameter(List<String> archiveTypes) {
    this("externalArchive", archiveTypes);
  }

  public Archive<S> getParameter() {
    Archive<S> archive;

    switch (value()) {
      case "crowdingDistanceArchive":
        archive = new CrowdingDistanceArchive<>(size) ;
        break;
      case "hypervolumeArchive":
        archive = new HypervolumeArchive<>(size, new WFGHypervolume<>()) ;
        break;
      case "spatialSpreadDeviationArchive":
        archive = new SpatialSpreadDeviationArchive<>(size) ;
        break;
      case "unboundedArchive":
        archive = new BestSolutionsArchive<>(new NonDominatedSolutionListArchive<>(), size) ;
        break;
      default:
        throw new JMetalException("Archive type does not exist: " + name());
    }
    return archive;
  }

  public void setSize(int size) {
    this.size = size ;
  }
}
