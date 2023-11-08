package jmetal.component.examples.multiobjective.moead;

import java.io.IOException;
import java.util.List;
import jmetal.component.algorithm.EvolutionaryAlgorithm;
import jmetal.component.algorithm.multiobjective.MOEADDEBuilder;
import jmetal.component.catalogue.common.termination.Termination;
import jmetal.component.catalogue.common.termination.impl.TerminationByEvaluations;
import jmetal.core.operator.mutation.impl.PolynomialMutation;
import jmetal.core.problem.Problem;
import jmetal.problem.ProblemFactory;
import jmetal.core.qualityindicator.QualityIndicatorUtils;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.SolutionListUtils;
import jmetal.core.util.VectorUtils;
import jmetal.core.util.aggregationfunction.impl.PenaltyBoundaryIntersection;
import jmetal.core.util.errorchecking.JMetalException;
import jmetal.core.util.fileoutput.SolutionListOutput;
import jmetal.core.util.fileoutput.impl.DefaultFileOutputContext;
import jmetal.core.util.observer.impl.RunTimeChartObserver;
import jmetal.core.util.pseudorandom.JMetalRandom;
import jmetal.core.util.sequencegenerator.SequenceGenerator;
import jmetal.core.util.sequencegenerator.impl.IntegerPermutationGenerator;

/**
 * Class to configure and run the NSGA-II algorithm configured with standard settings.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class MOEADDEDefaultConfigurationExample {

  public static void main(String[] args) throws JMetalException, IOException {
    String problemName = "jmetal.problem.multiobjective.lz09.LZ09F2";
    String referenceParetoFront = "resources/referenceFrontsCSV/LZ09_F2.csv";

    Problem<DoubleSolution> problem = ProblemFactory.<DoubleSolution>loadProblem(problemName);

    double cr = 1.0 ;
    double f = 0.5 ;

    double mutationProbability = 1.0 / problem.numberOfVariables();
    double mutationDistributionIndex = 20.0;
    var mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

    int populationSize = 300;

    Termination termination = new TerminationByEvaluations(175000);

    String weightVectorDirectory = "resources/weightVectorFiles/moead";
    SequenceGenerator<Integer> sequenceGenerator = new IntegerPermutationGenerator(populationSize) ;

    boolean normalizeObjectives = false ;
    EvolutionaryAlgorithm<DoubleSolution> moead = new MOEADDEBuilder(
        problem,
        populationSize,
        cr,
        f,
        mutation,
        weightVectorDirectory,
        sequenceGenerator, normalizeObjectives)
        .setTermination(termination)
        .setMaximumNumberOfReplacedSolutionsy(2)
        .setNeighborhoodSelectionProbability(0.9)
        .setNeighborhoodSize(20)
        .setAggregationFunction(new PenaltyBoundaryIntersection(5.0, normalizeObjectives))
        .build() ;

    RunTimeChartObserver<DoubleSolution> runTimeChartObserver =
        new RunTimeChartObserver<>("MOEA/D-DE", 80, 1000, referenceParetoFront);
    moead.observable().register(runTimeChartObserver);

    moead.run();

    List<DoubleSolution> population = moead.result();
    JMetalLogger.logger.info("Total execution time : " + moead.totalComputingTime() + "ms");
    JMetalLogger.logger.info("Number of evaluations: " + moead.numberOfEvaluations());

    new SolutionListOutput(population)
        .setVarFileOutputContext(new DefaultFileOutputContext("VAR.csv", ","))
        .setFunFileOutputContext(new DefaultFileOutputContext("FUN.csv", ","))
        .print();

    JMetalLogger.logger.info("Random seed: " + JMetalRandom.getInstance().getSeed());
    JMetalLogger.logger.info("Objectives values have been written to file FUN.csv");
    JMetalLogger.logger.info("Variables values have been written to file VAR.csv");

    QualityIndicatorUtils.printQualityIndicators(
        SolutionListUtils.getMatrixWithObjectiveValues(population),
        VectorUtils.readVectors(referenceParetoFront, ","));
  }
}
