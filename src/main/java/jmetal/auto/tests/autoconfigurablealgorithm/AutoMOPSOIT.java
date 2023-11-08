package jmetal.auto.tests.autoconfigurablealgorithm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import jmetal.auto.autoconfigurablealgorithm.AutoConfigurableAlgorithm;
import jmetal.auto.autoconfigurablealgorithm.AutoMOPSO;
import org.junit.jupiter.api.Test;
import jmetal.component.algorithm.ParticleSwarmOptimizationAlgorithm;
import jmetal.core.qualityindicator.QualityIndicator;
import jmetal.core.qualityindicator.impl.hypervolume.impl.PISAHypervolume;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.NormalizeUtils;
import jmetal.core.util.SolutionListUtils;
import jmetal.core.util.VectorUtils;

class AutoMOPSOIT {
  @Test
  void AutoMOPSOHas15FirstLevelConfigurableParameters() {
    assertThat(new AutoMOPSO().configurableParameterList()).hasSize(14);
  }

  @Test
  void AutoMOPSOHasFourFourFixedParameters() {
    assertThat(new AutoMOPSO().fixedParameterList()).hasSize(5);
  }

  @Test
  void AutoMOPSOHas36Parameters() {
    assertThat(AutoConfigurableAlgorithm.parameterFlattening(
        new AutoMOPSO().configurableParameterList())).hasSize(36);
  }

  @Test
  void AutoMOPSOWithDefaultSettingsReturnsAFrontWithHVHigherThanZeroPointSixtyFiveOnProblemZDT4() throws IOException {
    String referenceFrontFileName = "ZDT4.csv";

    String[] parameters =
        ("--problemName jmetal.problem.multiobjective.zdt.ZDT4 "
            + "--algorithmResult leaderArchive "
            + "--randomGeneratorSeed 13 "
            + "--referenceFrontFileName "
            + referenceFrontFileName
            + " "
            + "--maximumNumberOfEvaluations 25000 "
            + "--swarmSize 100 "
            + "--archiveSize 100 "
            + "--swarmInitialization random "
            + "--velocityInitialization defaultVelocityInitialization "
            + "--leaderArchive crowdingDistanceArchive "
            + "--localBestInitialization defaultLocalBestInitialization "
            + "--globalBestInitialization defaultGlobalBestInitialization "
            + "--globalBestSelection tournament "
            + "--perturbation frequencySelectionMutationBasedPerturbation "
            + "--frequencyOfApplicationOfMutationOperator 7 "
            + "--mutation polynomial "
            + "--mutationProbabilityFactor 1.0 "
            + "--mutationRepairStrategy bounds "
            + "--selectionTournamentSize 2 "
            + "--polynomialMutationDistributionIndex 20.0 "
            + "--positionUpdate defaultPositionUpdate "
            + "--velocityChangeWhenLowerLimitIsReached -1.0 "
            + "--velocityChangeWhenUpperLimitIsReached -1.0 "
            + "--globalBestUpdate defaultGlobalBestUpdate "
            + "--localBestUpdate defaultLocalBestUpdate "
            + "--velocityUpdate constrainedVelocityUpdate "
            + "--inertiaWeightComputingStrategy randomSelectedValue "
            + "--c1Min 1.5 "
            + "--c1Max 2.5 "
            + "--c2Min 1.5 "
            + "--c2Max 2.5 "
            + "--weightMin 0.1 "
            + "--weightMax 0.5 ")
            .split("\\s+");

    AutoMOPSO autoMOPSO = new AutoMOPSO();
    autoMOPSO.parse(parameters);

    ParticleSwarmOptimizationAlgorithm smpso = autoMOPSO.create() ;

    smpso.run();

    List<DoubleSolution> population  = smpso.result() ;

    String referenceFrontFile = "../resources/referenceFrontsCSV/"+referenceFrontFileName ;

    double[][] referenceFront = VectorUtils.readVectors(referenceFrontFile, ",") ;
    QualityIndicator hypervolume = new PISAHypervolume(referenceFront);

    double[][] normalizedFront =
        NormalizeUtils.normalize(
            SolutionListUtils.getMatrixWithObjectiveValues(population),
            NormalizeUtils.getMinValuesOfTheColumnsOfAMatrix(referenceFront),
            NormalizeUtils.getMaxValuesOfTheColumnsOfAMatrix(referenceFront));

    double hv = hypervolume.compute(normalizedFront);

    assertTrue(population.size() >= 95) ;
    assertTrue(hv > 0.65) ;
  }
}