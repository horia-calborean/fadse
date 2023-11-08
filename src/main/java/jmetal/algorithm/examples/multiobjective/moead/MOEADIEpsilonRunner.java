package jmetal.algorithm.examples.multiobjective.moead;

import java.io.IOException;
import java.util.List;
import jmetal.algorithm.examples.AlgorithmRunner;
import jmetal.algorithm.multiobjective.moead.AbstractMOEAD;
import jmetal.algorithm.multiobjective.moead.MOEADBuilder;
import jmetal.algorithm.multiobjective.moead.MOEADBuilder.Variant;
import jmetal.core.operator.crossover.impl.DifferentialEvolutionCrossover;
import jmetal.core.operator.mutation.impl.PolynomialMutation;
import jmetal.problem.ProblemFactory;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.core.qualityindicator.QualityIndicatorUtils;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.AbstractAlgorithmRunner;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.SolutionListUtils;
import jmetal.core.util.VectorUtils;

/**
 * Class for configuring and running the MOEA/D-IEpsilon algorithm, described in: An Improved
 * epsilon-constrained Method in MOEA/D for CMOPs with Large Infeasible Regions * Fan, Z., Li, W.,
 * Cai, X. et al. Soft Comput (2019). https://doi.org/10.1007/s00500-019-03794-x
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class MOEADIEpsilonRunner extends AbstractAlgorithmRunner {

  /**
   * @param args Command line arguments.
   */
  public static void main(String[] args) throws IOException {
    String problemName = "jmetal.core.problem.multiobjective.lircmop.LIRCMOP2";
    String referenceParetoFront = "resources/referenceFrontsCSV/LIRCMOP2.csv";

    DoubleProblem problem = (DoubleProblem) ProblemFactory.<DoubleSolution>loadProblem(problemName);

    double cr = 1.0;
    double f = 0.5;
    var crossover = new DifferentialEvolutionCrossover(cr, f,
        DifferentialEvolutionCrossover.DE_VARIANT.RAND_1_BIN);

    double mutationProbability = 1.0 / problem.numberOfVariables();
    double mutationDistributionIndex = 20.0;
    var mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

    var algorithm = new MOEADBuilder(problem, Variant.MOEADIEPSILON)
        .setCrossover(crossover)
        .setMutation(mutation)
        .setMaxEvaluations(300000)
        .setPopulationSize(300)
        .setNeighborhoodSelectionProbability(0.9)
        .setMaximumNumberOfReplacedSolutions(2)
        .setNeighborSize(30)
        .setFunctionType(AbstractMOEAD.FunctionType.TCHE)
        .setDataDirectory("MOEAD_Weights")
        .build();

    AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm)
        .execute();

    List<DoubleSolution> population = algorithm.result();
    long computingTime = algorithmRunner.getComputingTime();

    JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");

    printFinalSolutionSet(population);
    QualityIndicatorUtils.printQualityIndicators(
        SolutionListUtils.getMatrixWithObjectiveValues(population),
        VectorUtils.readVectors(referenceParetoFront, ","));
  }
}
