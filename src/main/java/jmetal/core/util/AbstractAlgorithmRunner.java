package jmetal.core.util;

import static jmetal.core.util.SolutionListUtils.getMatrixWithObjectiveValues;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import jmetal.core.qualityindicator.QualityIndicatorUtils;
import jmetal.core.solution.Solution;
import jmetal.core.util.fileoutput.SolutionListOutput;
import jmetal.core.util.fileoutput.impl.DefaultFileOutputContext;
import jmetal.core.util.pseudorandom.JMetalRandom;

/**
 * Abstract class for Runner classes
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public abstract class AbstractAlgorithmRunner {
  /**
   * Write the population into two files and prints some data on screen
   *
   * @param population
   */
  public static void printFinalSolutionSet(List<? extends Solution<?>> population) {
    new SolutionListOutput(population)
        .setVarFileOutputContext(new DefaultFileOutputContext("VAR.csv", ","))
        .setFunFileOutputContext(new DefaultFileOutputContext("FUN.csv", ","))
        .print();

    JMetalLogger.logger.info("Random seed: " + JMetalRandom.getInstance().getSeed());
    JMetalLogger.logger.info("Objectives values have been written to file FUN.tsv");
    JMetalLogger.logger.info("Variables values have been written to file VAR.tsv");
  }

  /**
   * Print all the available quality indicators
   *
   * @param population
   * @param paretoFrontFile
   * @throws FileNotFoundException
   */
  @Deprecated
  public static <S extends Solution<?>> void printQualityIndicators(
      List<S> population, String paretoFrontFile) {

    try {
      QualityIndicatorUtils.printQualityIndicators(
          getMatrixWithObjectiveValues(population), VectorUtils.readVectors(paretoFrontFile, ","));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
