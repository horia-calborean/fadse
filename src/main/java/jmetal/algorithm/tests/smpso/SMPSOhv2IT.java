package jmetal.algorithm.tests.smpso;

import static org.junit.Assert.assertTrue;

import java.util.List;

import jmetal.algorithm.multiobjective.smpso.SMPSOBuilder;
import org.junit.Before;
import org.junit.Test;
import jmetal.core.algorithm.Algorithm;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.problem.multiobjective.zdt.ZDT4;
import jmetal.core.qualityindicator.QualityIndicator;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.SolutionListUtils;
import jmetal.core.util.VectorUtils;
import jmetal.core.util.archive.BoundedArchive;
import jmetal.core.util.archive.impl.CrowdingDistanceArchive;
import jmetal.core.util.archive.impl.HypervolumeArchive;
import jmetal.core.util.legacy.qualityindicator.impl.hypervolume.impl.WFGHypervolume;

public class SMPSOhv2IT {
  private Algorithm<List<DoubleSolution>> algorithm;
  private BoundedArchive<DoubleSolution> archive;

  @Before
  public void setup() {
    archive = new HypervolumeArchive<DoubleSolution>(100, new WFGHypervolume<DoubleSolution>());
  }

  @Test
  public void shouldTheAlgorithmReturnANumberOfSolutionsWhenSolvingASimpleProblem()
      throws Exception {
    DoubleProblem problem = new ZDT4();

    algorithm = new SMPSOBuilder(problem, archive).build();

    algorithm.run();

    List<DoubleSolution> population = algorithm.result();

    /*
    Rationale: the default problem is ZDT4, and SMPSO, configured with standard settings, should
    return 100 solutions
    */
    assertTrue(population.size() >= 98);
  }

  @Test
  public void shouldTheHypervolumeHaveAMininumValue() throws Exception {
    DoubleProblem problem = new ZDT4();

    algorithm = new SMPSOBuilder(problem, new CrowdingDistanceArchive<>(100)).build();
    algorithm.run();

    List<DoubleSolution> population = algorithm.result();

    QualityIndicator hypervolume =
            new jmetal.core.qualityindicator.impl.hypervolume.impl.PISAHypervolume(
                    VectorUtils.readVectors("../resources/referenceFrontsCSV/ZDT4.csv", ","));

    // Rationale: the default problem is ZDT4, and SMPSO, configured with standard settings, should
    // return find a front with a hypervolume value higher than 0.64

    double hv = hypervolume.compute(SolutionListUtils.getMatrixWithObjectiveValues(population));

    assertTrue(hv > 0.64);
  }
}
