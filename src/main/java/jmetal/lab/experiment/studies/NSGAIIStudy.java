package jmetal.lab.experiment.studies;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import jmetal.core.algorithm.Algorithm;
import jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import jmetal.lab.experiment.Experiment;
import jmetal.lab.experiment.ExperimentBuilder;
import jmetal.lab.experiment.component.impl.ComputeQualityIndicators;
import jmetal.lab.experiment.component.impl.ExecuteAlgorithms;
import jmetal.lab.experiment.component.impl.GenerateBoxplotsWithR;
import jmetal.lab.experiment.component.impl.GenerateFriedmanTestTables;
import jmetal.lab.experiment.component.impl.GenerateHtmlPages;
import jmetal.lab.experiment.component.impl.GenerateLatexTablesWithStatistics;
import jmetal.lab.experiment.component.impl.GenerateWilcoxonTestTablesWithR;
import jmetal.lab.experiment.util.ExperimentAlgorithm;
import jmetal.lab.experiment.util.ExperimentProblem;
import jmetal.core.operator.crossover.impl.SBXCrossover;
import jmetal.core.operator.mutation.impl.PolynomialMutation;
import jmetal.core.problem.Problem;
import jmetal.problem.multiobjective.zdt.ZDT1;
import jmetal.problem.multiobjective.zdt.ZDT2;
import jmetal.problem.multiobjective.zdt.ZDT3;
import jmetal.problem.multiobjective.zdt.ZDT4;
import jmetal.problem.multiobjective.zdt.ZDT6;
import jmetal.core.qualityindicator.impl.Epsilon;
import jmetal.core.qualityindicator.impl.GenerationalDistance;
import jmetal.core.qualityindicator.impl.InvertedGenerationalDistance;
import jmetal.core.qualityindicator.impl.InvertedGenerationalDistancePlus;
import jmetal.core.qualityindicator.impl.NormalizedHypervolume;
import jmetal.core.qualityindicator.impl.Spread;
import jmetal.core.qualityindicator.impl.hypervolume.impl.PISAHypervolume;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.errorchecking.JMetalException;

/**
 * Example of experimental study based on solving the ZDT problems with four versions of NSGA-II,
 * each of them applying a different crossover probability (from 0.7 to 1.0).
 * <p>
 * This jmetal.experiment assumes that the reference Pareto front are known and that, given
 * a problem named P, there is a corresponding file called P.pf containing its corresponding Pareto
 * front. If this is not the case, please refer to class {@link DTLZStudy} to see an example of how
 * to explicitly indicate the name of those files.
 * <p>
 * Six quality indicators are used for performance assessment.
 * <p>
 * The steps to carry out the jmetal.experiment are: 1. Configure experiment 2. Execute
 * algorithms 3. Compute quality indicators 4. Generate Latex tables reporting means and medians 5.
 * Generate Latex tables with the result of applying the Wilcoxon Rank Sum Test 6. Generate Latex
 * tables with the ranking obtained by applying the Friedman test 7. Generate R scripts to obtain
 * boxplots 8. Generate HTML pages with including the above data
 *
 * @author Antonio J. Nebro (ajnebro@uma.es)
 */
public class NSGAIIStudy {

  private static final int INDEPENDENT_RUNS = 25;

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      throw new JMetalException("Missing argument: experimentBaseDirectory");
    }
    String experimentBaseDirectory = args[0];

    List<ExperimentProblem<DoubleSolution>> problemList = new ArrayList<>();
    problemList.add(new ExperimentProblem<>(new ZDT1()));
    problemList.add(new ExperimentProblem<>(new ZDT2()));
    problemList.add(new ExperimentProblem<>(new ZDT3()));
    problemList.add(new ExperimentProblem<>(new ZDT4()));
    problemList.add(new ExperimentProblem<>(new ZDT6()));

    List<ExperimentAlgorithm<DoubleSolution, List<DoubleSolution>>> algorithmList =
        configureAlgorithmList(problemList);

    Experiment<DoubleSolution, List<DoubleSolution>> experiment =
        new ExperimentBuilder<DoubleSolution, List<DoubleSolution>>("NSGAIIStudy")
            .setAlgorithmList(algorithmList)
            .setProblemList(problemList)
            .setExperimentBaseDirectory(experimentBaseDirectory)
            .setOutputParetoFrontFileName("FUN")
            .setOutputParetoSetFileName("VAR")
            .setReferenceFrontDirectory("resources/referenceFrontsCSV")
            .setIndicatorList(
                List.of(
                    new Epsilon(),
                    new Spread(),
                    new GenerationalDistance(),
                    new PISAHypervolume(),
                    new NormalizedHypervolume(),
                    new InvertedGenerationalDistance(),
                    new InvertedGenerationalDistancePlus()))
            .setIndependentRuns(INDEPENDENT_RUNS)
            .setNumberOfCores(8)
            .build();

    new ExecuteAlgorithms<>(experiment).run();
    new ComputeQualityIndicators<>(experiment).run();
    new GenerateLatexTablesWithStatistics(experiment).run();
    new GenerateWilcoxonTestTablesWithR<>(experiment).run();
    new GenerateFriedmanTestTables<>(experiment).run();
    new GenerateBoxplotsWithR<>(experiment).setRows(2).setColumns(3).run();
    new GenerateHtmlPages<>(experiment).run();
  }

  /**
   * The algorithm list is composed of pairs {@link Algorithm} + {@link Problem} which form part of
   * a {@link ExperimentAlgorithm}, which is a decorator for class {@link Algorithm}. The
   * {@link ExperimentAlgorithm} has an optional tag component, that can be set as it is shown in
   * this example, where four variants of a same algorithm are defined.
   */
  static List<ExperimentAlgorithm<DoubleSolution, List<DoubleSolution>>> configureAlgorithmList(
      List<ExperimentProblem<DoubleSolution>> problemList) {
    List<ExperimentAlgorithm<DoubleSolution, List<DoubleSolution>>> algorithms = new ArrayList<>();

    for (int run = 0; run < INDEPENDENT_RUNS; run++) {
      for (var experimentProblem : problemList) {
        nsgaIIa(algorithms, run, experimentProblem);
        nsgaIIb(algorithms, run, experimentProblem);
        nsgaIIc(algorithms, run, experimentProblem);
        nsgaIId(algorithms, run, experimentProblem);
      }
    }
    return algorithms;
  }

  private static void nsgaIId(
      List<ExperimentAlgorithm<DoubleSolution, List<DoubleSolution>>> algorithms, int run,
      ExperimentProblem<DoubleSolution> experimentProblem) {
    Algorithm<List<DoubleSolution>> algorithm =
        new NSGAIIBuilder<>(
            experimentProblem.getProblem(),
            new SBXCrossover(1.0, 80.0),
            new PolynomialMutation(
                1.0 / experimentProblem.getProblem().numberOfVariables(),
                80.0),
            100)
            .setMaxEvaluations(25000)
            .build();
    algorithms.add(
        new ExperimentAlgorithm<>(algorithm, "NSGAIId", experimentProblem, run));
  }

  private static void nsgaIIc(
      List<ExperimentAlgorithm<DoubleSolution, List<DoubleSolution>>> algorithms, int run,
      ExperimentProblem<DoubleSolution> experimentProblem) {
    Algorithm<List<DoubleSolution>> algorithm =
        new NSGAIIBuilder<>(
            experimentProblem.getProblem(),
            new SBXCrossover(1.0, 40.0),
            new PolynomialMutation(
                1.0 / experimentProblem.getProblem().numberOfVariables(),
                40.0),
            10)
            .setMaxEvaluations(25000)
            .build();
    algorithms.add(
        new ExperimentAlgorithm<>(algorithm, "NSGAIIc", experimentProblem, run));
  }

  private static void nsgaIIb(
      List<ExperimentAlgorithm<DoubleSolution, List<DoubleSolution>>> algorithms, int run,
      ExperimentProblem<DoubleSolution> experimentProblem) {
    Algorithm<List<DoubleSolution>> algorithm =
        new NSGAIIBuilder<>(
            experimentProblem.getProblem(),
            new SBXCrossover(1.0, 20.0),
            new PolynomialMutation(
                1.0 / experimentProblem.getProblem().numberOfVariables(),
                20.0),
            100)
            .setMaxEvaluations(25000)
            .build();
    algorithms.add(
        new ExperimentAlgorithm<>(algorithm, "NSGAIIb", experimentProblem, run));
  }

  private static void nsgaIIa(
      List<ExperimentAlgorithm<DoubleSolution, List<DoubleSolution>>> algorithms, int run,
      ExperimentProblem<DoubleSolution> experimentProblem) {
    Algorithm<List<DoubleSolution>> algorithm =
        new NSGAIIBuilder<>(
            experimentProblem.getProblem(),
            new SBXCrossover(1.0, 5),
            new PolynomialMutation(
                1.0 / experimentProblem.getProblem().numberOfVariables(),
                10.0),
            100)
            .setMaxEvaluations(25000)
            .build();
    algorithms.add(
        new ExperimentAlgorithm<>(algorithm, "NSGAIIa", experimentProblem, run));
  }
}
