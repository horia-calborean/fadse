package jmetal.algorithm.tests.abyss;

import static org.junit.Assert.assertTrue;

import java.util.Comparator;
import java.util.List;

import jmetal.algorithm.multiobjective.abyss.ABYSS;
import org.junit.Before;
import org.junit.Test;
import jmetal.core.algorithm.Algorithm;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.crossover.impl.SBXCrossover;
import jmetal.core.operator.localsearch.LocalSearchOperator;
import jmetal.core.operator.localsearch.impl.BasicLocalSearch;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.operator.mutation.impl.PolynomialMutation;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.problem.multiobjective.Tanaka;
import jmetal.core.qualityindicator.QualityIndicator;
import jmetal.core.qualityindicator.impl.hypervolume.impl.PISAHypervolume;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.SolutionListUtils;
import jmetal.core.util.VectorUtils;
import jmetal.core.util.archive.Archive;
import jmetal.core.util.archive.impl.CrowdingDistanceArchive;
import jmetal.core.util.comparator.constraintcomparator.impl.OverallConstraintViolationDegreeComparator;
import jmetal.core.util.comparator.dominanceComparator.impl.DominanceWithConstraintsComparator;


/**
 * Created by ajnebro on 11/6/15.
 */
public class ABYSSConstrainedProblemIT {

  Algorithm<List<DoubleSolution>> algorithm;
  DoubleProblem problem;
  CrossoverOperator<DoubleSolution> crossover;
  MutationOperator<DoubleSolution> mutation;
  LocalSearchOperator<DoubleSolution> localSearchOperator;
  Archive<DoubleSolution> archive;

  @Before
  public void setup() {
    problem = new Tanaka();

    double crossoverProbability = 1.0;
    double crossoverDistributionIndex = 20.0;
    crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);

    double mutationProbability = 1.0 / problem.numberOfVariables();
    double mutationDistributionIndex = 20.0;
    mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

    archive = new CrowdingDistanceArchive<>(100);

    Comparator<DoubleSolution> comparator = new DominanceWithConstraintsComparator<>(
        new OverallConstraintViolationDegreeComparator<>());

    localSearchOperator = new BasicLocalSearch<>(1, mutation, comparator, problem);
  }

  @Test
  public void shouldTheAlgorithmReturnANumberOfSolutionsWhenSolvingAConstrainedProblem() {
    int populationSize = 10;
    int numberOfSubRanges = 4;
    int referenceSet1Size = 4;
    int referenceSet2Size = 4;

    algorithm =
        new ABYSS(
            problem,
            25000,
            populationSize,
            referenceSet1Size,
            referenceSet2Size,
            100,
            archive,
            localSearchOperator,
            crossover,
            numberOfSubRanges);

    algorithm.run();

    List<DoubleSolution> population = algorithm.result();

    /*
    Rationale: the default problem is Tanaka, and AbYSS, configured with standard settings, should
    return more than 90 solutions
    */
    assertTrue(population.size() >= 90);
  }

  @Test
  public void shouldTheHypervolumeHaveAMinimumValue() throws Exception {
    int populationSize = 10;
    int numberOfSubRanges = 4;
    int referenceSet1Size = 4;
    int referenceSet2Size = 4;

    algorithm =
        new ABYSS(
            problem,
            25000,
            populationSize,
            referenceSet1Size,
            referenceSet2Size,
            100,
            archive,
            localSearchOperator,
            crossover,
            numberOfSubRanges);

    algorithm.run();

    List<DoubleSolution> population = algorithm.result();

    QualityIndicator hypervolume =
        new PISAHypervolume(
            VectorUtils.readVectors("../resources/referenceFrontsCSV/Tanaka.csv", ","));

    // Rationale: the default problem is Tanaka, and AbYSS, configured with standard settings,
    // should return find a front with a hypervolume value higher than 0.22

    double hv = hypervolume.compute(SolutionListUtils.getMatrixWithObjectiveValues(population));

    assertTrue(hv > 0.22);
  }
}
