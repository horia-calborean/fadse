package jmetal.algorithm.tests.microfame;

import static org.junit.Assert.assertTrue;

import java.util.List;

import jmetal.algorithm.multiobjective.microfame.MicroFAME;
import org.junit.Test;
import jmetal.core.algorithm.Algorithm;
import jmetal.algorithm.multiobjective.microfame.util.HVTournamentSelection;
import jmetal.core.operator.crossover.impl.NullCrossover;
import jmetal.core.operator.mutation.impl.NullMutation;
import jmetal.core.operator.selection.SelectionOperator;
import jmetal.core.problem.Problem;
import jmetal.problem.multiobjective.zdt.ZDT1;
import jmetal.core.qualityindicator.QualityIndicator;
import jmetal.core.qualityindicator.impl.hypervolume.impl.PISAHypervolume;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.SolutionListUtils;
import jmetal.core.util.VectorUtils;
import jmetal.core.util.pseudorandom.JMetalRandom;

public class MicroFAMEIT {

  @Test
  public void shouldTheAlgorithmReturnANumberOfSolutionsWhenSolvingASimpleProblem() {
    Problem<DoubleSolution> problem;
    Algorithm<List<DoubleSolution>> algorithm;
    SelectionOperator<List<DoubleSolution>, DoubleSolution> selection;

    int archiveSize = 100;
    int evaluations = 25000;

    problem = new ZDT1();

    var crossover = new NullCrossover<DoubleSolution>();
    var mutation = new NullMutation<DoubleSolution>();
    selection = new HVTournamentSelection(5);
    algorithm = new MicroFAME<>(problem, evaluations, archiveSize, crossover, mutation, selection);

    algorithm.run();

    /*
    Rationale: the default problem is ZDT1, and MicroFAME, configured with standard settings, should
    return 100 solutions
    */
    assertTrue(algorithm.result().size() >= 98);
    JMetalRandom.getInstance().setSeed(System.currentTimeMillis());
  }

  @Test
  public void shouldTheHypervolumeHaveAMininumValue() throws Exception {
    Problem<DoubleSolution> problem;
    Algorithm<List<DoubleSolution>> algorithm;
    SelectionOperator<List<DoubleSolution>, DoubleSolution> selection;

    int archiveSize = 100;
    int evaluations = 25000;

    problem = new ZDT1();

    var crossover = new NullCrossover<DoubleSolution>();
    var mutation = new NullMutation<DoubleSolution>();
    selection = new HVTournamentSelection(5);
    algorithm = new MicroFAME<>(problem, evaluations, archiveSize, crossover, mutation, selection);

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
