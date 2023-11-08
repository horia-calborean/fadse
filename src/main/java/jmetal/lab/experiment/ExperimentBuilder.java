package jmetal.lab.experiment;

import java.util.ArrayList;
import java.util.List;
import jmetal.lab.experiment.util.ExperimentAlgorithm;
import jmetal.lab.experiment.util.ExperimentProblem;
import jmetal.core.qualityindicator.QualityIndicator;
import jmetal.core.solution.Solution;

/**
 * Builder for class {@link Experiment}
 *
 * @author Antonio J. Nebro
 */
public class ExperimentBuilder<S extends Solution<?>, Result extends List<S>> {
  private final String experimentName ;
  private List<ExperimentAlgorithm<S, Result>> algorithmList;
  private List<ExperimentProblem<S>> problemList;
  private String referenceFrontDirectory;
  private String experimentBaseDirectory;
  private String outputParetoFrontFileName;
  private String outputParetoSetFileName;
  private int independentRuns;
  private List<QualityIndicator> indicatorList ;
  private int numberOfCores ;

  public ExperimentBuilder(String experimentName) {
    this.experimentName = experimentName ;
    this.independentRuns = 1 ;
    this.numberOfCores = 1 ;
    this.referenceFrontDirectory = null ;
  }

  public ExperimentBuilder<S, Result> setAlgorithmList(List<ExperimentAlgorithm<S, Result>> algorithmList) {
    this.algorithmList = new ArrayList<>(algorithmList) ;

    return this ;
  }

  public ExperimentBuilder<S, Result> setProblemList(List<ExperimentProblem<S>> problemList) {
    this.problemList = problemList ;

    return this ;
  }

  public ExperimentBuilder<S, Result> setExperimentBaseDirectory(String experimentBaseDirectory) {
    this.experimentBaseDirectory = experimentBaseDirectory+"/"+experimentName ;

    return this ;
  }

  public ExperimentBuilder<S, Result> setReferenceFrontDirectory(String referenceFrontDirectory) {
    this.referenceFrontDirectory = referenceFrontDirectory ;

    return this ;
  }

  public ExperimentBuilder<S, Result> setIndicatorList(
      List<QualityIndicator> indicatorList ) {
    this.indicatorList = indicatorList ;

    return this ;
  }

  public ExperimentBuilder<S, Result> setOutputParetoFrontFileName(String outputParetoFrontFileName) {
    this.outputParetoFrontFileName = outputParetoFrontFileName ;

    return this ;
  }

  public ExperimentBuilder<S, Result> setOutputParetoSetFileName(String outputParetoSetFileName) {
    this.outputParetoSetFileName = outputParetoSetFileName ;

    return this ;
  }

  public ExperimentBuilder<S, Result> setIndependentRuns(int independentRuns) {
    this.independentRuns = independentRuns ;

    return this ;
  }

  public ExperimentBuilder<S, Result> setNumberOfCores(int numberOfCores) {
    this.numberOfCores = numberOfCores;

    return this ;
  }

  public Experiment<S, Result> build() {
    return new Experiment<S, Result>(this);
  }

  /* Getters */
  public String getExperimentName() {
    return experimentName;
  }

  public List<ExperimentAlgorithm<S, Result>> getAlgorithmList() {
    return algorithmList;
  }

  public List<ExperimentProblem<S>> getProblemList() {
    return problemList;
  }

  public String getExperimentBaseDirectory() {
    return experimentBaseDirectory;
  }

  public String getOutputParetoFrontFileName() {
    return outputParetoFrontFileName;
  }

  public String getOutputParetoSetFileName() {
    return outputParetoSetFileName;
  }

  public int getIndependentRuns() {
    return independentRuns;
  }

  public int getNumberOfCores() {
    return numberOfCores;
  }

  public String getReferenceFrontDirectory() {
    return referenceFrontDirectory;
  }

  public List<QualityIndicator> getIndicatorList() {
    return indicatorList;
  }
}
