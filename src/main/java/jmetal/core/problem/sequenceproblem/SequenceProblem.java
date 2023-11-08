package jmetal.core.problem.sequenceproblem;

import jmetal.core.problem.Problem;
import jmetal.core.solution.sequencesolution.SequenceSolution;

public interface SequenceProblem <S extends SequenceSolution<?>> extends Problem<S>  {
    int length() ;
}
