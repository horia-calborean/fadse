package jmetal.auto.autoconfigurablealgorithm.examples;

import jmetal.auto.autoconfigurablealgorithm.AutoMOPSO;
import jmetal.component.algorithm.ParticleSwarmOptimizationAlgorithm;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.fileoutput.SolutionListOutput;
import jmetal.core.util.fileoutput.impl.DefaultFileOutputContext;
import jmetal.core.util.observer.impl.RunTimeChartObserver;

/**
 * Program to configure {@link AutoMOPSO} with the parameter values of SMPSO
 *
 * @author Daniel Doblas
 * @author Antonio J. Nebro (ajnebro@uma.es)
 */
public class SMPSOWithUnboundedArchiveConfiguredFromAParameterString {

  public static void main(String[] args) {
    String referenceFrontFileName = "DTLZ2.3D.csv";

    String[] parameters =
        ("--problemName jmetal.problem.multiobjective.dtlz.DTLZ2 "
            + "--algorithmResult unboundedArchive "
            + "--randomGeneratorSeed 13 "
            + "--referenceFrontFileName "
            + referenceFrontFileName
            + " "
            + "--maximumNumberOfEvaluations 40000 "
            + "--swarmSize 100 "
            + "--archiveSize 100 "
            + "--swarmInitialization random "
            + "--velocityInitialization defaultVelocityInitialization "
            + "--leaderArchive crowdingDistanceArchive "
            + "--localBestInitialization defaultLocalBestInitialization "
            + "--globalBestInitialization defaultGlobalBestInitialization "
            + "--globalBestSelection tournament "
            + "--selectionTournamentSize 2 "
            + "--perturbation frequencySelectionMutationBasedPerturbation "
            + "--frequencyOfApplicationOfMutationOperator 7 "
            + "--mutation polynomial "
            + "--mutationProbabilityFactor 1.0 "
            + "--mutationRepairStrategy bounds "
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

    AutoMOPSO.print(autoMOPSO.fixedParameterList());
    AutoMOPSO.print(autoMOPSO.configurableParameterList());

    ParticleSwarmOptimizationAlgorithm smpso = autoMOPSO.create();

    RunTimeChartObserver<DoubleSolution> runTimeChartObserver =
        new RunTimeChartObserver<>(
            "SMPSO", 80, 500, "resources/referenceFrontsCSV/" + referenceFrontFileName);

    smpso.observable().register(runTimeChartObserver);

    smpso.run();

    JMetalLogger.logger.info("Total computing time: " + smpso.totalComputingTime()); ;

    new SolutionListOutput(smpso.result())
        .setVarFileOutputContext(new DefaultFileOutputContext("VAR.csv", ","))
        .setFunFileOutputContext(new DefaultFileOutputContext("FUN.csv", ","))
        .print();

    System.exit(0);
  }
}
