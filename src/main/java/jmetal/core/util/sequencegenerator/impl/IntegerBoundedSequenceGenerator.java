package jmetal.core.util.sequencegenerator.impl;

import jmetal.core.util.errorchecking.Check;
import jmetal.core.util.sequencegenerator.SequenceGenerator;

/**
 * This class generates a bounded sequence of consecutive integer numbers. When the last number is generated, the
 * sequence starts again.
 *
 * @author Antonio J. Nebro
 */
public class IntegerBoundedSequenceGenerator implements SequenceGenerator<Integer> {
  private int index;
  private int size ;

  public IntegerBoundedSequenceGenerator(int size) {
    Check.that(size > 0, "Size " + size + " is not a positive number greater than zero");
    this.size = size ;
    index = 0;
  }

  @Override
  public Integer getValue() {
    return index ;
  }

  @Override
  public void generateNext() {
    index++;
    if (index == size) {
      index = 0;
    }
  }

  @Override
  public int getSequenceLength() {
    return size ;
  }
}
