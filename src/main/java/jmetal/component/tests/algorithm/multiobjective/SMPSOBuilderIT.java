package jmetal.component.tests.algorithm.multiobjective;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;

import jmetal.component.algorithm.multiobjective.SMPSOBuilder;
import org.junit.jupiter.api.Test;
import jmetal.component.algorithm.ParticleSwarmOptimizationAlgorithm;
import jmetal.component.catalogue.common.evaluation.Evaluation;
import jmetal.component.catalogue.common.evaluation.impl.SequentialEvaluationWithArchive;
import jmetal.component.catalogue.common.termination.Termination;
import jmetal.component.catalogue.common.termination.impl.TerminationByEvaluations;
import jmetal.core.operator.mutation.impl.PolynomialMutation;
import jmetal.core.problem.Problem;
import jmetal.problem.ProblemFactory;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.core.qualityindicator.QualityIndicator;
import jmetal.core.qualityindicator.impl.hypervolume.impl.PISAHypervolume;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.NormalizeUtils;
import jmetal.core.util.SolutionListUtils;
import jmetal.core.util.VectorUtils;
import jmetal.core.util.archive.Archive;
import jmetal.core.util.archive.impl.BestSolutionsArchive;
import jmetal.core.util.archive.impl.NonDominatedSolutionListArchive;

class SMPSOBuilderIT {

  @Test
  void SMPSOWithDefaultSettingsReturnsAFrontWithHVHigherThanZeroPointSixtyFiveOnProblemZDT4() {
    String problemName = "jmetal.problem.multiobjective.zdt.ZDT4";

    Problem<DoubleSolution> problem = ProblemFactory.<DoubleSolution>loadProblem(problemName);

    int swarmSize = 100;
    Termination termination = new TerminationByEvaluations(25000);

    ParticleSwarmOptimizationAlgorithm smpso = new SMPSOBuilder(
        (DoubleProblem) problem,
        swarmSize)
        .setTermination(termination)
        .build();

    smpso.run();

    double[][] referenceFront = new double[][]{{0.0, 1.0}, {1.0, 0.0}};
    QualityIndicator hypervolume = new PISAHypervolume(referenceFront);

    double[][] normalizedFront =
        NormalizeUtils.normalize(
            SolutionListUtils.getMatrixWithObjectiveValues(smpso.result()),
            NormalizeUtils.getMinValuesOfTheColumnsOfAMatrix(referenceFront),
            NormalizeUtils.getMaxValuesOfTheColumnsOfAMatrix(referenceFront));

    double hv = hypervolume.compute(normalizedFront);

    assertThat(smpso.result()).hasSizeGreaterThan(95);
    assertThat(hv).isGreaterThan(0.65);
  }

  @Test
  void SMPSOWithExternalUnboundedArchiveReturnsAFrontWithHVHigherThanZeroPointThirtyFiveOnProblemDTLZ2()
      throws IOException {
    String problemName = "jmetal.problem.multiobjective.dtlz.DTLZ2";
    String referenceFrontFileName = "DTLZ2.3D.csv";

    Problem<DoubleSolution> problem = ProblemFactory.<DoubleSolution>loadProblem(problemName);

    double mutationProbability = 1.0 / problem.numberOfVariables();
    double mutationDistributionIndex = 20.0;
    var mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

    int swarmSize = 100;

    Termination termination = new TerminationByEvaluations(50000);

    Archive<DoubleSolution> archive = new BestSolutionsArchive<>(
        new NonDominatedSolutionListArchive<>(), swarmSize);
    Evaluation<DoubleSolution> evaluation = new SequentialEvaluationWithArchive<>(problem, archive);

    ParticleSwarmOptimizationAlgorithm smpso = new SMPSOBuilder(
        (DoubleProblem) problem,
        swarmSize)
        .setTermination(termination)
        .setEvaluation(evaluation)
        .build();

    smpso.run();

    List<DoubleSolution> obtainedSolutions = archive.solutions();

    String referenceFrontFile = "../resources/referenceFrontsCSV/" + referenceFrontFileName;

    double[][] referenceFront = VectorUtils.readVectors(referenceFrontFile, ",");
    QualityIndicator hypervolume = new PISAHypervolume(referenceFront);

    double[][] normalizedFront =
        NormalizeUtils.normalize(
            SolutionListUtils.getMatrixWithObjectiveValues(obtainedSolutions),
            NormalizeUtils.getMinValuesOfTheColumnsOfAMatrix(referenceFront),
            NormalizeUtils.getMaxValuesOfTheColumnsOfAMatrix(referenceFront));

    double hv = hypervolume.compute(normalizedFront);

    assertThat(obtainedSolutions).hasSizeGreaterThan(95);
    assertThat(hv).isGreaterThan(0.35);
  }
}