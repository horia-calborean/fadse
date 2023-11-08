//

//

package jmetal.lab.experiment.studies;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jmetal.core.algorithm.Algorithm;
import jmetal.algorithm.multiobjective.mocell.MOCellBuilder;
import jmetal.algorithm.multiobjective.mochc.MOCHCBuilder;
import jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import jmetal.algorithm.multiobjective.spea2.SPEA2Builder;
import jmetal.lab.experiment.Experiment;
import jmetal.lab.experiment.ExperimentBuilder;
import jmetal.lab.experiment.component.impl.ComputeQualityIndicators;
import jmetal.lab.experiment.component.impl.ExecuteAlgorithms;
import jmetal.lab.experiment.component.impl.GenerateBoxplotsWithR;
import jmetal.lab.experiment.component.impl.GenerateFriedmanTestTables;
import jmetal.lab.experiment.component.impl.GenerateLatexTablesWithStatistics;
import jmetal.lab.experiment.component.impl.GenerateReferenceParetoFront;
import jmetal.lab.experiment.component.impl.GenerateWilcoxonTestTablesWithR;
import jmetal.lab.experiment.util.ExperimentAlgorithm;
import jmetal.lab.experiment.util.ExperimentProblem;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.crossover.impl.HUXCrossover;
import jmetal.core.operator.crossover.impl.SinglePointCrossover;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.operator.mutation.impl.BitFlipMutation;
import jmetal.core.operator.selection.SelectionOperator;
import jmetal.core.operator.selection.impl.RandomSelection;
import jmetal.core.operator.selection.impl.RankingAndCrowdingSelection;
import jmetal.core.problem.Problem;
import jmetal.core.problem.binaryproblem.BinaryProblem;
import jmetal.problem.multiobjective.OneZeroMax;
import jmetal.problem.multiobjective.zdt.ZDT5;
import jmetal.core.qualityindicator.impl.Epsilon;
import jmetal.core.qualityindicator.impl.GenerationalDistance;
import jmetal.core.qualityindicator.impl.InvertedGenerationalDistance;
import jmetal.core.qualityindicator.impl.InvertedGenerationalDistancePlus;
import jmetal.core.qualityindicator.impl.NormalizedHypervolume;
import jmetal.core.qualityindicator.impl.Spread;
import jmetal.core.qualityindicator.impl.hypervolume.impl.PISAHypervolume;
import jmetal.core.solution.binarysolution.BinarySolution;
import jmetal.core.util.errorchecking.JMetalException;
import jmetal.core.util.evaluator.impl.SequentialSolutionListEvaluator;

/**
 * Example of experimental study based on solving two binary problems with four algorithms: NSGAII,
 * SPEA2, MOCell, and MOCHC
 *
 * This jmetal.experiment assumes that the reference Pareto front are not known, so the must be produced.
 *
 * Six quality indicators are used for performance assessment.
 *
 * The steps to carry out the jmetal.experiment are: 1. Configure the jmetal.experiment 2. Execute the algorithms
 * 3. Generate the reference Pareto fronts 4. Compute que quality indicators 5. Generate Latex
 * tables reporting means and medians 6. Generate Latex tables with the result of applying the
 * Wilcoxon Rank Sum Test 7. Generate Latex tables with the ranking obtained by applying the
 * Friedman test 8. Generate R scripts to obtain boxplots
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class BinaryProblemsStudy {

  private static final int INDEPENDENT_RUNS = 25;

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      throw new JMetalException("Needed arguments: experimentBaseDirectory");
    }
    String experimentBaseDirectory = args[0];

    List<ExperimentProblem<BinarySolution>> problemList = new ArrayList<>();
    problemList.add(new ExperimentProblem<>(new ZDT5()));
    problemList.add(new ExperimentProblem<>(new OneZeroMax(512)));

    List<ExperimentAlgorithm<BinarySolution, List<BinarySolution>>> algorithmList =
        configureAlgorithmList(problemList);

    Experiment<BinarySolution, List<BinarySolution>> experiment;
    experiment = new ExperimentBuilder<BinarySolution, List<BinarySolution>>("BinaryProblemsStudy")
        .setAlgorithmList(algorithmList)
        .setProblemList(problemList)
        .setExperimentBaseDirectory(experimentBaseDirectory)
        .setOutputParetoFrontFileName("FUN")
        .setOutputParetoSetFileName("VAR")
        .setReferenceFrontDirectory(experimentBaseDirectory + "/BinaryProblemsStudy/referenceFronts")
        .setIndicatorList(Arrays.asList(
            new Epsilon(),
            new Spread(),
            new GenerationalDistance(),
            new PISAHypervolume(),
                new NormalizedHypervolume(),
                new InvertedGenerationalDistance(),
            new InvertedGenerationalDistancePlus())
        )
        .setIndependentRuns(INDEPENDENT_RUNS)
        .setNumberOfCores(8)
        .build();

    new ExecuteAlgorithms<>(experiment).run();
    new GenerateReferenceParetoFront(experiment).run();
    new ComputeQualityIndicators<>(experiment).run();
    new GenerateLatexTablesWithStatistics(experiment).run();
    new GenerateWilcoxonTestTablesWithR<>(experiment).run();
    new GenerateFriedmanTestTables<>(experiment).run();
    new GenerateBoxplotsWithR<>(experiment).setRows(1).setColumns(2).setDisplayNotch().run();
  }

  /**
   * The algorithm list is composed of pairs {@link Algorithm} + {@link Problem} which form part of
   * a {@link ExperimentAlgorithm}, which is a decorator for class {@link Algorithm}.
   */

  static List<ExperimentAlgorithm<BinarySolution, List<BinarySolution>>> configureAlgorithmList(
      List<ExperimentProblem<BinarySolution>> problemList) {
    List<ExperimentAlgorithm<BinarySolution, List<BinarySolution>>> algorithms = new ArrayList<>();
    for (int run = 0; run < INDEPENDENT_RUNS; run++) {

      for (ExperimentProblem<BinarySolution> problem : problemList) {
        Algorithm<List<BinarySolution>> algorithm = new NSGAIIBuilder<>(
                problem.getProblem(),
                new SinglePointCrossover(1.0),
                new BitFlipMutation(
                        1.0 / ((BinaryProblem) problem.getProblem()).bitsFromVariable(0)),
                100)
                .setMaxEvaluations(25000)
                .build();
        algorithms.add(new ExperimentAlgorithm<>(algorithm, problem, run));
      }

      for (ExperimentProblem<BinarySolution> problem : problemList) {
        Algorithm<List<BinarySolution>> algorithm = new SPEA2Builder<>(
                problem.getProblem(),
                new SinglePointCrossover(1.0),
                new BitFlipMutation(
                        1.0 / ((BinaryProblem) problem.getProblem()).bitsFromVariable(0)))
                .setMaxIterations(250)
                .setPopulationSize(100)
                .build();
        algorithms.add(new ExperimentAlgorithm<>(algorithm, problem, run));
      }

      for (ExperimentProblem<BinarySolution> problem : problemList) {
        Algorithm<List<BinarySolution>> algorithm = new MOCellBuilder<>(
                problem.getProblem(),
                new SinglePointCrossover(1.0),
                new BitFlipMutation(
                        1.0 / ((BinaryProblem) problem.getProblem()).bitsFromVariable(0)))
                .setMaxEvaluations(25000)
                .setPopulationSize(100)
                .build();
        algorithms.add(new ExperimentAlgorithm<>(algorithm, problem, run));
      }

      for (ExperimentProblem<BinarySolution> problem : problemList) {
        CrossoverOperator<BinarySolution> crossoverOperator;
        MutationOperator<BinarySolution> mutationOperator;
        SelectionOperator<List<BinarySolution>, BinarySolution> parentsSelection;
        SelectionOperator<List<BinarySolution>, List<BinarySolution>> newGenerationSelection;

        crossoverOperator = new HUXCrossover(1.0);
        parentsSelection = new RandomSelection<>();
        newGenerationSelection = new RankingAndCrowdingSelection<>(100);
        mutationOperator = new BitFlipMutation(0.35);
        Algorithm<List<BinarySolution>> algorithm = new MOCHCBuilder(
                (BinaryProblem) problem.getProblem())
                .setInitialConvergenceCount(0.25)
                .setConvergenceValue(3)
                .setPreservedPopulation(0.05)
                .setPopulationSize(100)
                .setMaxEvaluations(25000)
                .setCrossover(crossoverOperator)
                .setNewGenerationSelection(newGenerationSelection)
                .setCataclysmicMutation(mutationOperator)
                .setParentSelection(parentsSelection)
                .setEvaluator(new SequentialSolutionListEvaluator<>())
                .build();
        algorithms.add(new ExperimentAlgorithm<>(algorithm, problem, run));
      }
    }
    return algorithms;
  }
}
