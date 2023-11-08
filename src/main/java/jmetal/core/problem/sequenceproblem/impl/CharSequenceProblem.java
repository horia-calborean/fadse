package jmetal.core.problem.sequenceproblem.impl;

import jmetal.core.problem.sequenceproblem.SequenceProblem;
import jmetal.core.solution.sequencesolution.impl.CharSequenceSolution;

@SuppressWarnings("serial")
public abstract class CharSequenceProblem implements SequenceProblem<CharSequenceSolution> {

  @Override
  public CharSequenceSolution createSolution() {
    return new CharSequenceSolution(length(), numberOfObjectives()) ;
  }
}
