package jmetal.lab.experiment.component.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import jmetal.lab.experiment.Experiment;
import jmetal.lab.experiment.component.ExperimentComponent;
import jmetal.lab.experiment.util.ExperimentAlgorithm;
import jmetal.lab.experiment.util.ExperimentProblem;
import jmetal.core.solution.pointsolution.PointSolution;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.archive.impl.NonDominatedSolutionListArchive;
import jmetal.core.util.fileoutput.SolutionListOutput;
import jmetal.core.util.legacy.front.Front;
import jmetal.core.util.legacy.front.impl.ArrayFront;
import jmetal.core.util.legacy.front.util.FrontUtils;
import jmetal.core.util.solutionattribute.impl.GenericSolutionAttribute;

/**
 * This class computes a reference Pareto front from a set of files. Once the algorithms of an
 * jmetal.experiment have been executed through running an instance of class {@link ExecuteAlgorithms},
 * all the obtained fronts of all the algorithms are gathered per problem; then, the dominated solutions
 * are removed and the final result is a file per problem containing the reference Pareto front.
 *
 * By default, the files are stored in a directory called "referenceFront", which is located in the
 * jmetal.experiment base directory. Each front is named following the scheme "problemName.rf".
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class GenerateReferenceParetoFront implements ExperimentComponent {
  private final Experiment<?, ?> experiment;
  
  public GenerateReferenceParetoFront(Experiment<?, ?> experimentConfiguration) {
    this.experiment = experimentConfiguration ;

    experiment.removeDuplicatedAlgorithms();
  }

  /**
   * The run() method creates de output directory and compute the fronts
   */
  @Override
  public void run() throws IOException {
    String outputDirectoryName = experiment.getReferenceFrontDirectory() ;

    createOutputDirectory(outputDirectoryName) ;

    List<String> referenceFrontFileNames = new LinkedList<>() ;
    for (ExperimentProblem<?> problem : experiment.getProblemList()) {
      NonDominatedSolutionListArchive<PointSolution> nonDominatedSolutionArchive =
          new NonDominatedSolutionListArchive<PointSolution>() ;

      for (ExperimentAlgorithm<?,?> algorithm : experiment.getAlgorithmList()) {
        String problemDirectory = experiment.getExperimentBaseDirectory() + "/data/" +
            algorithm.getAlgorithmTag() + "/" + problem.getTag() ;

        for (int i = 0; i < experiment.getIndependentRuns(); i++) {
          String frontFileName = problemDirectory + "/" + experiment.getOutputParetoFrontFileName() +
              i + ".csv";
          Front front = new ArrayFront(frontFileName, ",") ;
          List<PointSolution> solutionList = FrontUtils.convertFrontToSolutionList(front) ;
          GenericSolutionAttribute<PointSolution, String> solutionAttribute = new GenericSolutionAttribute<PointSolution, String>()  ;

          for (PointSolution solution : solutionList) {
            solutionAttribute.setAttribute(solution, algorithm.getAlgorithmTag());
            nonDominatedSolutionArchive.add(solution) ;
          }
        }
      }
      String referenceSetFileName = outputDirectoryName + "/" + problem.getTag() + ".csv" ;
      referenceFrontFileNames.add(problem.getTag() + ".csv");
      new SolutionListOutput(nonDominatedSolutionArchive.solutions())
          .printObjectivesToFile(referenceSetFileName, ",");

      writeFilesWithTheSolutionsContributedByEachAlgorithm(outputDirectoryName, problem,
          nonDominatedSolutionArchive.solutions()) ;
    }

  }

  private File createOutputDirectory(String outputDirectoryName) {
    File outputDirectory ;
    outputDirectory = new File(outputDirectoryName) ;
    if (!outputDirectory.exists()) {
      boolean result = new File(outputDirectoryName).mkdir() ;
      JMetalLogger.logger.info("Creating " + outputDirectoryName + ". Status = " + result);
    }

    return outputDirectory ;
  }

  private void writeFilesWithTheSolutionsContributedByEachAlgorithm(
      String outputDirectoryName, ExperimentProblem<?> problem,
      List<PointSolution> nonDominatedSolutions) throws IOException {
    GenericSolutionAttribute<PointSolution, String> solutionAttribute = new GenericSolutionAttribute<PointSolution, String>()  ;

    for (ExperimentAlgorithm<?, ?> algorithm : experiment.getAlgorithmList()) {
      List<PointSolution> solutionsPerAlgorithm = new ArrayList<>() ;
      for (PointSolution solution : nonDominatedSolutions) {
        if (algorithm.getAlgorithmTag().equals(solutionAttribute.getAttribute(solution))) {
          solutionsPerAlgorithm.add(solution) ;
        }
      }

      new SolutionListOutput(solutionsPerAlgorithm)
          .printObjectivesToFile(
              outputDirectoryName + "/" + problem.getTag() + "." +
                  algorithm.getAlgorithmTag() + ".csv", ","
          );
    }
  }
}
