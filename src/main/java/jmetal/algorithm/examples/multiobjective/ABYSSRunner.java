package jmetal.algorithm.examples.multiobjective;

import java.io.IOException;
import java.util.List;
import jmetal.algorithm.examples.AlgorithmRunner;
import jmetal.algorithm.multiobjective.abyss.ABYSSBuilder;
import jmetal.problem.ProblemFactory;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.core.qualityindicator.QualityIndicatorUtils;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.AbstractAlgorithmRunner;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.SolutionListUtils;
import jmetal.core.util.VectorUtils;
import jmetal.core.util.archive.Archive;
import jmetal.core.util.archive.impl.CrowdingDistanceArchive;

/**
 * This class is the main program used to configure and run AbYSS, a multiobjective scatter search
 * metaheuristics, which is described in: A.J. Nebro, F. Luna, E. Alba, B. Dorronsoro, J.J. Durillo,
 * A. Beham "AbYSS: Adapting Scatter Search to Multiobjective Optimization." IEEE Transactions on
 * Evolutionary Computation. Vol. 12, No. 4 (August 2008), pp. 439-457
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class ABYSSRunner extends AbstractAlgorithmRunner {

  /**
   * @param args Command line arguments.
   */
  public static void main(String[] args) throws IOException {
    String problemName = "jmetal.core.problem.multiobjective.zdt.ZDT4";
    String referenceParetoFront = "resources/referenceFrontsCSV/ZDT4.csv";

    var problem = (DoubleProblem) ProblemFactory.<DoubleSolution>loadProblem(problemName);

    Archive<DoubleSolution> archive = new CrowdingDistanceArchive<DoubleSolution>(100);

    var algorithm = new ABYSSBuilder(problem, archive)
        .setMaxEvaluations(25000)
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
