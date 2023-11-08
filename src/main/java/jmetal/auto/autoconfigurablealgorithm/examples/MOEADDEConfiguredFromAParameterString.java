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
public class MOEADDEConfiguredFromAParameterString {
  public static void main(String[] args) {
    String referenceFrontFileName = "LZ09_F9.csv" ;

    String[] parameters =
        ("--problemName jmetal.problem.multiobjective.lz09.LZ09F9 "
            + "--referenceFrontFileName LZ09_F9.csv "
            + "--randomGeneratorSeed 124 "
            + "--maximumNumberOfEvaluations 175000 "
            + "--algorithmResult population "
            + "--populationSize 300 "
            + "--normalizeObjectives false "
            + "--offspringPopulationSize 1 "
            + "--createInitialSolutions random "
            + "--neighborhoodSize 20 "
            + "--maximumNumberOfReplacedSolutions 2 "
            + "--aggregationFunction tschebyscheff "
            + "--neighborhoodSelectionProbability 0.9 "
            + "--variation differentialEvolutionVariation "
            + "--differentialEvolutionCrossover RAND_1_BIN "
            + "--CR 1.0 "
            + "--F 0.5 "
            + "--selection populationAndNeighborhoodMatingPoolSelection "
            + "--crossover differentialEvolutionCrossover "
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
