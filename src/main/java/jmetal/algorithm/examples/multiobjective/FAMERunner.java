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

import java.util.List;
import jmetal.core.algorithm.Algorithm;
import jmetal.algorithm.examples.AlgorithmRunner;
import jmetal.algorithm.multiobjective.fame.FAME;
import jmetal.core.operator.selection.SelectionOperator;
import jmetal.core.operator.selection.impl.SpatialSpreadDeviationSelection;
import jmetal.core.problem.Problem;
import jmetal.problem.multiobjective.glt.GLT1;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.AbstractAlgorithmRunner;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.evaluator.impl.SequentialSolutionListEvaluator;

/**
 * Class to configure and run the FAME algorithm, described in: A. Santiago, B. Dorronsoro, A.J.
 * Nebro, J.J. Durillo, O. Castillo, H.J. Fraire A Novel Multi-Objective Evolutionary Algorithm with
 * Fuzzy Logic Based Adaptive Selection of Operators: FAME. Information Sciences. Volume 471,
 * January 2019, Pages 233-251. DOI: https://doi.org/10.1016/j.ins.2018.09.005
 *
 * @author Alejandro Santiago <aurelio.santiago@upalt.edu.mx>
 */
public class FAMERunner extends AbstractAlgorithmRunner {

  /**
   * @param args Command line arguments.
   */
  public static void main(String[] args) {

    Problem<DoubleSolution> problem = new GLT1();

    SelectionOperator<List<DoubleSolution>, DoubleSolution> selection =
        new SpatialSpreadDeviationSelection<>(5);

    int populationSize = 25;
    int archiveSize = 100;
    int maxEvaluations = 45000;

    Algorithm<List<DoubleSolution>> algorithm =
        new FAME<>(
            problem,
            populationSize,
            archiveSize,
            maxEvaluations,
            selection,
            new SequentialSolutionListEvaluator<>());

    AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();

    List<DoubleSolution> population = algorithm.result();
    long computingTime = algorithmRunner.getComputingTime();

    JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");

    printFinalSolutionSet(population);
  }
}
