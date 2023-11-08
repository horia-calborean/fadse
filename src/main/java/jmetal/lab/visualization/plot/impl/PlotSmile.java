package jmetal.lab.visualization.plot.impl;

import java.lang.reflect.InvocationTargetException;
import jmetal.lab.visualization.plot.PlotFront;
import jmetal.core.util.errorchecking.Check;
import smile.plot.swing.ScatterPlot;


public class PlotSmile implements PlotFront {
  private double[][] matrix;
  private String plotTitle;

  public PlotSmile(double[][] matrix) {
    this(matrix, "Front") ;
  }

  public PlotSmile(double[][] matrix, String plotTitle) {
    Check.notNull(matrix);
    Check.that(matrix.length >= 1, "The data matrix is empty");

    this.matrix = matrix;
    this.plotTitle = plotTitle ;
  }

  @Override
  public void plot() {
    var canvas = ScatterPlot.of(matrix).canvas() ;
    canvas.setTitle(plotTitle) ;
    try {
      canvas.window() ;
    } catch (InterruptedException e) {
      e.printStackTrace();
      Thread.currentThread().interrupt();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
  }
}
