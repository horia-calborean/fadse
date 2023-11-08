package jmetal.component.tests.catalogue.pso.globalbestinitialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import jmetal.component.catalogue.pso.globalbestinitialization.impl.DefaultGlobalBestInitialization;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.core.problem.doubleproblem.impl.FakeDoubleProblem;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.archive.BoundedArchive;
import jmetal.core.util.errorchecking.exception.InvalidConditionException;
import jmetal.core.util.errorchecking.exception.NullParameterException;

class DefaultGlobalBestInitializationTest {
  @Test
  void initializeRaisesAnExceptionIfTheSwarmIsNull() {
    assertThrows(NullParameterException.class, () -> new DefaultGlobalBestInitialization().initialize(null, Mockito.mock(
        BoundedArchive.class))) ;
  }

  @Test
  void initializeRaisesAnExceptionIfTheGlobalBestIsNull() {
    assertThrows(NullParameterException.class, () -> new DefaultGlobalBestInitialization().initialize(new ArrayList<>(), null)) ;
  }

  @Test
  void initializeRaisesAnExceptionIfTheSwarmIsEmpty() {
    assertThrows(InvalidConditionException.class, () -> new DefaultGlobalBestInitialization().initialize(new ArrayList<>(), Mockito.mock(BoundedArchive.class))) ;
  }

  @Test
  void shouldInitializeReturnAGlobalBestArchiveWithASolution() {
    List<DoubleSolution> swarm = new ArrayList<>();
    DoubleProblem problem = new FakeDoubleProblem(3, 2, 0) ;

    DoubleSolution particle = problem.createSolution() ;
    swarm.add(particle);

    BoundedArchive<DoubleSolution> archive = Mockito.mock(BoundedArchive.class) ;
    List<DoubleSolution> archiveList = List.of(particle) ;
    Mockito.when(archive.solutions()).thenReturn(archiveList) ;

    BoundedArchive<DoubleSolution> globalBest = new DefaultGlobalBestInitialization().initialize(swarm, archive) ;

    assertEquals(1, globalBest.solutions().size());
    assertSame(particle, globalBest.solutions().get(0));
  }
}