package jmetal.component.tests.catalogue.ea.replacement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import jmetal.component.catalogue.ea.replacement.impl.MuPlusLambdaReplacement;
import jmetal.core.problem.doubleproblem.impl.FakeDoubleProblem;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.comparator.ObjectiveComparator;

class MuPlusLambdaReplacementTest {

  @Test
  public void replaceReturnsAPopulationOfTheRequiredSizeIfMuIs10AndLambdaIs2() {
    int mu = 10;
    int lambda = 2;
    FakeDoubleProblem problem = new FakeDoubleProblem();
    List<DoubleSolution> population = new ArrayList<>();
    for (int i = 0; i < mu; i++) {
      population.add(problem.createSolution());
    }

    List<DoubleSolution> offspringPopulation = new ArrayList<>();
    for (int i = 0; i < lambda; i++) {
      offspringPopulation.add(problem.createSolution());
    }

    MuPlusLambdaReplacement<DoubleSolution> replacement = new MuPlusLambdaReplacement<>(new ObjectiveComparator<>(0)) ;

    assertEquals(mu, replacement.replace(population, offspringPopulation).size()) ;
  }
}