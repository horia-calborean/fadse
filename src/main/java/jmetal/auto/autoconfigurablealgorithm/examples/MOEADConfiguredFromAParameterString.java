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
public class MOEADConfiguredFromAParameterString {
  public static void main(String[] args) {
    String referenceFrontFileName = "DTLZ1.3D.csv" ;

    String[] parameters =
        ("--problemName jmetal.problem.multiobjective.dtlz.DTLZ1 "
            + "--referenceFrontFileName DTLZ1.3D.csv "
            + "--randomGeneratorSeed 124 "
            + "--maximumNumberOfEvaluations 35000 "
            + "--algorithmResult population "
            + "--populationSize 91 "
            + "--offspringPopulationSize 1 "
            + "--createInitialSolutions random "
            + "--normalizeObjectives false "
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

    AutoMOEAD autoNSGAII = new AutoMOEAD();
    autoNSGAII.parse(parameters);

    AutoNSGAII.print(autoNSGAII.fixedParameterList);
    AutoNSGAII.print(autoNSGAII.autoConfigurableParameterList);

    EvolutionaryAlgorithm<DoubleSolution> nsgaII = autoNSGAII.create();

    EvaluationObserver evaluationObserver = new EvaluationObserver(1000);
    RunTimeChartObserver<DoubleSolution> runTimeChartObserver =
        new RunTimeChartObserver<>(
            "MOEAD", 80, 1000,"resources/referenceFrontsCSV/" + referenceFrontFileName);

    nsgaII.observable().register(evaluationObserver);
    nsgaII.observable().register(runTimeChartObserver);

    nsgaII.run();

    JMetalLogger.logger.info("Total computing time: " + nsgaII.totalComputingTime()); ;

    new SolutionListOutput(nsgaII.result())
        .setVarFileOutputContext(new DefaultFileOutputContext("VAR.csv", ","))
        .setFunFileOutputContext(new DefaultFileOutputContext("FUN.csv", ","))
        .print();
  }
}
