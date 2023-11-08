package jmetal.core.qualityindicator.impl;

import jmetal.core.qualityindicator.QualityIndicator;
import jmetal.core.util.VectorUtils;
import jmetal.core.util.errorchecking.Check;

/**
 * This class implements the generational distance indicator.
 * Reference: Van Veldhuizen, D.A., Lamont, G.B.: Multiobjective Evolutionary
 * Algorithm Research: A History and Analysis.
 * Technical Report TR-98-03, Dept. Elec. Comput. Eng., Air Force
 * Inst. Technol. (1998)
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 * @author Juan J. Durillo
 */
@SuppressWarnings("serial")
public class GenerationalDistance extends QualityIndicator {
  private double pow = 2.0;

  /**
   * Default constructor
   */
  public GenerationalDistance() {
  }

  /**
   * Constructor
   *
   * @param referenceFront
   */
  public GenerationalDistance(double[][] referenceFront) {
    super(referenceFront) ;
  }

  /**
   * Evaluate() method
   * @param front
   * @return
   */
  @Override public double compute(double[][] front) {
    Check.notNull(front);

    return generationalDistance(front, referenceFront);
  }

  /**
   * Returns the generational distance value for a given front
   *
   * @param front           The front
   * @param referenceFront The reference pareto front
   */
  public double generationalDistance(double[][] front, double[][] referenceFront) {
    double sum = 0.0;
    for (int i = 0; i < front.length;  i++) {
      sum += Math.pow(VectorUtils.distanceToClosestVector(front[i], referenceFront), pow);
    }

    sum = Math.pow(sum, 1.0 / pow);

    return sum / front.length;
  }

  @Override public String name() {
    return "GD" ;
  }

  @Override
  public String description() {
    return "Generational distance quality indicator" ;
  }

  @Override
  public boolean isTheLowerTheIndicatorValueTheBetter() {
    return true ;
  }
}
