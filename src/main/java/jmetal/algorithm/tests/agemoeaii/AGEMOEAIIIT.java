package jmetal.algorithm.tests.agemoeaii;

import static org.junit.Assert.assertTrue;

import java.util.List;

import jmetal.algorithm.multiobjective.agemoeaii.AGEMOEAIIBuilder;
import org.junit.Test;
import jmetal.core.algorithm.Algorithm;
import jmetal.algorithm.multiobjective.agemoea.util.SurvivalScoreComparator;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.crossover.impl.SBXCrossover;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.operator.mutation.impl.PolynomialMutation;
import jmetal.core.operator.selection.SelectionOperator;
import jmetal.core.operator.selection.impl.BinaryTournamentSelection;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.problem.multiobjective.ConstrEx;
import jmetal.problem.multiobjective.Kursawe;
import jmetal.problem.multiobjective.zdt.ZDT1;
import jmetal.core.qualityindicator.QualityIndicator;
import jmetal.core.qualityindicator.impl.hypervolume.impl.PISAHypervolume;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.NormalizeUtils;
import jmetal.core.util.SolutionListUtils;
import jmetal.core.util.VectorUtils;

public class AGEMOEAIIIT {

  Algorithm<List<DoubleSolution>> algorithm;

  @Test
  public void shouldTheAlgorithmReturnANumberOfSolutionsWhenSolvingASimpleProblem()
      throws Exception {
    DoubleProblem problem = new Kursawe();
    CrossoverOperator<DoubleSolution> crossover;
    MutationOperator<DoubleSolution> mutation;

    double crossoverProbability = 0.9;
    double crossoverDistributionIndex = 20.0;
    crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);

    double mutationProbability = 1.0 / problem.numberOfVariables();
    double mutationDistributionIndex = 20.0;
    mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

    SelectionOperator<List<DoubleSolution>, DoubleSolution> selection = new BinaryTournamentSelection<>(
        new SurvivalScoreComparator<>());

    int populationSize = 100;
    algorithm =
        new AGEMOEAIIBuilder<>(problem)
            .setCrossoverOperator(crossover)
            .setMutationOperator(mutation)
            .setMaxIterations(250)
            .setPopulationSize(populationSize)
            .build();

    algorithm.run();

    List<DoubleSolution> population = algorithm.result();

    /*
    Rationale: the default problem is Kursawe, and usually AGE-MOEA-II, configured with standard
    settings, should return 100 solutions
    */
    assertTrue(population.size() >= 90);
  }

  @Test
  public void shouldTheHypervolumeHaveAMininumValue() throws Exception {
    DoubleProblem problem = new ZDT1();
    CrossoverOperator<DoubleSolution> crossover;
    MutationOperator<DoubleSolution> mutation;

    double crossoverProbability = 0.9;
    double crossoverDistributionIndex = 20.0;
    crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);

    double mutationProbability = 1.0 / problem.numberOfVariables();
    double mutationDistributionIndex = 20.0;
    mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

    int populationSize = 100;
    algorithm =
        new AGEMOEAIIBuilder<>(problem)
            .setCrossoverOperator(crossover)
            .setMutationOperator(mutation)
            .setMaxIterations(250)
            .setPopulationSize(populationSize)
            .build();

    algorithm.run();

    algorithm.run();

    List<DoubleSolution> population = algorithm.result();

    QualityIndicator hypervolume =
        new PISAHypervolume(
            VectorUtils.readVectors("../resources/referenceFrontsCSV/ZDT1.csv", ","));

    // Rationale: the default problem is ZDT1, and AGE-MOEA-II, configured with standard settings,
    // should return find a front with a hypervolume value higher than 0.62

    double hv = hypervolume.compute(SolutionListUtils.getMatrixWithObjectiveValues(population));

    assertTrue(hv > 0.62);
  }

  @Test
  public void shouldTheAlgorithmReturnAGoodQualityFrontWhenSolvingAConstrainedProblem()
      throws Exception {
    ConstrEx problem = new ConstrEx();
    CrossoverOperator<DoubleSolution> crossover;
    MutationOperator<DoubleSolution> mutation;

    double crossoverProbability = 0.9;
    double crossoverDistributionIndex = 20.0;
    crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);

    double mutationProbability = 1.0 / problem.numberOfVariables();
    double mutationDistributionIndex = 20.0;
    mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

    int populationSize = 100;
    algorithm =
        new AGEMOEAIIBuilder<>(problem)
            .setCrossoverOperator(crossover)
            .setMutationOperator(mutation)
            .setMaxIterations(250)
            .setPopulationSize(populationSize)
            .build();

    algorithm.run();

    List<DoubleSolution> population = algorithm.result();

    String referenceFrontFileName = "../resources/referenceFrontsCSV/ConstrEx.csv";

    double[][] referenceFront = VectorUtils.readVectors(referenceFrontFileName, ",");
    QualityIndicator hypervolume = new PISAHypervolume(referenceFront);

    // Rationale: the default problem is DTLZ1 (3 objectives), and AGE-MOEA-II, configured with standard settings, should
    // return find a front with a hypervolume value higher than 0.96

    double[][] normalizedFront =
        NormalizeUtils.normalize(
            SolutionListUtils.getMatrixWithObjectiveValues(population),
            NormalizeUtils.getMinValuesOfTheColumnsOfAMatrix(referenceFront),
            NormalizeUtils.getMaxValuesOfTheColumnsOfAMatrix(referenceFront));

    double hv = hypervolume.compute(normalizedFront);

    assertTrue(population.size() >= 85);
    assertTrue(hv > 0.77);
  }
}