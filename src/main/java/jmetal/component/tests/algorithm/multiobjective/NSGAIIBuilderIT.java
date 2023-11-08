package jmetal.component.tests.algorithm.multiobjective;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.io.IOException;
import java.util.List;

import jmetal.component.algorithm.multiobjective.NSGAIIBuilder;
import org.junit.jupiter.api.Test;
import jmetal.component.algorithm.EvolutionaryAlgorithm;
import jmetal.component.catalogue.common.evaluation.Evaluation;
import jmetal.component.catalogue.common.evaluation.impl.SequentialEvaluationWithArchive;
import jmetal.component.catalogue.common.termination.Termination;
import jmetal.component.catalogue.common.termination.impl.TerminationByEvaluations;
import jmetal.core.operator.crossover.impl.SBXCrossover;
import jmetal.core.operator.mutation.impl.PolynomialMutation;
import jmetal.core.problem.Problem;
import jmetal.problem.ProblemFactory;
import jmetal.core.qualityindicator.QualityIndicator;
import jmetal.core.qualityindicator.impl.hypervolume.impl.PISAHypervolume;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.NormalizeUtils;
import jmetal.core.util.SolutionListUtils;
import jmetal.core.util.VectorUtils;
import jmetal.core.util.archive.Archive;
import jmetal.core.util.archive.impl.BestSolutionsArchive;
import jmetal.core.util.archive.impl.CrowdingDistanceArchive;
import jmetal.core.util.archive.impl.NonDominatedSolutionListArchive;

class NSGAIIBuilderIT {
  @Test
  void NSGAIIWithDefaultSettingsReturnsAFrontWithHVHigherThanZeroPointSixtyFiveOnProblemZDT1()
      throws IOException {
    String problemName = "jmetal.problem.multiobjective.zdt.ZDT1";

    Problem<DoubleSolution> problem = ProblemFactory.<DoubleSolution>loadProblem(problemName);

    double crossoverProbability = 0.9;
    double crossoverDistributionIndex = 20.0;
    var crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);

    double mutationProbability = 1.0 / problem.numberOfVariables();
    double mutationDistributionIndex = 20.0;
    var mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

    int populationSize = 100;
    int offspringPopulationSize = populationSize;

    Termination termination = new TerminationByEvaluations(25000);

    EvolutionaryAlgorithm<DoubleSolution> nsgaii = new NSGAIIBuilder<>(
        problem,
        populationSize,
        offspringPopulationSize,
        crossover,
        mutation)
        .setTermination(termination)
        .build();

    nsgaii.run();

    List<DoubleSolution> population = nsgaii.result();

    double[][] referenceFront = new double[][]{{0.0, 1.0}, {1.0, 0.0}} ;
    QualityIndicator hypervolume = new PISAHypervolume(referenceFront);

    double[][] normalizedFront =
        NormalizeUtils.normalize(
            SolutionListUtils.getMatrixWithObjectiveValues(population),
            NormalizeUtils.getMinValuesOfTheColumnsOfAMatrix(referenceFront),
            NormalizeUtils.getMaxValuesOfTheColumnsOfAMatrix(referenceFront));

    double hv = hypervolume.compute(normalizedFront);

    assertThat(populationSize).isGreaterThan(95) ;
    assertThat(hv).isGreaterThan(0.65) ;
  }

  @Test
  void NSGAIIWithExternalCrowdingArchiveReturnsAFrontWithHVHigherThanZeroPointSixtyFiveOnProblemZDT4()
      throws IOException {
    String problemName = "jmetal.problem.multiobjective.zdt.ZDT4";

    Problem<DoubleSolution> problem = ProblemFactory.<DoubleSolution>loadProblem(problemName);

    double crossoverProbability = 0.9;
    double crossoverDistributionIndex = 20.0;
    var crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);

    double mutationProbability = 1.0 / problem.numberOfVariables();
    double mutationDistributionIndex = 20.0;
    var mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

    int populationSize = 100;
    int offspringPopulationSize = populationSize;

    Termination termination = new TerminationByEvaluations(25000);

    Archive<DoubleSolution> archive = new CrowdingDistanceArchive<>(populationSize) ;
    Evaluation<DoubleSolution> evaluation = new SequentialEvaluationWithArchive<>(problem, archive) ;

    EvolutionaryAlgorithm<DoubleSolution> nsgaii = new NSGAIIBuilder<>(
        problem,
        populationSize,
        offspringPopulationSize,
        crossover,
        mutation)
        .setTermination(termination)
        .setEvaluation(evaluation)
        .build();

    nsgaii.run();

    List<DoubleSolution> population = nsgaii.result();

    double[][] referenceFront = new double[][]{{0.0, 1.0}, {1.0, 0.0}} ;
    QualityIndicator hypervolume = new PISAHypervolume(referenceFront);

    double[][] normalizedFront =
        NormalizeUtils.normalize(
            SolutionListUtils.getMatrixWithObjectiveValues(population),
            NormalizeUtils.getMinValuesOfTheColumnsOfAMatrix(referenceFront),
            NormalizeUtils.getMaxValuesOfTheColumnsOfAMatrix(referenceFront));

    double hv = hypervolume.compute(normalizedFront);

    System.out.println(hv);

    assertThat(populationSize).isGreaterThan(95) ;
    assertThat(hv).isGreaterThan(0.65) ;
  }

  @Test
  void NSGAIIWithExternalUnboundedArchiveReturnsAFrontWithHVHigherThanZeroPointFourOnProblemDTLZ2()
      throws IOException {
    String problemName = "jmetal.problem.multiobjective.dtlz.DTLZ2";
    String referenceFrontFileName = "DTLZ2.3D.csv";

    Problem<DoubleSolution> problem = ProblemFactory.<DoubleSolution>loadProblem(problemName);

    double crossoverProbability = 0.9;
    double crossoverDistributionIndex = 20.0;
    var crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);

    double mutationProbability = 1.0 / problem.numberOfVariables();
    double mutationDistributionIndex = 20.0;
    var mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

    int populationSize = 100;
    int offspringPopulationSize = populationSize;

    Termination termination = new TerminationByEvaluations(50000);

    Archive<DoubleSolution> archive = new BestSolutionsArchive<>(new NonDominatedSolutionListArchive<>(), populationSize) ;
    Evaluation<DoubleSolution> evaluation = new SequentialEvaluationWithArchive<>(problem, archive) ;

    EvolutionaryAlgorithm<DoubleSolution> nsgaii = new NSGAIIBuilder<>(
        problem,
        populationSize,
        offspringPopulationSize,
        crossover,
        mutation)
        .setTermination(termination)
        .setEvaluation(evaluation)
        .build();

    nsgaii.run();

    List<DoubleSolution> population = archive.solutions();

    String referenceFrontFile = "../resources/referenceFrontsCSV/"+referenceFrontFileName ;

    double[][] referenceFront = VectorUtils.readVectors(referenceFrontFile, ",") ;
    QualityIndicator hypervolume = new PISAHypervolume(referenceFront);

    double[][] normalizedFront =
        NormalizeUtils.normalize(
            SolutionListUtils.getMatrixWithObjectiveValues(population),
            NormalizeUtils.getMinValuesOfTheColumnsOfAMatrix(referenceFront),
            NormalizeUtils.getMaxValuesOfTheColumnsOfAMatrix(referenceFront));

    double hv = hypervolume.compute(normalizedFront);

    assertThat(populationSize).isGreaterThan(95) ;
    assertThat(hv).isGreaterThan(0.40) ;
  }
}