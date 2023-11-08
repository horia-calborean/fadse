package jmetal.core.problem.binaryproblem;

import java.util.List;
import jmetal.core.problem.Problem;
import jmetal.core.solution.binarysolution.BinarySolution;

/**
 * Interface representing binary problems
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public interface BinaryProblem extends Problem<BinarySolution> {
  List<Integer> listOfBitsPerVariable() ;
  int bitsFromVariable(int index) ;
  int totalNumberOfBits() ;
}
