package jmetal.algorithm.tests.fame;

import static org.junit.Assert.assertTrue;

import java.util.List;

import jmetal.algorithm.multiobjective.fame.FAME;
import org.junit.Test;
import jmetal.core.algorithm.Algorithm;
import jmetal.core.operator.selection.SelectionOperator;
import jmetal.core.operator.selection.impl.SpatialSpreadDeviationSelection;
import jmetal.core.problem.Problem;
import jmetal.problem.multiobjective.zdt.ZDT1;
import jmetal.core.qualityindicator.QualityIndicator;
import jmetal.core.qualityindicator.impl.hypervolume.impl.PISAHypervolume;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.SolutionListUtils;
import jmetal.core.util.VectorUtils;
import jmetal.core.util.evaluator.impl.SequentialSolutionListEvaluator;
import jmetal.core.util.pseudorandom.JMetalRandom;

public class FAMEIT {

  @Test
  public void shouldTheAlgorithmReturnANumberOfSolutionsWhenSolvingASimpleProblem() {
    Problem<DoubleSolution> problem;
    Algorithm<List<DoubleSolution>> algorithm;
    SelectionOperator<List<DoubleSolution>, DoubleSolution> selection;

    problem = new ZDT1();
    selection = new SpatialSpreadDeviationSelection<DoubleSolution>(5);

    int populationSize = 25;
    int archiveSize = 100;
    int maxEvaluations = 25000;

    algorithm =
        new FAME<>(
            problem,
            populationSize,
            archiveSize,
            maxEvaluations,
            selection,
            new SequentialSolutionListEvaluator<>());

    algorithm.run();

    /*
    Rationale: the default problem is ZDT1, and FAME, configured with standard settings, should
    return 100 solutions
    */
    assertTrue(algorithm.result().size() >= 99);
    JMetalRandom.getInstance().setSeed(System.currentTimeMillis());
  }

  @Test
  public void shouldTheHypervolumeHaveAMininumValue() throws Exception {
    Problem<DoubleSolution> problem;
    Algorithm<List<DoubleSolution>> algorithm;
    SelectionOperator<List<DoubleSolution>, DoubleSolution> selection;

    problem = new ZDT1();
    selection = new SpatialSpreadDeviationSelection<DoubleSolution>(5);

    int populationSize = 25;
    int archiveSize = 100;
    int maxEvaluations = 25000;

    algorithm =
        new FAME<>(
            problem,
            populationSize,
            archiveSize,
            maxEvaluations,
            selection,
            new SequentialSolutionListEvaluator<>());

    algorithm.run();

    QualityIndicator hypervolume =
            new PISAHypervolume(
                    VectorUtils.readVectors("../resources/referenceFrontsCSV/ZDT1.csv", ","));

    // Rationale: the default problem is ZDT1, and AbYSS, configured with standard settings,
    // should return find a front with a hypervolume value higher than 0.22

    double hv = hypervolume.compute(SolutionListUtils.getMatrixWithObjectiveValues(algorithm.result()));

    assertTrue(hv > 0.65);

    JMetalRandom.getInstance().setSeed(System.currentTimeMillis());
  }
}
