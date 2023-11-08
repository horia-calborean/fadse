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
public class MOEADDEConfiguredForDTLZ1 {
  public static void main(String[] args) {
    String referenceFrontFileName = "DTLZ1.3D.csv" ;

    String[] parameters =
        ("--problemName jmetal.problem.multiobjective.dtlz.DTLZ1 "
            + "--referenceFrontFileName DTLZ1.3D.csv "
            + "--randomGeneratorSeed 124 "
            + "--maximumNumberOfEvaluations 50000 "
            + "--algorithmResult population "
            + "--normalizeObjectives false "
            + "--populationSize 91 "
            + "--offspringPopulationSize 1 "
            + "--createInitialSolutions random "
            + "--neighborhoodSize 30 "
            + "--maximumNumberOfReplacedSolutions 5 "
            + "--aggregationFunction penaltyBoundaryIntersection "
            + "--pbiTheta 5.0 "
            + "--variation differentialEvolutionVariation "
            + "--differentialEvolutionCrossover RAND_2_BIN "
            + "--CR 0.1199 "
            + "--F 0.6261 "
            + "--neighborhoodSelectionProbability 0.0016 "
            + "--selection populationAndNeighborhoodMatingPoolSelection "
            + "--nonUniformMutationPerturbation 0.8439 "
            + "--mutation nonUniform "
            + "--mutationProbabilityFactor 0.7089 "
            + "--mutationRepairStrategy round ")
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
