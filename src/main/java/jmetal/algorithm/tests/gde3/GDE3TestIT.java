package jmetal.algorithm.tests.gde3;

import static org.junit.Assert.assertTrue;

import java.util.List;

import jmetal.algorithm.multiobjective.gde3.GDE3Builder;
import org.junit.Test;
import jmetal.core.algorithm.Algorithm;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.problem.multiobjective.zdt.ZDT1;
import jmetal.core.qualityindicator.QualityIndicator;
import jmetal.core.qualityindicator.impl.hypervolume.impl.PISAHypervolume;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.SolutionListUtils;
import jmetal.core.util.VectorUtils;
import jmetal.core.util.pseudorandom.JMetalRandom;

/** Created by ajnebro on 3/11/15. */
public class GDE3TestIT {
  Algorithm<List<DoubleSolution>> algorithm;

  @Test
  public void shouldTheAlgorithmReturnANumberOfSolutionsWhenSolvingASimpleProblem()
      throws Exception {
    DoubleProblem problem = new ZDT1();
    JMetalRandom.getInstance().setSeed(1446505566148L);
    algorithm = new GDE3Builder(problem).setMaxEvaluations(25000).setPopulationSize(100).build();

    algorithm.run();

    List<DoubleSolution> population = algorithm.result();

    /*
    Rationale: the default problem is ZDT4, and GDE3, configured with standard settings, should
    return 100 solutions
    */
    assertTrue(population.size() >= 99);
  }

  @Test
  public void shouldTheHypervolumeHaveAMininumValue() throws Exception {
    DoubleProblem problem = new ZDT1();

    JMetalRandom.getInstance().setSeed(1446505566148L);
    algorithm = new GDE3Builder(problem).setMaxEvaluations(25000).setPopulationSize(100).build();

    algorithm.run();

    List<DoubleSolution> population = algorithm.result();

    QualityIndicator hypervolume =
            new PISAHypervolume(
                    VectorUtils.readVectors("../resources/referenceFrontsCSV/ZDT1.csv", ","));

    // Rationale: the default problem is ZDT1, and AbYSS, configured with standard settings,
    // should return find a front with a hypervolume value higher than 0.22

    double hv = hypervolume.compute(SolutionListUtils.getMatrixWithObjectiveValues(population));

    assertTrue(hv > 0.65);
  }
}
