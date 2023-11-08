package jmetal.core.problem.binaryproblem.impl;

import jmetal.core.problem.binaryproblem.BinaryProblem;
import jmetal.core.solution.binarysolution.BinarySolution;
import jmetal.core.solution.binarysolution.impl.DefaultBinarySolution;

@SuppressWarnings("serial")
public abstract class AbstractBinaryProblem implements BinaryProblem {

  @Override
  public int bitsFromVariable(int index) {
    return listOfBitsPerVariable().get(index);
  }

  @Override
  public int totalNumberOfBits() {
    int count = 0;
    for (int i = 0; i < this.numberOfVariables(); i++) {
      count += this.listOfBitsPerVariable().get(i);
    }

    return count;
  }

  @Override
  public BinarySolution createSolution() {
    return new DefaultBinarySolution(listOfBitsPerVariable(), numberOfObjectives(), numberOfConstraints());
  }
}
