package jmetal.algorithm.examples.multiobjective.gde3;

import java.util.List;
import jmetal.algorithm.examples.AlgorithmRunner;
import jmetal.algorithm.multiobjective.gde3.GDE3;
import jmetal.algorithm.multiobjective.gde3.GDE3Builder;
import jmetal.core.operator.crossover.impl.DifferentialEvolutionCrossover;
import jmetal.core.operator.selection.impl.DifferentialEvolutionSelection;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.problem.multiobjective.cec2015OptBigDataCompetition.BigOpt2015;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.fileoutput.SolutionListOutput;
import jmetal.core.util.fileoutput.impl.DefaultFileOutputContext;

/**
 * Class for configuring and running the GDE3 algorithm for solving a problem of the Big
 * Optimization competition at CEC2015
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class GDE3BigDataRunner {

  /**
   * @param args Command line arguments.
   */
  public static void main(String[] args) {
    DoubleProblem problem = new BigOpt2015("D12");

    double cr = 1.5;
    double f = 0.5;
    var crossover = new DifferentialEvolutionCrossover(cr, f,
        DifferentialEvolutionCrossover.DE_VARIANT.RAND_1_BIN);

    var selection = new DifferentialEvolutionSelection();

    var algorithm = new GDE3Builder(problem)
        .setCrossover(crossover)
        .setSelection(selection)
        .setMaxEvaluations(250000)
        .setPopulationSize(100)
        .build();

    AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();

    List<DoubleSolution> population = ((GDE3) algorithm).result();
    long computingTime = algorithmRunner.getComputingTime();

    new SolutionListOutput(population)
        .setVarFileOutputContext(new DefaultFileOutputContext("VAR.tsv"))
        .setFunFileOutputContext(new DefaultFileOutputContext("FUN.tsv"))
        .print();

    JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");
    JMetalLogger.logger.info("Objectives values have been written to file FUN.tsv");
    JMetalLogger.logger.info("Variables values have been written to file VAR.tsv");
  }
}
