package jmetal.component.examples.multiobjective.moead;

import java.io.IOException;
import java.util.List;
import jmetal.component.algorithm.EvolutionaryAlgorithm;
import jmetal.component.algorithm.multiobjective.MOEADBuilder;
import jmetal.component.catalogue.common.evaluation.impl.SequentialEvaluationWithArchive;
import jmetal.component.catalogue.common.termination.Termination;
import jmetal.component.catalogue.common.termination.impl.TerminationByEvaluations;
import jmetal.core.operator.crossover.impl.SBXCrossover;
import jmetal.core.operator.mutation.impl.PolynomialMutation;
import jmetal.core.problem.Problem;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.JMetalLogger;
import jmetal.problem.ProblemFactory;
import jmetal.core.util.archive.Archive;
import jmetal.core.util.archive.impl.BestSolutionsArchive;
import jmetal.core.util.archive.impl.NonDominatedSolutionListArchive;
import jmetal.core.util.errorchecking.JMetalException;
import jmetal.core.util.fileoutput.SolutionListOutput;
import jmetal.core.util.fileoutput.impl.DefaultFileOutputContext;
import jmetal.core.util.pseudorandom.JMetalRandom;
import jmetal.core.util.sequencegenerator.SequenceGenerator;
import jmetal.core.util.sequencegenerator.impl.IntegerPermutationGenerator;

/**
 * Class to configure and run the NSGA-II algorithm configured with standard settings.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class MOEADWithUnboundedArchiveExample {
  public static void main(String[] args) throws JMetalException, IOException {
    String problemName = "jmetal.problem.multiobjective.dtlz.DTLZ2Minus";

    Problem<DoubleSolution> problem = ProblemFactory.<DoubleSolution>loadProblem(problemName);

    double crossoverProbability = 0.9;
    double crossoverDistributionIndex = 20.0;
    var crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);

    double mutationProbability = 1.0 / problem.numberOfVariables();
    double mutationDistributionIndex = 20.0;
    var mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

    int populationSize = 91;

    Termination termination = new TerminationByEvaluations(40000);

    String weightVectorDirectory = "resources/weightVectorFiles/moead";

    SequenceGenerator<Integer> sequenceGenerator = new IntegerPermutationGenerator(populationSize) ;

    Archive<DoubleSolution> externalArchive = new BestSolutionsArchive<>(new NonDominatedSolutionListArchive<>(), populationSize) ;
    boolean normalizeObjectives = false ;

    EvolutionaryAlgorithm<DoubleSolution> moead = new MOEADBuilder<>(
        problem,
        populationSize,
        crossover,
        mutation,
        weightVectorDirectory,
        sequenceGenerator,
        normalizeObjectives)
        .setTermination(termination)
        .setEvaluation(new SequentialEvaluationWithArchive<DoubleSolution>(problem, externalArchive))
        .build();

    moead.run();

    List<DoubleSolution> population = externalArchive.solutions();
    JMetalLogger.logger.info("Total execution time : " + moead.totalComputingTime() + "ms");
    JMetalLogger.logger.info("Number of evaluations: " + moead.numberOfEvaluations());

    new SolutionListOutput(population)
        .setVarFileOutputContext(new DefaultFileOutputContext("VAR.csv", ","))
        .setFunFileOutputContext(new DefaultFileOutputContext("FUN.csv", ","))
        .print();

    JMetalLogger.logger.info("Random seed: " + JMetalRandom.getInstance().getSeed());
    JMetalLogger.logger.info("Objectives values have been written to file FUN.csv");
    JMetalLogger.logger.info("Variables values have been written to file VAR.csv");
  }
}
