package jmetal.component.examples.singleobjective.randomsearch;

import java.util.List;
import jmetal.component.algorithm.RandomSearchAlgorithm;
import jmetal.component.catalogue.common.evaluation.Evaluation;
import jmetal.component.catalogue.common.evaluation.impl.SequentialEvaluation;
import jmetal.component.catalogue.common.solutionscreation.SolutionsCreation;
import jmetal.component.catalogue.common.solutionscreation.impl.RandomSolutionsCreation;
import jmetal.component.catalogue.common.termination.Termination;
import jmetal.component.catalogue.common.termination.impl.TerminationByEvaluations;
import jmetal.core.problem.binaryproblem.BinaryProblem;
import jmetal.problem.singleobjective.OneMax;
import jmetal.core.solution.binarysolution.BinarySolution;
import jmetal.core.util.AbstractAlgorithmRunner;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.errorchecking.JMetalException;
import jmetal.core.util.fileoutput.SolutionListOutput;
import jmetal.core.util.fileoutput.impl.DefaultFileOutputContext;
import jmetal.core.util.observer.impl.EvaluationObserver;
import jmetal.core.util.pseudorandom.JMetalRandom;

/**
 * Class to configure and run the a random search.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class RandomSearchSingleObjectiveBinaryEncodingExample extends AbstractAlgorithmRunner {
  public static void main(String[] args) throws JMetalException {
    BinaryProblem problem = new OneMax(512) ;
    Termination termination = new TerminationByEvaluations(50000);
    Evaluation<BinarySolution> evaluation = new SequentialEvaluation<>(problem) ;
    SolutionsCreation<BinarySolution> solutionsCreation = new RandomSolutionsCreation<>(problem, 1) ;

    var algorithm = new RandomSearchAlgorithm<>(
          "Random Search", solutionsCreation, evaluation,
                    termination);

    EvaluationObserver evaluationObserver = new EvaluationObserver(1000) ;
    algorithm.getObservable().register(evaluationObserver);

    algorithm.run();

    List<BinarySolution> population = algorithm.result();
    JMetalLogger.logger.info("Total execution time : " + algorithm.totalComputingTime() + "ms");
    JMetalLogger.logger.info("Number of evaluations: " + algorithm.evaluations());

    new SolutionListOutput(population)
            .setVarFileOutputContext(new DefaultFileOutputContext("VAR.csv", ","))
            .setFunFileOutputContext(new DefaultFileOutputContext("FUN.csv", ","))
            .print();

    JMetalLogger.logger.info("Random seed: " + JMetalRandom.getInstance().getSeed());
    JMetalLogger.logger.info("Objectives values have been written to file FUN.csv");
    JMetalLogger.logger.info("Variables values have been written to file VAR.csv");

    JMetalLogger.logger.info("Best found solution: " + population.get(0).objectives()[0]) ;
  }
}
