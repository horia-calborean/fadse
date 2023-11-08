package jmetal.lab.experiment.util;

import java.io.File;
import java.util.List;
import jmetal.core.algorithm.Algorithm;
import jmetal.lab.experiment.Experiment;
import jmetal.core.solution.Solution;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.fileoutput.SolutionListOutput;
import jmetal.core.util.fileoutput.impl.DefaultFileOutputContext;

/**
 * Class defining tasks for the execution of algorithms in parallel.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class ExperimentAlgorithm<S extends Solution<?>, Result extends List<S>> {
  private Algorithm<Result> algorithm;
  private String algorithmTag;
  private String problemTag;
  private int runId;

  /** Constructor */
  public ExperimentAlgorithm(
      Algorithm<Result> algorithm, String algorithmTag, ExperimentProblem<S> problem, int runId) {
    this.algorithm = algorithm;
    this.algorithmTag = algorithmTag;
    this.problemTag = problem.getTag();
    this.runId = runId;
  }

  public ExperimentAlgorithm(Algorithm<Result> algorithm, ExperimentProblem<S> problem, int runId) {

    this(algorithm, algorithm.name(), problem, runId);
  }

  public void runAlgorithm(Experiment<?, ?> experimentData) {
    String outputDirectoryName =
        experimentData.getExperimentBaseDirectory() + "/data/" + algorithmTag + "/" + problemTag;

    File outputDirectory = new File(outputDirectoryName);
    if (!outputDirectory.exists()) {
      boolean result = new File(outputDirectoryName).mkdirs();
      if (result) {
        JMetalLogger.logger.info("Creating " + outputDirectoryName);
      } else {
        JMetalLogger.logger.severe("Creating " + outputDirectoryName + " failed");
      }
    }

    String funFile =
        outputDirectoryName + "/" + experimentData.getOutputParetoFrontFileName() + runId + ".csv";
    String varFile =
        outputDirectoryName + "/" + experimentData.getOutputParetoSetFileName() + runId + ".csv";
    String message = " Running algorithm: "
        + algorithmTag
        + ", problem: "
        + problemTag
        + ", run: "
        + runId
        + ", funFile: "
        + funFile ;
    JMetalLogger.logger.info(message);

    try {
      algorithm.run();
      Result population = algorithm.result();

      new SolutionListOutput(population)
          .setVarFileOutputContext(new DefaultFileOutputContext(varFile, ","))
          .setFunFileOutputContext(new DefaultFileOutputContext(funFile, ","))
          .print();
    } catch (Exception exception) {
      JMetalLogger.logger.warning("Execution failed: " + funFile + " has not been created.");
    }
  }

  public Algorithm<Result> getAlgorithm() {
    return algorithm;
  }

  public String getAlgorithmTag() {
    return algorithmTag;
  }

  public String getProblemTag() {
    return problemTag;
  }

  public int getRunId() {
    return this.runId;
  }
}
