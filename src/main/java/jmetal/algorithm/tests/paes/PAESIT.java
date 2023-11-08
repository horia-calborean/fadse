package jmetal.algorithm.tests.paes;

import static org.junit.Assert.assertTrue;
import static jmetal.core.util.AbstractAlgorithmRunner.printFinalSolutionSet;

import java.util.List;

import jmetal.algorithm.multiobjective.paes.PAES;
import org.junit.Ignore;
import org.junit.Test;
import jmetal.core.algorithm.Algorithm;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.operator.mutation.impl.PolynomialMutation;
import jmetal.problem.multiobjective.ConstrEx;
import jmetal.problem.multiobjective.Kursawe;
import jmetal.problem.multiobjective.zdt.ZDT1;
import jmetal.core.qualityindicator.QualityIndicator;
import jmetal.core.qualityindicator.impl.hypervolume.impl.PISAHypervolume;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.NormalizeUtils;
import jmetal.core.util.SolutionListUtils;
import jmetal.core.util.VectorUtils;
import jmetal.core.util.archive.impl.GenericBoundedArchive;
import jmetal.core.util.densityestimator.impl.CrowdingDistanceDensityEstimator;

public class PAESIT {
  Algorithm<List<DoubleSolution>> algorithm;

  @Test
  public void shouldTheAlgorithmReturnANumberOfSolutionsWhenSolvingASimpleProblem()
      throws Exception {
    Kursawe problem = new Kursawe();
    CrossoverOperator<DoubleSolution> crossover;
    MutationOperator<DoubleSolution> mutation;

    double mutationProbability = 1.0 / problem.numberOfVariables();
    double mutationDistributionIndex = 20.0;
    mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

    algorithm = new PAES<>(problem, 25000, 100, 5, mutation);

    algorithm.run();

    List<DoubleSolution> population = algorithm.result();

    /*
    Rationale: the default problem is Kursawe, and usually PAES, configured with standard
    settings, should return 100 solutions
    */
    assertTrue(population.size() >= 99);
  }

  @Test
  @Ignore
  public void shouldTheHypervolumeHaveAMinimumValue() throws Exception {
    ZDT1 problem = new ZDT1();
    MutationOperator<DoubleSolution> mutation;

    double mutationProbability = 1.0 / problem.numberOfVariables();
    double mutationDistributionIndex = 20.0;
    mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

    algorithm = new PAES<>(problem, 25000, 100, 5, mutation);
    algorithm.run();

    List<DoubleSolution> population = algorithm.result();

    jmetal.core.qualityindicator.QualityIndicator hypervolume =
            new jmetal.core.qualityindicator.impl.hypervolume.impl.PISAHypervolume(
                    VectorUtils.readVectors("../resources/referenceFrontsCSV/ZDT1.csv", ","));

    // Rationale: the default problem is ZDT1, and OMOPSO, configured with standard settings, should
    // return find a front with a hypervolume value higher than 0.64

    double hv = hypervolume.compute(SolutionListUtils.getMatrixWithObjectiveValues(population));

    assertTrue(hv > 0.6);
  }

  @Test
  @Ignore
  public void shouldTheCrowdingDistanceVariantWorkProperly() throws Exception {
    ZDT1 problem = new ZDT1();
    MutationOperator<DoubleSolution> mutation;

    double mutationProbability = 1.0 / problem.numberOfVariables();
    double mutationDistributionIndex = 20.0;
    mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

    algorithm =
        new PAES<>(
            problem,
            25000,
            new GenericBoundedArchive<>(100, new CrowdingDistanceDensityEstimator<>()),
            mutation);
    algorithm.run();

    List<DoubleSolution> population = algorithm.result();

    QualityIndicator hypervolume =
            new PISAHypervolume(
                    VectorUtils.readVectors("../resources/referenceFrontsCSV/ZDT1.csv", ","));

    // Rationale: the default problem is ZDT1, and PAES, configured with standard settings, should
    // return find a front with a hypervolume value higher than 0.6

    double hv = hypervolume.compute(SolutionListUtils.getMatrixWithObjectiveValues(population));

    assertTrue(hv > 0.6);
  }

  @Test
  public void shouldTheAlgorithmReturnAGoodQualityFrontWhenSolvingAConstrainedProblem()
      throws Exception {
    ConstrEx problem = new ConstrEx();
    CrossoverOperator<DoubleSolution> crossover;
    MutationOperator<DoubleSolution> mutation;

    double mutationProbability = 1.0 / problem.numberOfVariables();
    double mutationDistributionIndex = 20.0;
    mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

    algorithm = new PAES<>(problem, 25000, 100, 5, mutation);

    algorithm.run();

    List<DoubleSolution> population = algorithm.result() ;

    String referenceFrontFileName = "../resources/referenceFrontsCSV/ConstrEx.csv" ;

    printFinalSolutionSet(population);

    double[][] referenceFront = VectorUtils.readVectors(referenceFrontFileName, ",") ;
    QualityIndicator hypervolume = new PISAHypervolume(referenceFront);

    // Rationale: the default problem is ConstrEx, and APES, configured with standard settings, should
    // return find a front with a hypervolume value higher than 0.7

    double[][] normalizedFront =
            NormalizeUtils.normalize(
                    SolutionListUtils.getMatrixWithObjectiveValues(population),
                    NormalizeUtils.getMinValuesOfTheColumnsOfAMatrix(referenceFront),
                    NormalizeUtils.getMaxValuesOfTheColumnsOfAMatrix(referenceFront));


    double hv = hypervolume.compute(normalizedFront);

    assertTrue(population.size() >= 85) ;
    assertTrue(hv > 0.7) ;
  }
}
