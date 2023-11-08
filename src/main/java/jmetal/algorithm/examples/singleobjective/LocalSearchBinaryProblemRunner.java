package jmetal.algorithm.examples.singleobjective;

import java.util.Comparator;
import jmetal.algorithm.singleobjective.localsearch.BasicLocalSearch;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.operator.mutation.impl.BitFlipMutation;
import jmetal.core.problem.binaryproblem.BinaryProblem;
import jmetal.problem.singleobjective.OneMax;
import jmetal.core.solution.binarysolution.BinarySolution;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.comparator.ObjectiveComparator;

/**
 * Class to configure and run a single objective local search. The target problem is OneMax.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class LocalSearchBinaryProblemRunner {
  public static void main(String[] args)  {
    BinaryProblem problem = new OneMax(512) ;

    MutationOperator<BinarySolution> mutationOperator =
        new BitFlipMutation(1.0 / problem.bitsFromVariable(0)) ;

    int improvementRounds = 5000 ;

    Comparator<BinarySolution> comparator = new ObjectiveComparator<>(0) ;

    BinarySolution initialSolution = problem.createSolution() ;
    problem.evaluate(initialSolution );

    BasicLocalSearch<BinarySolution> localSearch = new BasicLocalSearch<>(initialSolution,
        improvementRounds,
            problem,
            mutationOperator,
            comparator) ;

    localSearch.run();

    BinarySolution foundSolution = localSearch.result() ;

    String fitnessMessage = "Fitness: " + foundSolution.objectives()[0] ;
    JMetalLogger.logger.info(fitnessMessage) ;
    JMetalLogger.logger.info("Solution: " + foundSolution.variables().get(0)) ;
  }
}
