package jmetal.lab.experiment.studies;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import jmetal.core.algorithm.Algorithm;
import jmetal.algorithm.multiobjective.moead.AbstractMOEAD;
import jmetal.algorithm.multiobjective.moead.MOEADBuilder;
import jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import jmetal.algorithm.multiobjective.smpso.SMPSOBuilder;
import jmetal.lab.experiment.Experiment;
import jmetal.lab.experiment.ExperimentBuilder;
import jmetal.lab.experiment.component.impl.ComputeQualityIndicators;
import jmetal.lab.experiment.component.impl.ExecuteAlgorithms;
import jmetal.lab.experiment.component.impl.GenerateBoxplotsWithR;
import jmetal.lab.experiment.component.impl.GenerateFriedmanHolmTestTables;
import jmetal.lab.experiment.component.impl.GenerateHtmlPages;
import jmetal.lab.experiment.component.impl.GenerateLatexTablesWithStatistics;
import jmetal.lab.experiment.component.impl.GenerateWilcoxonTestTablesWithR;
import jmetal.lab.experiment.util.ExperimentAlgorithm;
import jmetal.lab.experiment.util.ExperimentProblem;
import jmetal.lab.visualization.StudyVisualizer;
import jmetal.core.operator.crossover.impl.DifferentialEvolutionCrossover;
import jmetal.core.operator.crossover.impl.SBXCrossover;
import jmetal.core.operator.mutation.impl.PolynomialMutation;
import jmetal.core.problem.Problem;
import jmetal.core.problem.doubleproblem.DoubleProblem;
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
import jmetal.core.util.archive.impl.CrowdingDistanceArchive;
import jmetal.core.util.errorchecking.JMetalException;
import jmetal.core.util.evaluator.impl.SequentialSolutionListEvaluator;

/**
 * Example of experimental study based on solving the ZDT problems with the algorithms NSGAII,
 * MOEA/D, and SMPSO.
 *
 * This experiment assumes that the reference Pareto front are known and that, given a problem named
 * P, there is a corresponding file called P.csv containing its corresponding Pareto front. If this
 * is not the case, please refer to class {@link DTLZStudy} to see an example of how to explicitly
 * indicate the name of those files.
 *
 * Five quality indicators are used for performance assessment: {@link Epsilon}, {@link Spread},
 * {@link GenerationalDistance}, {@link PISAHypervolume}, and {@link InvertedGenerationalDistancePlus}.
 *
 * The steps to carry out are:
 * 1. Configure the experiment
 * 2. Execute the algorithms
 * 3. Compute que quality indicators
 * 4. Generate Latex tables reporting means and medians, and tables with statistical tests
 * 5. Generate HTML pages with tables, boxplots, and fronts.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */

public class ZDTStudy {
  private static final int INDEPENDENT_RUNS = 25;

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      throw new JMetalException("Missing argument: experimentBaseDirectory");
    }
    String experimentBaseDirectory = args[0];

    List<ExperimentProblem<DoubleSolution>> problemList = List.of(
            new ExperimentProblem<>(new ZDT1()),
            // new ExperimentProblem<>(new ZDT1().setReferenceFront("front.csv"))
            new ExperimentProblem<>(new ZDT2()),
            new ExperimentProblem<>(new ZDT3()),
            new ExperimentProblem<>(new ZDT4()),
            new ExperimentProblem<>(new ZDT6()));

    List<ExperimentAlgorithm<DoubleSolution, List<DoubleSolution>>> algorithmList =
            configureAlgorithmList(problemList);

    Experiment<DoubleSolution, List<DoubleSolution>> experiment =
            new ExperimentBuilder<DoubleSolution, List<DoubleSolution>>("ZDTStudy")
                    .setAlgorithmList(algorithmList)
                    .setProblemList(problemList)
                    .setReferenceFrontDirectory("resources/referenceFrontsCSV")
                    .setExperimentBaseDirectory(experimentBaseDirectory)
                    .setOutputParetoFrontFileName("FUN")
                    .setOutputParetoSetFileName("VAR")
                    .setIndicatorList(List.of(
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
    new GenerateFriedmanHolmTestTables<>(experiment).run();
    new GenerateWilcoxonTestTablesWithR<>(experiment).run();
    new GenerateBoxplotsWithR<>(experiment).setRows(2).setColumns(3).run();
    new GenerateHtmlPages<>(experiment, StudyVisualizer.TYPE_OF_FRONT_TO_SHOW.MEDIAN).run() ;
  }

  /**
   * The algorithm list is composed of pairs {@link Algorithm} + {@link Problem} which form part of
   * a {@link ExperimentAlgorithm}, which is a decorator for class {@link Algorithm}.
   */
  static List<ExperimentAlgorithm<DoubleSolution, List<DoubleSolution>>> configureAlgorithmList(
          List<ExperimentProblem<DoubleSolution>> problemList) {
    List<ExperimentAlgorithm<DoubleSolution, List<DoubleSolution>>> algorithms = new ArrayList<>();
    for (int run = 0; run < INDEPENDENT_RUNS; run++) {
      for (var experimentProblem : problemList) {
        smpso(algorithms, run, experimentProblem);
        nsgaii(algorithms, run, experimentProblem);
        moead(algorithms, run, experimentProblem);
      }
    }
    return algorithms;
  }

  private static void moead(
      List<ExperimentAlgorithm<DoubleSolution, List<DoubleSolution>>> algorithms, int run,
      ExperimentProblem<DoubleSolution> experimentProblem) {
    Algorithm<List<DoubleSolution>> algorithm = new MOEADBuilder(experimentProblem.getProblem(), MOEADBuilder.Variant.MOEAD)
            .setCrossover(new DifferentialEvolutionCrossover(1.0, 0.5, DifferentialEvolutionCrossover.DE_VARIANT.RAND_1_BIN))
            .setMutation(new PolynomialMutation(1.0 / experimentProblem.getProblem().numberOfVariables(),
                    20.0))
            .setMaxEvaluations(25000)
            .setPopulationSize(100)
            .setResultPopulationSize(100)
            .setNeighborhoodSelectionProbability(0.9)
            .setMaximumNumberOfReplacedSolutions(2)
            .setNeighborSize(20)
            .setFunctionType(AbstractMOEAD.FunctionType.TCHE)
            .build();

    algorithms.add(new ExperimentAlgorithm<>(algorithm, experimentProblem, run));
  }

  private static void nsgaii(
      List<ExperimentAlgorithm<DoubleSolution, List<DoubleSolution>>> algorithms, int run,
      ExperimentProblem<DoubleSolution> experimentProblem) {
    Algorithm<List<DoubleSolution>> algorithm = new NSGAIIBuilder<DoubleSolution>(
            experimentProblem.getProblem(),
            new SBXCrossover(1.0, 20.0),
            new PolynomialMutation(1.0 / experimentProblem.getProblem().numberOfVariables(),
                    20.0),
            100)
            .build();
    algorithms.add(new ExperimentAlgorithm<>(algorithm, experimentProblem, run));
  }

  private static void smpso(
      List<ExperimentAlgorithm<DoubleSolution, List<DoubleSolution>>> algorithms, int run,
      ExperimentProblem<DoubleSolution> experimentProblem) {
    double mutationProbability = 1.0 / experimentProblem.getProblem().numberOfVariables();
    double mutationDistributionIndex = 20.0;
    Algorithm<List<DoubleSolution>> algorithm = new SMPSOBuilder(
            (DoubleProblem) experimentProblem.getProblem(),
            new CrowdingDistanceArchive<DoubleSolution>(100))
            .setMutation(new PolynomialMutation(mutationProbability, mutationDistributionIndex))
            .setMaxIterations(250)
            .setSwarmSize(100)
            .setSolutionListEvaluator(new SequentialSolutionListEvaluator<>())
            .build();
    algorithms.add(new ExperimentAlgorithm<>(algorithm, experimentProblem, run));
  }
}
