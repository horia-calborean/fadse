package jmetal.component.tests.catalogue.ea.replacement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import jmetal.component.catalogue.ea.replacement.impl.MuCommaLambdaReplacement;
import jmetal.core.problem.doubleproblem.impl.FakeDoubleProblem;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.comparator.ObjectiveComparator;
import jmetal.core.util.errorchecking.exception.InvalidConditionException;

class MuCommaLambdaReplacementTest {

  @Test
  void ReplaceReturnsAPopulationOfTheRequiredSizeIfMuIs10AndLambdaIs12() {
    int mu = 10;
    int lambda = 12;
    FakeDoubleProblem problem = new FakeDoubleProblem();
    List<DoubleSolution> population = new ArrayList<>();
    for (int i = 0; i < mu; i++) {
      population.add(problem.createSolution());
    }

    List<DoubleSolution> offspringPopulation = new ArrayList<>();
    for (int i = 0; i < lambda; i++) {
      offspringPopulation.add(problem.createSolution());
    }

    MuCommaLambdaReplacement<DoubleSolution> replacement = new MuCommaLambdaReplacement<>(new ObjectiveComparator<>(0)) ;

    assertEquals(mu, replacement.replace(population, offspringPopulation).size()) ;
  }

  @Test
  void ReplaceRaisesAnExceptionIfMuIsEqualToLambda() {
    int mu = 10;
    int lambda = 10;
    FakeDoubleProblem problem = new FakeDoubleProblem();
    List<DoubleSolution> population = new ArrayList<>();
    for (int i = 0; i < mu; i++) {
      population.add(problem.createSolution());
    }

    List<DoubleSolution> offspringPopulation = new ArrayList<>();
    for (int i = 0; i < lambda; i++) {
      offspringPopulation.add(problem.createSolution());
    }

    MuCommaLambdaReplacement<DoubleSolution> replacement = new MuCommaLambdaReplacement<>(new ObjectiveComparator<>(0)) ;


    assertThrows(
        InvalidConditionException.class, () -> replacement.replace(population, offspringPopulation)) ;
  }

  @Test
  void replaceRaisesAnExceptionIfMuIsLowerThanLambda() {
    int mu = 10;
    int lambda = 8;
    FakeDoubleProblem problem = new FakeDoubleProblem();
    List<DoubleSolution> population = new ArrayList<>();
    for (int i = 0; i < mu; i++) {
      population.add(problem.createSolution());
    }

    List<DoubleSolution> offspringPopulation = new ArrayList<>();
    for (int i = 0; i < lambda; i++) {
      offspringPopulation.add(problem.createSolution());
    }

    MuCommaLambdaReplacement<DoubleSolution> replacement = new MuCommaLambdaReplacement<>(new ObjectiveComparator<>(0)) ;


    assertThrows(InvalidConditionException.class, () -> replacement.replace(population, offspringPopulation)) ;
  }
}