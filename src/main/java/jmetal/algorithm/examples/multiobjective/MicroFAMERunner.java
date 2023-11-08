//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jmetal.algorithm.examples.multiobjective;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import jmetal.core.algorithm.Algorithm;
import jmetal.algorithm.examples.AlgorithmRunner;
import jmetal.algorithm.multiobjective.microfame.MicroFAME;
import jmetal.algorithm.multiobjective.microfame.util.HVTournamentSelection;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.crossover.impl.NullCrossover;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.operator.mutation.impl.NullMutation;
import jmetal.core.operator.selection.SelectionOperator;
import jmetal.core.problem.Problem;
import jmetal.problem.ProblemFactory;
import jmetal.core.qualityindicator.QualityIndicatorUtils;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.AbstractAlgorithmRunner;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.SolutionListUtils;
import jmetal.core.util.VectorUtils;
import jmetal.core.util.errorchecking.JMetalException;

/**
 * Class to configure and run the Micro-FAME algorithm
 *
 * @author Alejandro Santiago <aurelio.santiago@upalt.edu.mx>
 */
public class MicroFAMERunner extends AbstractAlgorithmRunner {

  /**
   * @param args Command line arguments.
   * @throws JMetalException
   * @throws FileNotFoundException
   */
  public static void main(String[] args) throws JMetalException, IOException {

    int evaluations = 175000;
    int archiveSize = 100;

    String problemName = "jmetal.core.problem.multiobjective.lz09.LZ09F2";
    String referenceParetoFront = "resources/referenceFrontsCSV/LZ09_F2.csv";

    Problem<DoubleSolution> problem = ProblemFactory.<DoubleSolution>loadProblem(problemName);

    CrossoverOperator<DoubleSolution> crossover = new NullCrossover<>();
    MutationOperator<DoubleSolution> mutation = new NullMutation<>();
    SelectionOperator<List<DoubleSolution>, DoubleSolution> selection = new HVTournamentSelection(
        5);

    Algorithm<List<DoubleSolution>> algorithm = new MicroFAME<>(problem, evaluations, archiveSize,
        crossover, mutation, selection);

    AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();

    List<DoubleSolution> population = algorithm.result();
    long computingTime = algorithmRunner.getComputingTime();

    JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");

    printFinalSolutionSet(population);
    QualityIndicatorUtils.printQualityIndicators(
        SolutionListUtils.getMatrixWithObjectiveValues(population),
        VectorUtils.readVectors(referenceParetoFront, ","));
  }
}
