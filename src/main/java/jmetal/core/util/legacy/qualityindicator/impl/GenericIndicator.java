package jmetal.core.util.legacy.qualityindicator.impl;

import java.io.FileNotFoundException;
import java.util.List;
import jmetal.core.util.errorchecking.Check;
import jmetal.core.util.legacy.front.Front;
import jmetal.core.util.legacy.front.impl.ArrayFront;
import jmetal.core.util.legacy.qualityindicator.QualityIndicator;
import jmetal.core.util.naming.impl.SimpleDescribedEntity;

/**
 * Abstract class representing quality indicators that need a reference front to be computed
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
@Deprecated
public abstract class GenericIndicator<S>
    extends SimpleDescribedEntity
    implements QualityIndicator<List<S>, Double> {

  protected Front referenceParetoFront = null ;
  /**
   * Default constructor
   */
  public GenericIndicator() {
  }

  public GenericIndicator(String referenceParetoFrontFile) throws FileNotFoundException {
    setReferenceParetoFront(referenceParetoFrontFile);
  }

  public GenericIndicator(Front referenceParetoFront) {
   Check.notNull(referenceParetoFront);

    this.referenceParetoFront = referenceParetoFront ;
  }

  public void setReferenceParetoFront(String referenceParetoFrontFile) throws FileNotFoundException {
    Check.notNull(referenceParetoFrontFile);

    Front front = new ArrayFront(referenceParetoFrontFile);
    referenceParetoFront = front ;
  }

  public void setReferenceParetoFront(Front referenceFront) {
    Check.notNull(referenceFront);

    referenceParetoFront = referenceFront ;
  }

  /**
   * This method returns true if lower indicator values are preferred and false otherwise
   * @return
   */
  public abstract boolean isTheLowerTheIndicatorValueTheBetter() ;

  public Front getReferenceParetoFront() {
    return referenceParetoFront ;
  }
}
