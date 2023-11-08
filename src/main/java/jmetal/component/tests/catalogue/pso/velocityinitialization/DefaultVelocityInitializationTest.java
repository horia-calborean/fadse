package jmetal.component.tests.catalogue.pso.velocityinitialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import jmetal.component.catalogue.pso.velocityinitialization.impl.DefaultVelocityInitialization;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.core.problem.doubleproblem.impl.FakeDoubleProblem;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.errorchecking.exception.InvalidConditionException;
import jmetal.core.util.errorchecking.exception.NullParameterException;

class DefaultVelocityInitializationTest {

  @Test
  void initializeRaisesAnExceptionIfTheSwarmIsNull() {
    assertThrows(NullParameterException.class, () -> new DefaultVelocityInitialization().initialize(null)) ;
  }

  @Test
  void initializeRaisesAnExceptionIfTheSwarmIsEmpty() {
    List<DoubleSolution> swarm = new ArrayList<>() ;
    assertThrows(InvalidConditionException.class, () -> new DefaultVelocityInitialization().initialize(swarm)) ;
  }

  @Test
  void initializeReturnsASpeedMatrixWithTheRightDimensions() {
    List<DoubleSolution> swarm = new ArrayList<>();
    DoubleProblem problem = new FakeDoubleProblem(3, 2, 0) ;

    swarm.add(problem.createSolution());
    swarm.add(problem.createSolution());
    swarm.add(problem.createSolution());
    swarm.add(problem.createSolution());

    double[][] speed = new DefaultVelocityInitialization().initialize(swarm) ;
    assertEquals(4, speed.length) ;
    assertEquals(3, speed[0].length) ;
  }

  @Test
  void initializesASpeedMatrixFullOfZeroes() {
    List<DoubleSolution> swarm = new ArrayList<>();
    DoubleProblem problem = new FakeDoubleProblem(2, 2, 0) ;

    swarm.add(problem.createSolution());
    swarm.add(problem.createSolution());

    double[][] speed = new DefaultVelocityInitialization().initialize(swarm) ;
    assertEquals(0.0, speed[0][0]) ;
    assertEquals(0.0, speed[0][1]) ;
    assertEquals(0.0, speed[1][0]) ;
    assertEquals(0.0, speed[1][1]) ;
  }
}