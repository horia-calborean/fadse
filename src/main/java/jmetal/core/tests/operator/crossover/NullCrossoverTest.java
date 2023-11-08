package jmetal.core.tests.operator.crossover;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.util.ArrayList;
import java.util.List;

import jmetal.core.operator.crossover.CrossoverOperator;
import org.junit.Test;
import jmetal.core.operator.crossover.impl.NullCrossover;
import jmetal.core.problem.Problem;
import jmetal.core.problem.doubleproblem.impl.FakeDoubleProblem;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.errorchecking.exception.InvalidConditionException;
import jmetal.core.util.errorchecking.exception.NullParameterException;

/**
 * Created by ajnebro on 10/6/15.
 */
public class NullCrossoverTest {

  @Test (expected = NullParameterException.class)
  public void shouldExecuteRaiseAnExceptionIfTheParameterListIsNull() {
    new NullCrossover<DoubleSolution>().execute(null) ;
  }

  @Test (expected = InvalidConditionException.class)
  public void shouldExecuteRaiseAnExceptionIfTheParameterListHasNotTwoElements() {
    new NullCrossover<DoubleSolution>().execute(new ArrayList<>()) ;
  }

  @Test
  public void shouldExecuteReturnTwoDifferentObjectsWhichAreEquals() {
    Problem<DoubleSolution> problem = new FakeDoubleProblem() ;
    List<DoubleSolution> parents = new ArrayList<>(2) ;
    parents.add(problem.createSolution()) ;
    parents.add(problem.createSolution()) ;

    CrossoverOperator<DoubleSolution> crossover;
    crossover = new NullCrossover<>() ;

    List<DoubleSolution> offspring = crossover.execute(parents);
    assertNotSame(parents.get(0), offspring.get(0)) ;
    assertNotSame(parents.get(1), offspring.get(1)) ;

    assertEquals(parents.get(0), offspring.get(0)) ;
    assertEquals(parents.get(1), offspring.get(1)) ;
  }
}
