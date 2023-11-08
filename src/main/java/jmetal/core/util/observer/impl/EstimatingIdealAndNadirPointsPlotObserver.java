package jmetal.core.util.observer.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.knowm.xchart.XYChart;
import jmetal.core.solution.Solution;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.archive.impl.NonDominatedSolutionListArchive;
import jmetal.core.util.errorchecking.Check;
import jmetal.core.util.observable.Observable;
import jmetal.core.util.observer.Observer;
import jmetal.core.util.plot.FrontScatterPlot;
import jmetal.core.util.point.impl.IdealPoint;
import jmetal.core.util.point.impl.NadirPoint;

/**
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class EstimatingIdealAndNadirPointsPlotObserver<S extends Solution<?>> implements
    Observer<Map<String, Object>> {

  private final FrontScatterPlot chart;
  private Integer evaluations;
  private final int plotUpdateFrequency;

  private IdealPoint idealPoint = null;
  private NadirPoint nadirPoint = null;

  private String plotTitle;

  private NonDominatedSolutionListArchive<S> nonDominatedSolutionListArchive;

  /**
   * Constructor
   */
  public EstimatingIdealAndNadirPointsPlotObserver(String title, String xAxisTitle, String yAxisTitle,
      String legend,
      int plotUpdateFrequency, long delay) {
    chart = new FrontScatterPlot(title, xAxisTitle, yAxisTitle, legend);
    this.plotUpdateFrequency = plotUpdateFrequency;
    this.plotTitle = title;
    setPoint(0, 0, "Ideal point");
    setPoint(0, 0, "Nadir point");

    nonDominatedSolutionListArchive = new NonDominatedSolutionListArchive<>();
    chart.delay(delay);
  }

  public void setFront(double[][] front, String frontName) {
    chart.setFront(front, frontName);
  }

  public void setPoint(double x, double y, String pointName) {
    chart.addPoint(x, y, pointName);
  }

  public void updatePoint(double x, double y, String pointName) {
    chart.updatePoint(x, y, pointName);
  }

  /**
   * This method displays a front (population)
   *
   * @param data Map of pairs (key, value)
   */
  @Override
  public void update(Observable<Map<String, Object>> observable, Map<String, Object> data) {
    evaluations = (Integer) data.get("EVALUATIONS");
    List<S> population = (List<S>) data.get("POPULATION");

    Check.notNull(population);

    nonDominatedSolutionListArchive = new NonDominatedSolutionListArchive<>() ;
    nonDominatedSolutionListArchive.addAll(population);

    if (idealPoint == null) {
      idealPoint = new IdealPoint(population.get(0).objectives().length);
    }

    nadirPoint = new NadirPoint(population.get(0).objectives().length);
    idealPoint.update(nonDominatedSolutionListArchive.solutions());
    nadirPoint.update(nonDominatedSolutionListArchive.solutions());

    List<S> solutionsToPlot = nonDominatedSolutionListArchive.solutions() ;

    if (evaluations != null) {
      if (evaluations % plotUpdateFrequency == 0) {
        List<Double> objective1 = solutionsToPlot.stream().map(s -> s.objectives()[0])
            .collect(Collectors.toList());
        List<Double> objective2 = solutionsToPlot.stream().map(s -> s.objectives()[1])
            .collect(Collectors.toList());
        chart.chartTitle(plotTitle + ". Evaluations: " + evaluations);
        chart.updatePoint(idealPoint.value(0), idealPoint.value(1), "Ideal point");
        chart.updatePoint(nadirPoint.value(0), nadirPoint.value(1), "Nadir point");
        chart.updateChart(objective1, objective2);
      }
    } else {
      JMetalLogger.logger.warning(getClass().getName() +
          " : insufficient for generating real time information." +
          " Either EVALUATIONS or POPULATION keys have not been registered yet by the algorithm");
    }
  }

  public XYChart chart() {
    return chart.chart();
  }
}
