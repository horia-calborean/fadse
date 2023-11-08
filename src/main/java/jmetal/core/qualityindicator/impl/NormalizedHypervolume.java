package jmetal.core.qualityindicator.impl;

import jmetal.core.qualityindicator.QualityIndicator;
import jmetal.core.qualityindicator.impl.hypervolume.Hypervolume;
import jmetal.core.qualityindicator.impl.hypervolume.impl.PISAHypervolume;

/**
 * Class providing an implementation of the normalized hypervolume, which is calculated as follows:
 * relative hypervolume = 1 - (HV of the front / HV of the reference front).
 * Before computing this indicator it must be checked that the HV of the reference front is not zero.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class NormalizedHypervolume extends QualityIndicator {
  private double referenceFrontHypervolume;
  private Hypervolume hypervolume;

  public NormalizedHypervolume() {
  }

  public NormalizedHypervolume(double[] referencePoint) {
    // TODO: add a unit test
    double[][] referenceFront = {referencePoint};
    hypervolume = new PISAHypervolume(referenceFront);

    referenceFrontHypervolume = hypervolume.compute(referenceFront);
  }

  public NormalizedHypervolume(double[][] referenceFront) {
    super(referenceFront);
    hypervolume = new PISAHypervolume(referenceFront);

    referenceFrontHypervolume = hypervolume.compute(referenceFront);
  }

  @Override
  public void referenceFront(double[][] referenceFront) {
    super.referenceFront(referenceFront);

    hypervolume = new PISAHypervolume(referenceFront);
    referenceFrontHypervolume = hypervolume.compute(referenceFront);
  }

  @Override
  public String name() {
    return "NHV";
  }

  @Override
  public String description() {
    return "Normalized hypervolume";
  }


  @Override
  public boolean isTheLowerTheIndicatorValueTheBetter() {
    return true;
  }

  @Override
  public double compute(double[][] front) {
    double hypervolumeValue = hypervolume.compute(front);

    return 1 - (hypervolumeValue / referenceFrontHypervolume);
  }
}
