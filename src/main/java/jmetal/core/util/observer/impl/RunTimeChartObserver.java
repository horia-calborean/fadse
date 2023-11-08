package jmetal.core.util.observer.impl;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import jmetal.core.solution.Solution;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.chartcontainer.GenericChartContainer;
import jmetal.core.util.observable.Observable;
import jmetal.core.util.observer.Observer;

/**
 * This observer prints a chart in real time showing the current Pareto front approximation produced
 * by an algorithm. It requires a two pairs in the map used in the update() method:
 *  * - (EVALUATIONS, int)
 *  * - (POPULATION, List<Solution>)
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class RunTimeChartObserver<S extends Solution<?>> implements Observer<Map<String, Object>> {
  private GenericChartContainer<S> chart;
  private Integer evaluations ;
  private int plotUpdateFrequency ;

  /**
   * Constructor
   * @param legend Legend to be included in the chart
   * @param delay Display delay
   */
  public RunTimeChartObserver(String legend, int delay) {
    this(legend, delay, "") ;
  }

  /**
   *
   * @param legend Legend to be included in the chart
   * @param delay Display delay
   * @param referenceFrontName File name containing a reference front
   */
  public RunTimeChartObserver(String legend, int delay, String referenceFrontName) {
    this(legend, delay, 1, referenceFrontName, "Objective A", "Objective B") ;
  }

  public RunTimeChartObserver(String legend, int delay, int plotUpdateFrequency, String referenceFrontName) {
    this(legend, delay, plotUpdateFrequency, referenceFrontName, "Function 1", "Function 2") ;
  }

  public RunTimeChartObserver(String legend, int delay, int plotUpdateFrequency, String referenceFrontName,
      String xLabel, String yLabel) {
    this.plotUpdateFrequency = plotUpdateFrequency ;
    chart = new GenericChartContainer<S>(legend, delay) ;
    try {
      chart.setFrontChart(0, 1, referenceFrontName, xLabel, yLabel);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    chart.initChart();
  }

  public void setReferencePoints(List<List<Double>> referencePoins) {
    chart.setReferencePoint(referencePoins);
  }

  /**
   * This method is used to set a list of reference points; it is used by reference-point based
   * algorithms.
   * @param referencePointList
   */
  public void setReferencePointList(List<List<Double>> referencePointList) {
    chart.setReferencePoint(referencePointList);
  }

  /**
   * This method displays a front (population)
   * @param data Map of pairs (key, value)
   */
  @Override
  public void update(Observable<Map<String, Object>> observable, Map<String, Object> data) {
    evaluations = (Integer)data.get("EVALUATIONS") ;
    List<S> population = (List<S>) data.get("POPULATION");

    if (evaluations!=null && population!=null) {
      if ((this.chart != null) && (evaluations%plotUpdateFrequency == 0)){
        this.chart.getFrontChart().setTitle("Evaluation: " + evaluations);
        this.chart.updateFrontCharts(population);
        this.chart.refreshCharts();
      }
    } else {
      JMetalLogger.logger.warning(getClass().getName()+
        " : insufficient for generating real time information." +
        " Either EVALUATIONS or POPULATION keys have not been registered yet by the algorithm");
    }
  }

  public GenericChartContainer<S> getChart() {
    return chart ;
  }

  public String getName() {
    return "Runtime chart observer";
  }

  @Override
  public String toString() {
    return getName() ;
  }
}
