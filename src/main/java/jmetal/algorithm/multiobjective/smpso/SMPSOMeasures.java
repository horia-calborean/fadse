package jmetal.algorithm.multiobjective.smpso;

import java.util.List;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.archive.BoundedArchive;
import jmetal.core.util.comparator.dominanceComparator.DominanceComparator;
import jmetal.core.util.evaluator.SolutionListEvaluator;
import jmetal.core.util.measure.Measurable;
import jmetal.core.util.measure.MeasureManager;
import jmetal.core.util.measure.impl.BasicMeasure;
import jmetal.core.util.measure.impl.CountingMeasure;
import jmetal.core.util.measure.impl.DurationMeasure;
import jmetal.core.util.measure.impl.SimpleMeasureManager;

/**
 * This class implements a version of SMPSO using measures
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class SMPSOMeasures extends SMPSO implements Measurable {

  protected CountingMeasure iterations;
  protected DurationMeasure durationMeasure;
  protected SimpleMeasureManager measureManager;

  protected BasicMeasure<List<DoubleSolution>> solutionListMeasure;

  /**
   * Constructor
   *
   * @param problem
   * @param swarmSize
   * @param leaders
   * @param mutationOperator
   * @param maxIterations
   * @param r1Min
   * @param r1Max
   * @param r2Min
   * @param r2Max
   * @param c1Min
   * @param c1Max
   * @param c2Min
   * @param c2Max
   * @param weightMin
   * @param weightMax
   * @param changeVelocity1
   * @param changeVelocity2
   * @param evaluator
   */
  public SMPSOMeasures(DoubleProblem problem, int swarmSize, BoundedArchive<DoubleSolution> leaders,
      MutationOperator<DoubleSolution> mutationOperator, int maxIterations, double r1Min,
      double r1Max, double r2Min, double r2Max, double c1Min, double c1Max, double c2Min,
      double c2Max, double weightMin, double weightMax, double changeVelocity1,
      double changeVelocity2, DominanceComparator<DoubleSolution> dominanceComparator,
      SolutionListEvaluator<DoubleSolution> evaluator) {
    super(problem, swarmSize, leaders, mutationOperator, maxIterations, r1Min, r1Max, r2Min, r2Max,
        c1Min, c1Max, c2Min, c2Max, weightMin, weightMax, changeVelocity1, changeVelocity2,
        dominanceComparator, evaluator);

    initMeasures();
  }

  @Override
  public void run() {
    durationMeasure.reset();
    durationMeasure.start();
    super.run();
    durationMeasure.stop();
  }

  @Override
  protected boolean isStoppingConditionReached() {
    return iterations.get() >= getMaxIterations();
  }

  @Override
  protected void initProgress() {
    iterations.reset(1);
    updateLeadersDensityEstimator();
  }

  @Override
  protected void updateProgress() {
    iterations.increment(1);
    ;
    updateLeadersDensityEstimator();

    solutionListMeasure.push(super.result());
  }

  @Override
  public MeasureManager getMeasureManager() {
    return measureManager;
  }

  /* Measures code */
  private void initMeasures() {
    durationMeasure = new DurationMeasure();
    iterations = new CountingMeasure(0);
    solutionListMeasure = new BasicMeasure<>();

    measureManager = new SimpleMeasureManager();
    measureManager.setPullMeasure("currentExecutionTime", durationMeasure);
    measureManager.setPullMeasure("currentIteration", iterations);

    measureManager.setPushMeasure("currentPopulation", solutionListMeasure);
    measureManager.setPushMeasure("currentIteration", iterations);
  }

  @Override
  public String name() {
    return "SMPSOMeasures";
  }

  @Override
  public String description() {
    return "SMPSO. Version using measures";
  }
}
