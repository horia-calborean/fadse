package jmetal.algorithm.tests.dmopso;

import static org.junit.Assert.assertTrue;

import java.util.List;

import jmetal.algorithm.multiobjective.dmopso.DMOPSO;
import org.junit.Test;
import jmetal.core.algorithm.Algorithm;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.problem.multiobjective.zdt.ZDT1;
import jmetal.core.qualityindicator.QualityIndicator;
import jmetal.core.qualityindicator.impl.hypervolume.impl.PISAHypervolume;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.SolutionListUtils;
import jmetal.core.util.VectorUtils;

/**
 * Integration tests for algorithm DMOPSO
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class DMOPSOIT {
  Algorithm<List<DoubleSolution>> algorithm;

  @Test
  public void shouldTheAlgorithmReturnANumberOfSolutionsWhenSolvingASimpleProblem()
      throws Exception {
    DoubleProblem problem = new ZDT1();

    algorithm =
        new DMOPSO(
            problem,
            100,
            250,
            0.0,
            0.1,
            0.0,
            1.0,
            1.5,
            2.5,
            1.5,
            2.5,
            0.1,
            0.4,
            -1.0,
            -1.0,
            DMOPSO.FunctionType.TCHE,
            "",
            2);

    algorithm.run();

    List<DoubleSolution> population = algorithm.result();

    /*
    Rationale: the default problem is ZDT1, and dMOPSO, configured with standard settings, should
    return 100 solutions
    */
    assertTrue(population.size() >= 98);
  }

  @Test
  public void shouldTheHypervolumeHaveAMininumValue() throws Exception {
    DoubleProblem problem = new ZDT1();

    algorithm =
        new DMOPSO(
            problem,
            100,
            250,
            0.0,
            0.1,
            0.0,
            1.0,
            1.5,
            2.5,
            1.5,
            2.5,
            0.1,
            0.4,
            -1.0,
            -1.0,
            DMOPSO.FunctionType.TCHE,
            "",
            2);

    algorithm.run();

    List<DoubleSolution> population = algorithm.result();

    QualityIndicator hypervolume =
            new PISAHypervolume(
                    VectorUtils.readVectors("../resources/referenceFrontsCSV/ZDT1.csv", ","));

    // Rationale: the default problem is ZDT1, and AbYSS, configured with standard settings,
    // should return find a front with a hypervolume value higher than 0.22

    double hv = hypervolume.compute(SolutionListUtils.getMatrixWithObjectiveValues(population));

    assertTrue(hv > 0.64);
  }
}
