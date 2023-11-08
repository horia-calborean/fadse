package jmetal.core.util.legacy.qualityindicator.impl.hypervolume;

import java.io.FileNotFoundException;
import java.util.List;
import jmetal.core.util.legacy.front.Front;
import jmetal.core.util.legacy.front.impl.ArrayFront;
import jmetal.core.util.legacy.front.util.FrontUtils;
import jmetal.core.util.legacy.qualityindicator.impl.GenericIndicator;
import jmetal.core.util.point.Point;
import jmetal.core.util.point.impl.ArrayPoint;

/**
 * This interface represents implementations of the Hypervolume quality indicator
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 * @author Juan J. Durillo
 */
@SuppressWarnings("serial")
@Deprecated
public abstract class Hypervolume<S> extends GenericIndicator<S> {

  public Hypervolume() {}

  public Hypervolume(String referenceParetoFrontFile) throws FileNotFoundException {
    super(referenceParetoFrontFile);
  }

  public Hypervolume(double[] referencePoint) {
    Front referenceFront = new ArrayFront(referencePoint.length, referencePoint.length);
    for (int i = 0; i < referencePoint.length; i++) {
      Point point = new ArrayPoint(referencePoint.length);
      for (int j = 0; j < referencePoint.length; j++) {
        if (j == i) {
          double v = referencePoint[i] ;
          point.value(j, v);
        } else {
          point.value(j, 0.0);
        }
      }
      referenceFront.setPoint(i, point);
    }
    this.referenceParetoFront = referenceFront;
  }

  public Hypervolume(Front referenceParetoFront) {
    super(referenceParetoFront);
  }

  public abstract List<S> computeHypervolumeContribution(
      List<S> solutionList, List<S> referenceFrontList);

  public List<S> computeHypervolumeContribution(List<S> solutionList) {
    return this.computeHypervolumeContribution(
        solutionList, (List<S>) FrontUtils.convertFrontToSolutionList(referenceParetoFront));
  }

  public abstract double getOffset();

  public abstract void setOffset(double offset);

  @Override
  public String name() {
    return "HV";
  }

  @Override
  public boolean isTheLowerTheIndicatorValueTheBetter() {
    return false;
  }
}
