package jmetal.algorithm.examples.multiobjective.wasfga;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import jmetal.algorithm.examples.AlgorithmRunner;
import jmetal.algorithm.multiobjective.wasfga.WASFGAMeasures;
import jmetal.core.operator.crossover.impl.SBXCrossover;
import jmetal.core.operator.mutation.impl.PolynomialMutation;
import jmetal.core.operator.selection.impl.BinaryTournamentSelection;
import jmetal.core.problem.Problem;
import jmetal.problem.ProblemFactory;
import jmetal.core.qualityindicator.QualityIndicatorUtils;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.AbstractAlgorithmRunner;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.SolutionListUtils;
import jmetal.core.util.VectorUtils;
import jmetal.core.util.chartcontainer.ChartContainer;
import jmetal.core.util.comparator.RankingAndCrowdingDistanceComparator;
import jmetal.core.util.evaluator.impl.SequentialSolutionListEvaluator;
import jmetal.core.util.measure.MeasureListener;
import jmetal.core.util.measure.MeasureManager;
import jmetal.core.util.measure.impl.BasicMeasure;
import jmetal.core.util.measure.impl.CountingMeasure;

public class WASFGAMeasuresRunner extends AbstractAlgorithmRunner {

  /**
   * @param args Command line arguments.
   */
  public static void main(String[] args) throws IOException {
    String problemName = "jmetal.core.problem.multiobjective.zdt.ZDT1";
    String referenceParetoFront = "resources/referenceFrontsCSV/ZDT1.csv";

    Problem<DoubleSolution> problem = ProblemFactory.<DoubleSolution>loadProblem(problemName);

    List<Double> referencePoint = new ArrayList<>();
    referencePoint.add(0.6);
    referencePoint.add(0.4);

    double crossoverProbability = 0.9;
    double crossoverDistributionIndex = 20.0;
    var crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);

    double mutationProbability = 1.0 / problem.numberOfVariables();
    double mutationDistributionIndex = 20.0;
    var mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

    var selection = new BinaryTournamentSelection<DoubleSolution>(
        new RankingAndCrowdingDistanceComparator<DoubleSolution>());

    double epsilon = 0.01;
    var algorithm = new WASFGAMeasures<DoubleSolution>(
        problem,
        100,
        250,
        crossover, mutation, selection, new SequentialSolutionListEvaluator<DoubleSolution>(),
        epsilon,
        referencePoint);

    /* Measure management */
    MeasureManager measureManager = ((WASFGAMeasures<DoubleSolution>) algorithm).getMeasureManager();

    BasicMeasure<List<DoubleSolution>> solutionListMeasure = (BasicMeasure<List<DoubleSolution>>) measureManager
        .<List<DoubleSolution>>getPushMeasure("currentPopulation");
    CountingMeasure iterationMeasure = (CountingMeasure) measureManager.<Long>getPushMeasure(
        "currentEvaluation");

    ChartContainer chart = new ChartContainer(algorithm.name(), 200);
    chart.setFrontChart(0, 1, referenceParetoFront);
    chart.setReferencePoint(referencePoint);
    chart.setVarChart(0, 1);
    chart.initChart();

    solutionListMeasure.register(new ChartListener(chart));
    iterationMeasure.register(new IterationListener(chart));

    /* End of measure management */

    AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm)
        .execute();

    chart.saveChart("WASFGA", BitmapFormat.PNG);
    List<DoubleSolution> population = algorithm.result();
    long computingTime = algorithmRunner.getComputingTime();

    JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");

    printFinalSolutionSet(population);
    QualityIndicatorUtils.printQualityIndicators(
        SolutionListUtils.getMatrixWithObjectiveValues(population),
        VectorUtils.readVectors(referenceParetoFront, ","));
  }

  private static class ChartListener implements MeasureListener<List<DoubleSolution>> {

    private ChartContainer chart;
    private int iteration = 0;

    public ChartListener(ChartContainer chart) {
      this.chart = chart;
      this.chart.getFrontChart().setTitle("Iteration: " + this.iteration);
    }

    private void refreshChart(List<DoubleSolution> solutionList) {
      if (this.chart != null) {
        iteration++;
        this.chart.getFrontChart().setTitle("Iteration: " + this.iteration);
        this.chart.updateFrontCharts(solutionList);
        this.chart.refreshCharts();
      }
    }

    @Override
    synchronized public void measureGenerated(List<DoubleSolution> solutions) {
      refreshChart(solutions);
    }
  }

  private static class IterationListener implements MeasureListener<Long> {

    ChartContainer chart;

    public IterationListener(ChartContainer chart) {
      this.chart = chart;
      this.chart.getFrontChart().setTitle("Iteration: " + 0);
    }

    @Override
    synchronized public void measureGenerated(Long iteration) {
      if (this.chart != null) {
        this.chart.getFrontChart().setTitle("Iteration: " + iteration);
      }
    }
  }
}
