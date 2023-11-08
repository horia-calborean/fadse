package jmetal.component.tests.catalogue.common.evaluation;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import jmetal.component.catalogue.common.evaluation.Evaluation;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import jmetal.component.catalogue.common.evaluation.impl.SequentialEvaluation;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.errorchecking.exception.NullParameterException;

class SequentialEvaluationTest {

  @Test
  void invokeTheConstructorWithANullProblemRaisesAnException() {
    assertThatThrownBy(() -> new SequentialEvaluation<>(null)).isInstanceOf(NullParameterException.class) ;
  }

  @Test
  void theConstructorInitializesTheNumberOfComputedEvaluations() {
    DoubleProblem problem = mock(DoubleProblem.class) ;
    Evaluation<DoubleSolution> evaluation = new SequentialEvaluation<>(problem) ;

    assertThat(evaluation.computedEvaluations()).isZero() ;
  }

  @Test
  void evaluateANullListRaisesAnException() {
    DoubleProblem problem = mock(DoubleProblem.class) ;
    Evaluation<DoubleSolution> evaluation = new SequentialEvaluation<>(problem) ;

    assertThatThrownBy(() -> evaluation.evaluate(null)).isInstanceOf(NullParameterException.class) ;
  }

  @Test
  void evaluateAnEmptyListDoesNotIncrementTheNumberOfComputedEvaluations() {
    DoubleProblem problem = mock(DoubleProblem.class) ;
    Evaluation<DoubleSolution> evaluation = new SequentialEvaluation<>(problem) ;

    evaluation.evaluate(new ArrayList<>()) ;

    assertThat(evaluation.computedEvaluations()).isZero() ;
  }

  @Test
  void evaluateAnEmptyListWithASolutionWorksProperly() {
    DoubleProblem problem = mock(DoubleProblem.class) ;
    Evaluation<DoubleSolution> evaluation = new SequentialEvaluation<>(problem) ;

    evaluation.evaluate(List.of(mock(DoubleSolution.class))) ;

    assertThat(evaluation.computedEvaluations()).isEqualTo(1) ;
    verify(problem, times(1)).evaluate(Mockito.any()) ;
  }

  @Test
  void evaluateAnListWithNSolutionsWorksProperly() {
    DoubleProblem problem = mock(DoubleProblem.class) ;
    Evaluation<DoubleSolution> evaluation = new SequentialEvaluation<>(problem) ;

    int numberOfSolutions = 10 ;
    List<DoubleSolution> solutions = new ArrayList<>(numberOfSolutions) ;
    IntStream.range(0,numberOfSolutions).forEach(i -> solutions.add(mock(DoubleSolution.class))) ;

    evaluation.evaluate(solutions) ;

    assertThat(evaluation.computedEvaluations()).isEqualTo(numberOfSolutions) ;
    verify(problem, times(numberOfSolutions)).evaluate(Mockito.any()) ;
  }

  @Test
  void theProblemMethodReturnsTheProblem() {
    DoubleProblem problem = mock(DoubleProblem.class) ;
    Evaluation<DoubleSolution> evaluation = new SequentialEvaluation<>(problem) ;

    assertSame(problem, evaluation.problem()) ;
  }
}