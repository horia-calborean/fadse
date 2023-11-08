package jmetal.algorithm.tests.omopso;

import static org.junit.Assert.assertTrue;

import java.util.List;

import jmetal.algorithm.multiobjective.omopso.OMOPSOBuilder;
import org.junit.Test;
import jmetal.core.algorithm.Algorithm;
import jmetal.core.operator.mutation.impl.NonUniformMutation;
import jmetal.core.operator.mutation.impl.UniformMutation;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.problem.multiobjective.zdt.ZDT1;
import jmetal.core.qualityindicator.QualityIndicator;
import jmetal.core.qualityindicator.impl.hypervolume.impl.PISAHypervolume;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.SolutionListUtils;
import jmetal.core.util.VectorUtils;
import jmetal.core.util.evaluator.impl.SequentialSolutionListEvaluator;

/**
 * Integration tests for algorithm OMOPSO
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class OMOPSOIT {
  Algorithm<List<DoubleSolution>> algorithm;

  @Test
  public void shouldTheAlgorithmReturnANumberOfSolutionsWhenSolvingASimpleProblem()
      throws Exception {
    DoubleProblem problem = new ZDT1();

    double mutationProbability = 1.0 / problem.numberOfVariables();

    algorithm =
        new OMOPSOBuilder(problem, new SequentialSolutionListEvaluator<DoubleSolution>())
            .setMaxIterations(250)
            .setSwarmSize(100)
            .setEta(0.0075)
            .setUniformMutation(new UniformMutation(mutationProbability, 0.5))
            .setNonUniformMutation(new NonUniformMutation(mutationProbability, 0.5, 250))
            .build();

    algorithm.run();

    List<DoubleSolution> population = algorithm.result();

    /*
    Rationale: the default problem is ZDT1, and OMOPSO, configured with standard settings, should
    return 100 solutions
    */
    assertTrue(population.size() >= 98);
  }

  @Test
  public void shouldTheHypervolumeHaveAMininumValue() throws Exception {
    DoubleProblem problem = new ZDT1();

    double mutationProbability = 1.0 / problem.numberOfVariables();

    algorithm =
        new OMOPSOBuilder(problem, new SequentialSolutionListEvaluator<DoubleSolution>())
            .setMaxIterations(250)
            .setSwarmSize(100)
            .setEta(0.0075)
            .setUniformMutation(new UniformMutation(mutationProbability, 0.5))
            .setNonUniformMutation(new NonUniformMutation(mutationProbability, 0.5, 250))
            .build();

    algorithm.run();

    List<DoubleSolution> population = algorithm.result();

    QualityIndicator hypervolume =
            new PISAHypervolume(
                    VectorUtils.readVectors("../resources/referenceFrontsCSV/ZDT1.csv", ","));

    // Rationale: the default problem is ZDT1, and OMOPSO, configured with standard settings, should
    // return find a front with a hypervolume value higher than 0.64

    double hv = hypervolume.compute(SolutionListUtils.getMatrixWithObjectiveValues(population));

    assertTrue(hv > 0.64);
  }
}
