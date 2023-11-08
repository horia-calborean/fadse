package jmetal.algorithm.examples.singleobjective;

import static jmetal.core.util.AbstractAlgorithmRunner.printFinalSolutionSet;

import java.util.Comparator;
import java.util.List;
import jmetal.algorithm.singleobjective.localsearch.BasicLocalSearch;
import jmetal.core.operator.mutation.impl.PolynomialMutation;
import jmetal.problem.singleobjective.Sphere;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.comparator.ObjectiveComparator;

/**
 * Class to configure and run a single objective local search. The target problem is Sphere.
 *
 * @author Antonio J. Nebro
 */
public class LocalSearchContinuousProblemRunner {
  public static void main(String[] args)  {
    var problem = new Sphere(20) ;

    var mutationOperator =
        new PolynomialMutation(1.0 / problem.numberOfVariables(), 20.0) ;

    int improvementRounds = 500000 ;

    Comparator<DoubleSolution> comparator = new ObjectiveComparator<>(0) ;

    DoubleSolution initialSolution = problem.createSolution() ;
    problem.evaluate(initialSolution );

    BasicLocalSearch<DoubleSolution> localSearch = new BasicLocalSearch<>(initialSolution,
        improvementRounds,
        problem,
        mutationOperator,
        comparator) ;


    long startTime = System.currentTimeMillis() ;
    localSearch.run();
    long endTime = System.currentTimeMillis() ;

    DoubleSolution foundSolution = localSearch.result() ;

    String fitnessMessage = "Fitness: " + foundSolution.objectives()[0] ;
    JMetalLogger.logger.info(fitnessMessage) ;
    JMetalLogger.logger.info("Computing time: " + (endTime - startTime)) ;
    printFinalSolutionSet(List.of(foundSolution));
  }
}
