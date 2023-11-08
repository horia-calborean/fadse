package jmetal.component.examples.multiobjective.nsgaii;

import java.io.IOException;
import java.util.Arrays;
import jmetal.component.algorithm.EvolutionaryAlgorithm;
import jmetal.component.algorithm.multiobjective.NSGAIIBuilder;
import jmetal.component.catalogue.common.termination.Termination;
import jmetal.component.catalogue.common.termination.impl.TerminationByEvaluations;
import jmetal.core.operator.crossover.impl.CompositeCrossover;
import jmetal.core.operator.crossover.impl.IntegerSBXCrossover;
import jmetal.core.operator.crossover.impl.SBXCrossover;
import jmetal.core.operator.mutation.impl.CompositeMutation;
import jmetal.core.operator.mutation.impl.IntegerPolynomialMutation;
import jmetal.core.operator.mutation.impl.PolynomialMutation;
import jmetal.core.problem.Problem;
import jmetal.problem.multiobjective.MixedIntegerDoubleProblem;
import jmetal.core.solution.compositesolution.CompositeSolution;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.errorchecking.JMetalException;
import jmetal.core.util.fileoutput.SolutionListOutput;
import jmetal.core.util.fileoutput.impl.DefaultFileOutputContext;
import jmetal.core.util.observer.impl.EvaluationObserver;
import jmetal.core.util.observer.impl.RunTimeChartObserver;
import jmetal.core.util.pseudorandom.JMetalRandom;

/**
 * Class to configure and run the NSGA-II algorithm to solve a problem having a mixed-encoding.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class NSGAIIWithMixedSolutionEncodingExample {

  public static void main(String[] args) throws JMetalException, IOException {
    Problem<CompositeSolution> problem =
        new MixedIntegerDoubleProblem(10, 10, 100, -100, -1000, +1000);

    CompositeCrossover crossover =
        new CompositeCrossover(
            Arrays.asList(new IntegerSBXCrossover(1.0, 20.0), new SBXCrossover(1.0, 20.0)));

    CompositeMutation mutation =
        new CompositeMutation(
            Arrays.asList(
                new IntegerPolynomialMutation(0.1, 2.0), new PolynomialMutation(0.1, 20.0)));

    int populationSize = 100;
    int offspringPopulationSize = 100;

    Termination termination = new TerminationByEvaluations(25000);

    EvolutionaryAlgorithm<CompositeSolution> nsgaii = new NSGAIIBuilder<>(
        problem,
        populationSize,
        offspringPopulationSize,
        crossover,
        mutation)
        .setTermination(termination)
        .build();

    EvaluationObserver evaluationObserver = new EvaluationObserver(1000);
    RunTimeChartObserver<DoubleSolution> runTimeChartObserver =
        new RunTimeChartObserver<>("NSGA-II", 80, 100, null);

    nsgaii.observable().register(evaluationObserver);
    nsgaii.observable().register(runTimeChartObserver);

    nsgaii.run();

    new SolutionListOutput(nsgaii.result())
        .setVarFileOutputContext(new DefaultFileOutputContext("VAR.csv", ","))
        .setFunFileOutputContext(new DefaultFileOutputContext("FUN.csv", ","))
        .print();

    JMetalLogger.logger.info("Random seed: " + JMetalRandom.getInstance().getSeed());
    JMetalLogger.logger.info("Objectives values have been written to file FUN.csv");
    JMetalLogger.logger.info("Variables values have been written to file VAR.csv");

    System.exit(0);
  }
}
