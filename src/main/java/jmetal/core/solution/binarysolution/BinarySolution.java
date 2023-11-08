package jmetal.core.solution.binarysolution;

import jmetal.core.solution.Solution;
import jmetal.core.util.binarySet.BinarySet;

/**
 * Interface representing binary (bitset) solutions
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public interface BinarySolution extends Solution<BinarySet> {
  int getNumberOfBits(int index) ;
  int getTotalNumberOfBits() ;
}
