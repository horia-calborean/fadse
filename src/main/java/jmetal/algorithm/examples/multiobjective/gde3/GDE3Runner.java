package jmetal.algorithm.examples.multiobjective.gde3;

import java.io.IOException;
import java.util.List;
import jmetal.algorithm.examples.AlgorithmRunner;
import jmetal.algorithm.multiobjective.gde3.GDE3Builder;
import jmetal.core.operator.crossover.impl.DifferentialEvolutionCrossover;
import jmetal.core.operator.selection.impl.DifferentialEvolutionSelection;
import jmetal.problem.ProblemFactory;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.core.qualityindicator.QualityIndicatorUtils;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.AbstractAlgorithmRunner;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.SolutionListUtils;
import jmetal.core.util.VectorUtils;
import jmetal.core.util.evaluator.impl.SequentialSolutionListEvaluator;

/**
 * Class for configuring and running the GDE3 algorithm
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class GDE3Runner extends AbstractAlgorithmRunner {

  /**
   * @param args Command line arguments.
   */
  public static void main(String[] args) throws IOException {

    String problemName = "jmetal.core.problem.multiobjective.zdt.ZDT1";
    String referenceParetoFront = "resources/referenceFrontsCSV/ZDT1.csv";

    var problem = (DoubleProblem) ProblemFactory.<DoubleSolution>loadProblem(problemName);

    double cr = 0.5;
    double f = 0.5;
    var crossover = new DifferentialEvolutionCrossover(cr, f,
        DifferentialEvolutionCrossover.DE_VARIANT.RAND_1_BIN);

    var selection = new DifferentialEvolutionSelection();

    var algorithm = new GDE3Builder(problem)
        .setCrossover(crossover)
        .setSelection(selection)
        .setMaxEvaluations(25000)
        .setPopulationSize(100)
        .setSolutionSetEvaluator(new SequentialSolutionListEvaluator<>())
        .build();

    AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm)
        .execute();

    List<DoubleSolution> population = algorithm.result();
    long computingTime = algorithmRunner.getComputingTime();

    JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");

    printFinalSolutionSet(population);
    QualityIndicatorUtils.printQualityIndicators(
        SolutionListUtils.getMatrixWithObjectiveValues(population),
        VectorUtils.readVectors(referenceParetoFront, ","));  }
}
