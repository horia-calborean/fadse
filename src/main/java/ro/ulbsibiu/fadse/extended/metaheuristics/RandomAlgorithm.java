/**
 * RandomAlgorithm.java
 * @author Horia Calborean
 * @version 1.0  
 */
package ro.ulbsibiu.fadse.extended.metaheuristics;

import java.util.Random;

import ro.ulbsibiu.fadse.extended.problems.simulators.ServerSimulator;
import jmetal.base.*;
import jmetal.util.*;

/**
 * This class implements a random algorithm (random selection, all the offspring make it to the next generation, crossover and mutation remains) .
 */
public class RandomAlgorithm extends Algorithm {

    /**
     * stores the problem  to solve
     */
    private Problem problem_;

    /**
     * Constructor
     * @param problem Problem to solve
     */
    public RandomAlgorithm(Problem problem) {
        this.problem_ = problem;
    } // NSGAII
    private SolutionSet population;

    /**
     * Runs the random algorithm (random selection, all the offspring make it to the next generation, crossover and mutation remains).
     * @return a <code>SolutionSet</code> that is a set of non dominated solutions
     * as a result of the algorithm execution
     * @throws JMException
     */
    public SolutionSet execute() throws JMException, ClassNotFoundException {
        int populationSize;
        int maxEvaluations;
        int evaluations;
        int requiredEvaluations; // Use in the example of use of the
        // indicators object (see below)


        SolutionSet offspringPopulation;

        Operator mutationOperator;
        Operator crossoverOperator;

        //Read the parameters
        populationSize = ((Integer) getInputParameter("populationSize")).intValue();
        maxEvaluations = ((Integer) getInputParameter("maxEvaluations")).intValue();

        //Initialize the variables
        population = new SolutionSet(populationSize);
        evaluations = 0;

        requiredEvaluations = 0;

        //Read the operators
        mutationOperator = operators_.get("mutation");
        crossoverOperator = operators_.get("crossover");
//        selectionOperator = operators_.get("selection");

        // Create the initial solutionSet
        Solution newSolution;
        for (int i = 0; i < populationSize; i++) {
            newSolution = new Solution(problem_);
            problem_.evaluate(newSolution);
            problem_.evaluateConstraints(newSolution);
            evaluations++;
            population.add(newSolution);
        } //for
        if (problem_ instanceof ServerSimulator) {
            ((ServerSimulator) problem_).join();//blocks until all  the offsprings are evaluated
        }
        // Generations ...
        Random r = new Random(System.currentTimeMillis());
        while (evaluations < maxEvaluations) {

            // Create the offSpring solutionSet
            offspringPopulation = new SolutionSet(populationSize);
            Solution[] parents = new Solution[2];
            for (int i = 0; i < (populationSize / 2); i++) {
                if (evaluations < maxEvaluations) {
                    //obtain parents
                    parents[0] = population.get(r.nextInt(populationSize));//random selection
                    parents[1] = population.get(r.nextInt(populationSize));
                    Solution[] offSpring = (Solution[]) crossoverOperator.execute(parents);
                    mutationOperator.execute(offSpring[0]);
                    mutationOperator.execute(offSpring[1]);
                    problem_.evaluate(offSpring[0]);
                    problem_.evaluateConstraints(offSpring[0]);
                    problem_.evaluate(offSpring[1]);
                    problem_.evaluateConstraints(offSpring[1]);
                    offspringPopulation.add(offSpring[0]);
                    offspringPopulation.add(offSpring[1]);
                    evaluations += 2;
                } // if
            } // for
            if (problem_ instanceof ServerSimulator) {
                ((ServerSimulator) problem_).join();//blocks until all  the offsprings are evaluated
            }
            population = offspringPopulation;
        } // while

        // Return as output parameter the required evaluations
        setOutputParameter("evaluations", requiredEvaluations);

        // Return the first non-dominated front
        Ranking ranking = new Ranking(population);
        return ranking.getSubfront(0);
    } // execute
} // NSGA-II

