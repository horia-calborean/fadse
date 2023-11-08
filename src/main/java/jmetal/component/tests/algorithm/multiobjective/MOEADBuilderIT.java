package jmetal.component.tests.algorithm.multiobjective;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.io.IOException;
import java.util.List;

import jmetal.component.algorithm.multiobjective.MOEADBuilder;
import jmetal.component.algorithm.multiobjective.MOEADDEBuilder;
import org.junit.jupiter.api.Test;
import jmetal.component.algorithm.EvolutionaryAlgorithm;
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
import jmetal.core.util.aggregationfunction.impl.PenaltyBoundaryIntersection;
import jmetal.core.util.aggregationfunction.impl.Tschebyscheff;
import jmetal.core.util.sequencegenerator.SequenceGenerator;
import jmetal.core.util.sequencegenerator.impl.IntegerPermutationGenerator;

class MOEADBuilderIT {
  @Test
  void MOEADWithDefaultSettingsReturnsAFrontWithHVHigherThanZeroPointSeventySevenOnProblemDTLZ1()
      throws IOException {
    String problemName = "jmetal.problem.multiobjective.dtlz.DTLZ1";
    String referenceFrontFileName = "DTLZ1.3D.csv";

    Problem<DoubleSolution> problem = ProblemFactory.<DoubleSolution>loadProblem(problemName);

    double crossoverProbability = 0.9;
    double crossoverDistributionIndex = 20.0;
    var crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);

    double mutationProbability = 1.0 / problem.numberOfVariables();
    double mutationDistributionIndex = 20.0;
    var mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

    int populationSize = 91;

    Termination termination = new TerminationByEvaluations(40000);

    String weightVectorDirectory = "../resources/weightVectorFiles/moead";

    SequenceGenerator<Integer> sequenceGenerator = new IntegerPermutationGenerator(populationSize) ;
    EvolutionaryAlgorithm<DoubleSolution> moead = new MOEADBuilder<>(
        problem,
        populationSize,
        crossover,
        mutation,
        weightVectorDirectory,
        sequenceGenerator,
        false)
        .setTermination(termination)
        .setMaximumNumberOfReplacedSolutionsy(2)
        .setNeighborhoodSelectionProbability(0.9)
        .setNeighborhoodSize(20)
        .setAggregationFunction(new PenaltyBoundaryIntersection(5.0, false))
        .build();

    moead.run();

    List<DoubleSolution> population = moead.result();

    String referenceFrontFile = "../resources/referenceFrontsCSV/"+referenceFrontFileName ;

    double[][] referenceFront = VectorUtils.readVectors(referenceFrontFile, ",") ;
    QualityIndicator hypervolume = new PISAHypervolume(referenceFront);

    double[][] normalizedFront =
        NormalizeUtils.normalize(
            SolutionListUtils.getMatrixWithObjectiveValues(population),
            NormalizeUtils.getMinValuesOfTheColumnsOfAMatrix(referenceFront),
            NormalizeUtils.getMaxValuesOfTheColumnsOfAMatrix(referenceFront));

    double hv = hypervolume.compute(normalizedFront);

    assertThat(populationSize).isGreaterThan(90) ;
    assertThat(hv).isGreaterThan(0.77) ;
  }

  @Test
  void MOEADDEWithDefaultSettingsReturnsAFrontWithHVHigherThanZeroSixtyFiveOnProblemLZ09F2()
      throws IOException {
    String problemName = "jmetal.problem.multiobjective.dtlz.DTLZ1";
    String referenceFrontFileName = "DTLZ1.3D.csv";

    Problem<DoubleSolution> problem = ProblemFactory.<DoubleSolution>loadProblem(problemName);
    double cr = 1.0 ;
    double f = 0.5 ;

    double mutationProbability = 1.0 / problem.numberOfVariables();
    double mutationDistributionIndex = 20.0;
    var mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

    int populationSize = 300;

    Termination termination = new TerminationByEvaluations(175000);

    String weightVectorDirectory = "../resources/weightVectorFiles/moead";
    SequenceGenerator<Integer> sequenceGenerator = new IntegerPermutationGenerator(populationSize) ;

    EvolutionaryAlgorithm<DoubleSolution> moead = new MOEADDEBuilder(
        problem,
        populationSize,
        cr,
        f,
        mutation,
        weightVectorDirectory,
        sequenceGenerator,
        false)
        .setTermination(termination)
        .setMaximumNumberOfReplacedSolutionsy(2)
        .setNeighborhoodSelectionProbability(0.9)
        .setNeighborhoodSize(20)
        .setAggregationFunction(new Tschebyscheff(false))
        .build() ;

    moead.run();

    List<DoubleSolution> population = moead.result();

    String referenceFrontFile = "../resources/referenceFrontsCSV/"+referenceFrontFileName ;

    double[][] referenceFront = VectorUtils.readVectors(referenceFrontFile, ",") ;
    QualityIndicator hypervolume = new PISAHypervolume(referenceFront);

    double[][] normalizedFront =
        NormalizeUtils.normalize(
            SolutionListUtils.getMatrixWithObjectiveValues(population),
            NormalizeUtils.getMinValuesOfTheColumnsOfAMatrix(referenceFront),
            NormalizeUtils.getMaxValuesOfTheColumnsOfAMatrix(referenceFront));

    double hv = hypervolume.compute(normalizedFront);

    assertThat(populationSize).isGreaterThan(90) ;
    assertThat(hv).isGreaterThan(0.65) ;
  }
}