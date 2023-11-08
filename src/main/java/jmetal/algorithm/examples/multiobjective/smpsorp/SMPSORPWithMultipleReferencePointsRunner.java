package jmetal.algorithm.examples.multiobjective.smpsorp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jmetal.algorithm.examples.AlgorithmRunner;
import jmetal.algorithm.multiobjective.smpso.SMPSORP;
import jmetal.core.operator.mutation.impl.PolynomialMutation;
import jmetal.problem.multiobjective.zdt.ZDT2;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.archivewithreferencepoint.ArchiveWithReferencePoint;
import jmetal.core.util.archivewithreferencepoint.impl.CrowdingDistanceArchiveWithReferencePoint;
import jmetal.core.util.comparator.dominanceComparator.impl.DefaultDominanceComparator;
import jmetal.core.util.evaluator.impl.SequentialSolutionListEvaluator;
import jmetal.core.util.fileoutput.SolutionListOutput;
import jmetal.core.util.fileoutput.impl.DefaultFileOutputContext;

public class SMPSORPWithMultipleReferencePointsRunner {

  /**
   * Program to run the SMPSORP algorithm with two reference points. SMPSORP is described in
   * "Extending the Speed-constrained Multi-Objective PSO (SMPSO) With Reference Point Based
   * Preference Articulation. Antonio J. Nebro, Juan J. Durillo, José García-Nieto, Cristóbal
   * Barba-González, Javier Del Ser, Carlos A. Coello Coello, Antonio Benítez-Hidalgo, José F.
   * Aldana-Montes. Parallel Problem Solving from Nature -- PPSN XV. Lecture Notes In Computer
   * Science, Vol. 11101, pp. 298-310. 2018"
   *
   * @author Antonio J. Nebro
   */
  public static void main(String[] args) {
    var problem = new ZDT2();

    List<List<Double>> referencePoints;
    referencePoints = new ArrayList<>();
    referencePoints.add(Arrays.asList(0.2, 0.8));
    referencePoints.add(Arrays.asList(0.7, 0.4));

    double mutationProbability = 1.0 / problem.numberOfVariables();
    double mutationDistributionIndex = 20.0;
    var mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

    int maxIterations = 250;
    int swarmSize = 100;

    List<ArchiveWithReferencePoint<DoubleSolution>> archivesWithReferencePoints = new ArrayList<>();

    for (int i = 0; i < referencePoints.size(); i++) {
      archivesWithReferencePoints.add(
          new CrowdingDistanceArchiveWithReferencePoint<>(
              swarmSize / referencePoints.size(), referencePoints.get(i)));
    }

    var algorithm = new SMPSORP(problem,
        swarmSize,
        archivesWithReferencePoints,
        referencePoints,
        mutation,
        maxIterations,
        0.0, 1.0,
        0.0, 1.0,
        2.5, 1.5,
        2.5, 1.5,
        0.1, 0.1,
        -1.0, -1.0,
        new DefaultDominanceComparator<>(),
        new SequentialSolutionListEvaluator<>());

    AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm)
        .execute();

    List<DoubleSolution> population = algorithm.result();
    long computingTime = algorithmRunner.getComputingTime();

    JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");

    new SolutionListOutput(population)
        .setVarFileOutputContext(new DefaultFileOutputContext("VAR.tsv"))
        .setFunFileOutputContext(new DefaultFileOutputContext("FUN.tsv"))
        .print();

    for (int i = 0; i < archivesWithReferencePoints.size(); i++) {
      new SolutionListOutput(archivesWithReferencePoints.get(i).solutions())
          .setVarFileOutputContext(new DefaultFileOutputContext("VAR" + i + ".tsv"))
          .setFunFileOutputContext(new DefaultFileOutputContext("FUN" + i + ".tsv"))
          .print();
    }

    System.exit(0);
  }
}
