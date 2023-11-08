package jmetal.core.problem.permutationproblem.impl;

import jmetal.core.problem.permutationproblem.PermutationProblem;
import jmetal.core.solution.permutationsolution.PermutationSolution;
import jmetal.core.solution.permutationsolution.impl.IntegerPermutationSolution;

@SuppressWarnings("serial")
public abstract class AbstractIntegerPermutationProblem
    implements PermutationProblem<PermutationSolution<Integer>> {

  @Override
  public PermutationSolution<Integer> createSolution() {
    return new IntegerPermutationSolution(length(), numberOfObjectives(), numberOfConstraints()) ;
  }

  @Override
  public int length() {
    return numberOfVariables();
  }
}
