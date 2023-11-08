package jmetal.component.examples.multiobjective.smpso;

import java.util.List;
import jmetal.component.algorithm.ParticleSwarmOptimizationAlgorithm;
import jmetal.component.algorithm.multiobjective.SMPSOBuilder;
import jmetal.component.catalogue.common.termination.Termination;
import jmetal.component.catalogue.common.termination.impl.TerminationByEvaluations;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.problem.multiobjective.ebes.Ebes;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.AbstractAlgorithmRunner;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.fileoutput.SolutionListOutput;
import jmetal.core.util.fileoutput.impl.DefaultFileOutputContext;
import jmetal.core.util.observer.impl.RunTimeChartObserver;
import jmetal.core.util.pseudorandom.JMetalRandom;

/**
 * Class for configuring and running the SMPSO algorithm
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class SMPSOSolvingEbes extends AbstractAlgorithmRunner {
  public static void main(String[] args) throws Exception {
    String referenceParetoFront = null;

    DoubleProblem problem = new Ebes("resources/ebes/Mobile_Bridge_25N_35B_8G_16OrdZXY.ebe", new String[]{"W", "D"}) ;

    int swarmSize = 100 ;
    Termination termination = new TerminationByEvaluations(25000);

    ParticleSwarmOptimizationAlgorithm smpso = new SMPSOBuilder(
        (DoubleProblem) problem,
        swarmSize)
        .setTermination(termination)
        //.setDominanceComparator(new DominanceWithConstraintsComparator<>(new OverallConstraintViolationDegreeComparator<>()))
        .build();

    smpso.observable().register(new RunTimeChartObserver<>("SMPSO", 80, 1000, referenceParetoFront));

    smpso.run();

    List<DoubleSolution> population = smpso.result();
    JMetalLogger.logger.info("Total execution time : " + smpso.totalComputingTime() + "ms");
    JMetalLogger.logger.info("Number of evaluations: " + smpso.evaluation());

    new SolutionListOutput(population)
        .setVarFileOutputContext(new DefaultFileOutputContext("VAR.csv", ","))
        .setFunFileOutputContext(new DefaultFileOutputContext("FUN.csv", ","))
        .print();

    JMetalLogger.logger.info("Random seed: " + JMetalRandom.getInstance().getSeed());
    JMetalLogger.logger.info("Objectives values have been written to file FUN.csv");
    JMetalLogger.logger.info("Variables values have been written to file VAR.csv");

  }
}
