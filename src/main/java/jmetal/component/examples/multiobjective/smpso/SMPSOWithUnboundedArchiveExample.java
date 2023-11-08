package jmetal.component.examples.multiobjective.smpso;

import java.io.IOException;
import java.util.List;
import jmetal.component.algorithm.ParticleSwarmOptimizationAlgorithm;
import jmetal.component.algorithm.multiobjective.SMPSOBuilder;
import jmetal.component.catalogue.common.evaluation.impl.SequentialEvaluationWithArchive;
import jmetal.component.catalogue.common.termination.Termination;
import jmetal.component.catalogue.common.termination.impl.TerminationByEvaluations;
import jmetal.lab.visualization.plot.PlotFront;
import jmetal.lab.visualization.plot.impl.Plot3D;
import jmetal.core.problem.Problem;
import jmetal.problem.ProblemFactory;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.core.qualityindicator.QualityIndicatorUtils;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.SolutionListUtils;
import jmetal.core.util.VectorUtils;
import jmetal.core.util.archive.Archive;
import jmetal.core.util.archive.impl.BestSolutionsArchive;
import jmetal.core.util.archive.impl.NonDominatedSolutionListArchive;
import jmetal.core.util.errorchecking.JMetalException;
import jmetal.core.util.fileoutput.SolutionListOutput;
import jmetal.core.util.fileoutput.impl.DefaultFileOutputContext;
import jmetal.core.util.legacy.front.impl.ArrayFront;
import jmetal.core.util.pseudorandom.JMetalRandom;

public class SMPSOWithUnboundedArchiveExample {
  public static void main(String[] args) throws JMetalException, IOException {
    String problemName = "jmetal.problem.multiobjective.dtlz.DTLZ2";
    String referenceParetoFront = "resources/referenceFrontsCSV/DTLZ2.3D.csv";

    Problem<DoubleSolution> problem = ProblemFactory.<DoubleSolution>loadProblem(problemName);

    int swarmSize = 100 ;
    Termination termination = new TerminationByEvaluations(50000);

    Archive<DoubleSolution> externalUnboundedArchive = new BestSolutionsArchive<>(new NonDominatedSolutionListArchive<>(), swarmSize) ;

    ParticleSwarmOptimizationAlgorithm smpso = new SMPSOBuilder(
        (DoubleProblem) problem,
        swarmSize)
        .setTermination(termination)
        .setEvaluation(new SequentialEvaluationWithArchive<>(problem, externalUnboundedArchive))
        .build();

    smpso.run();

    List<DoubleSolution> population = externalUnboundedArchive.solutions();
    JMetalLogger.logger.info("Total execution time : " + smpso.totalComputingTime() + "ms");
    JMetalLogger.logger.info("Number of evaluations: " + smpso.evaluation());

    new SolutionListOutput(population)
        .setVarFileOutputContext(new DefaultFileOutputContext("VAR.csv", ","))
        .setFunFileOutputContext(new DefaultFileOutputContext("FUN.csv", ","))
        .print();

    JMetalLogger.logger.info("Random seed: " + JMetalRandom.getInstance().getSeed());
    JMetalLogger.logger.info("Objectives values have been written to file FUN.csv");
    JMetalLogger.logger.info("Variables values have been written to file VAR.csv");

    QualityIndicatorUtils.printQualityIndicators(
        SolutionListUtils.getMatrixWithObjectiveValues(population),
        VectorUtils.readVectors(referenceParetoFront, ","));

    PlotFront plot = new Plot3D(new ArrayFront(population).getMatrix(), problem.name() + " (NSGA-II)");
    plot.plot();
  }
}
