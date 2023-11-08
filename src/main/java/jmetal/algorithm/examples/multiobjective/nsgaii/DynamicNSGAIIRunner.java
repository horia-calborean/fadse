package jmetal.algorithm.examples.multiobjective.nsgaii;

import java.util.List;
import jmetal.algorithm.multiobjective.nsgaii.DynamicNSGAII;
import jmetal.algorithm.multiobjective.nsgaii.util.CoverageFront;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.crossover.impl.SBXCrossover;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.operator.mutation.impl.PolynomialMutation;
import jmetal.core.operator.selection.SelectionOperator;
import jmetal.core.operator.selection.impl.BinaryTournamentSelection;
import jmetal.core.problem.DynamicProblem;
import jmetal.problem.multiobjective.fda.FDA2;
import jmetal.core.qualityindicator.impl.InvertedGenerationalDistance;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.evaluator.impl.SequentialSolutionListEvaluator;
import jmetal.core.util.observable.impl.DefaultObservable;
import jmetal.core.util.observer.impl.RunTimeForDynamicProblemsChartObserver;
import jmetal.core.util.restartstrategy.impl.CreateNRandomSolutions;
import jmetal.core.util.restartstrategy.impl.DefaultRestartStrategy;
import jmetal.core.util.restartstrategy.impl.RemoveNRandomSolutions;

public class DynamicNSGAIIRunner {

  /**
   * main() method to run the algorithm as a process
   *
   * @param args
   */
  public static void main(String[] args) {
    DynamicProblem<DoubleSolution, Integer> problem = new FDA2();

    // STEP 2. Create the algorithm
    CrossoverOperator<DoubleSolution> crossover = new SBXCrossover(0.9, 20.0);
    MutationOperator<DoubleSolution> mutation =
        new PolynomialMutation(1.0 / problem.numberOfVariables(), 20.0);
    SelectionOperator<List<DoubleSolution>, DoubleSolution> selection =
        new BinaryTournamentSelection<>();

    InvertedGenerationalDistance igd = new InvertedGenerationalDistance();
    CoverageFront<DoubleSolution> coverageFront = new CoverageFront<>(0.055, igd);
    var algorithm =
        new DynamicNSGAII<>(
            problem,
            25000,
            100,
            100,
            100,
            crossover,
            mutation,
            selection,
            new SequentialSolutionListEvaluator<>(),
            new DefaultRestartStrategy<>(
                new RemoveNRandomSolutions<>(10), new CreateNRandomSolutions<>()),
            new DefaultObservable<>("Dynamic NSGA-II"),
            coverageFront);

    RunTimeForDynamicProblemsChartObserver<DoubleSolution> runTimeChartObserver =
        new RunTimeForDynamicProblemsChartObserver<>("Dynamic NSGA-II", 80);

    algorithm.getObservable().register(runTimeChartObserver);

    algorithm.run();
  }
}
