package jmetal.auto.autoconfigurablealgorithm.examples;

import jmetal.auto.autoconfigurablealgorithm.AutoMOEAD;
import jmetal.auto.autoconfigurablealgorithm.AutoNSGAII;
import jmetal.component.algorithm.EvolutionaryAlgorithm;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.fileoutput.SolutionListOutput;
import jmetal.core.util.fileoutput.impl.DefaultFileOutputContext;
import jmetal.core.util.observer.impl.EvaluationObserver;
import jmetal.core.util.observer.impl.RunTimeChartObserver;

/**
 * Class configuring NSGA-II using arguments in the form <key, value> and the {@link AutoNSGAII} class.
 *
 * @author Antonio J. Nebro (ajnebro@uma.es)
 */
public class MOEADWithUnboundedArchiveConfiguredFromAParameterString {
  public static void main(String[] args) {
    String referenceFrontFileName = "DTLZ2.3D.csv" ;

    String[] parameters =
        ("--problemName jmetal.problem.multiobjective.dtlz.DTLZ2Minus "
            + "--referenceFrontFileName DTLZ2.3D.csv "
            + "--randomGeneratorSeed 124 "
            + "--maximumNumberOfEvaluations 40000 "
            + "--populationSize 91 "
            + "--normalizeObjectives false "
            + "--algorithmResult externalArchive "
            + "--externalArchive unboundedArchive "
            + "--offspringPopulationSize 1 "
            + "--createInitialSolutions random "
            + "--neighborhoodSize 20 "
            + "--maximumNumberOfReplacedSolutions 2 "
            + "--aggregationFunction penaltyBoundaryIntersection "
            + "--pbiTheta 5.0 "
            + "--neighborhoodSelectionProbability 0.9 "
            + "--variation crossoverAndMutationVariation "
            + "--selection populationAndNeighborhoodMatingPoolSelection "
            + "--crossover SBX "
            + "--crossoverProbability 0.9 "
            + "--crossoverRepairStrategy bounds "
            + "--sbxDistributionIndex 20.0 "
            + "--mutation polynomial "
            + "--mutationProbabilityFactor 1.0 "
            + "--mutationRepairStrategy bounds "
            + "--polynomialMutationDistributionIndex 20.0 ")
            .split("\\s+");

    AutoMOEAD autoMOEAD = new AutoMOEAD();
    autoMOEAD.parse(parameters);

    AutoNSGAII.print(autoMOEAD.fixedParameterList);
    AutoNSGAII.print(autoMOEAD.autoConfigurableParameterList);

    EvolutionaryAlgorithm<DoubleSolution> moead = autoMOEAD.create();

    EvaluationObserver evaluationObserver = new EvaluationObserver(1000);
    RunTimeChartObserver<DoubleSolution> runTimeChartObserver =
        new RunTimeChartObserver<>(
            "MOEAD", 80, 1000,"resources/referenceFrontsCSV/" + referenceFrontFileName);

    moead.observable().register(evaluationObserver);
    moead.observable().register(runTimeChartObserver);

    moead.run();

    JMetalLogger.logger.info("Total computing time: " + moead.totalComputingTime()); ;

    new SolutionListOutput(moead.result())
        .setVarFileOutputContext(new DefaultFileOutputContext("VAR.csv", ","))
        .setFunFileOutputContext(new DefaultFileOutputContext("FUN.csv", ","))
        .print();
  }
}
