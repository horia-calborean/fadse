/**
 * SPEA2.java
 * @author Juan J. Durillo
 * @version 1.0
 */
package jmetal.metaheuristics.spea2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import jmetal.base.*;

import java.util.Comparator;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import ro.ulbsibiu.fadse.environment.parameters.CheckpointFileParameter;
import ro.ulbsibiu.fadse.extended.problems.simulators.ServerSimulator;
import jmetal.util.*;

/** 
 * This class representing the SPEA2 algorithm
 */
public class SPEA2 extends Algorithm {

    /**
     * Defines the number of tournaments for creating the mating pool
     */
    public static final int TOURNAMENTS_ROUNDS = 1;
    /**
     * Stores the problem to solve
     */
    private Problem problem_;

    /**
     * Constructor.
     * Create a new SPEA2 instance
     * @param problem Problem to solve
     */
    public SPEA2(Problem problem) {
        this.problem_ = problem;
    } // Spea2

    /**
     * Runs of the Spea2 algorithm.
     * @return a <code>SolutionSet</code> that is a set of non dominated solutions
     * as a result of the algorithm execution
     * @throws JMException
     */
    public SolutionSet execute() throws JMException, ClassNotFoundException {
        int populationSize, archiveSize, maxEvaluations, evaluations;
        Operator crossoverOperator, mutationOperator, selectionOperator;
        SolutionSet solutionSet, archive, offSpringSolutionSet;

        //Read the params
        populationSize = ((Integer) getInputParameter("populationSize")).intValue();
        archiveSize = ((Integer) getInputParameter("archiveSize")).intValue();
        maxEvaluations = ((Integer) getInputParameter("maxEvaluations")).intValue();

        //Read the operators
        crossoverOperator = operators_.get("crossover");
        mutationOperator = operators_.get("mutation");
        selectionOperator = operators_.get("selection");

        boolean outputEveryPopulation = false;
        Object output = getInputParameter("outputEveryPopulation");
        if (output != null) {
                outputEveryPopulation = (Boolean) output;
        }
        String outputPath = (String) getInputParameter("outputPath");
        
        //Initialize the variables
        solutionSet = new SolutionSet(populationSize);
        archive = new SolutionSet(archiveSize);
        evaluations = 0;

        //-> Create the initial solutionSet
        Solution newSolution;
        //Added by HORIA
        CheckpointFileParameter fileParam = (CheckpointFileParameter) getInputParameter("checkpointFile");
        String file ="";
        if(fileParam !=null){
            file = fileParam.GetCheckpointFile();
        }
        if (file != null && !file.equals("")) {
            Logger.getLogger(SPEA2.class.getName()).log(Level.WARNING, "Using a checkpoint file: " + file);
            int i = 0;
            try {
                BufferedReader input = new BufferedReader(new FileReader(file));

                String line = null; //not declared within while loop
                line = input.readLine();//skip the headder
                while ((line = input.readLine()) != null && i < populationSize) {
                    newSolution = new Solution(problem_);

                    StringTokenizer tokenizer = new StringTokenizer(line, ",");
                    for (int j = 0; j < problem_.getNumberOfVariables(); j++) {
                        newSolution.getDecisionVariables()[j].setValue(Double.valueOf(tokenizer.nextToken()));
                    }
                    problem_.evaluate(newSolution);
                    problem_.evaluateConstraints(newSolution);
                    evaluations++;
                    solutionSet.add(newSolution);
                    i++;
                } //while
            } catch (IOException ex) {
                Logger.getLogger(SPEA2.class.getName()).log(Level.SEVERE, "Checkpoint file does not have enough elements to fill the entire population [" + i + "<" + populationSize + "]. Filling it with random individuals");
                while (i < populationSize) {
                    newSolution = new Solution(problem_);
                    problem_.evaluate(newSolution);
                    problem_.evaluateConstraints(newSolution);
                    evaluations++;
                    solutionSet.add(newSolution);
                    i++;
                }
            }
        } else {
            //END added by Horia
            for (int i = 0; i < populationSize; i++) {
                newSolution = new Solution(problem_);
                problem_.evaluate(newSolution);
                problem_.evaluateConstraints(newSolution);
                evaluations++;
                solutionSet.add(newSolution);
            } //for
            //Added by HORIA
        }
        if (problem_ instanceof ServerSimulator) {
            ((ServerSimulator) problem_).join();//blocks until all  the offsprings are evaluated
        }
        //END added by Horia

        while (evaluations < maxEvaluations) {
            SolutionSet union = ((SolutionSet) solutionSet).union(archive);            
            Spea2Fitness spea = new Spea2Fitness(union);
            spea.fitnessAssign();
            archive = spea.environmentalSelection(archiveSize);
            if (problem_ instanceof ServerSimulator) {
                ((ServerSimulator) problem_).dumpCurrentPopulation(archive);
            }
            // Create a new offspringPopulation
            offSpringSolutionSet = new SolutionSet(populationSize);
            Solution[] parents = new Solution[2];
            while (offSpringSolutionSet.size() < populationSize) {
                int j = 0;
                do {
                    j++;
                    parents[0] = (Solution) selectionOperator.execute(archive);
                } while (j < SPEA2.TOURNAMENTS_ROUNDS); // do-while
                int k = 0;
                do {
                    k++;
                    parents[1] = (Solution) selectionOperator.execute(archive);
                } while (k < SPEA2.TOURNAMENTS_ROUNDS); // do-while

                //make the crossover
                Solution[] offSpring = (Solution[]) crossoverOperator.execute(parents);
                mutationOperator.execute(offSpring[0]);
                problem_.evaluate(offSpring[0]);
                problem_.evaluateConstraints(offSpring[0]);
                offSpringSolutionSet.add(offSpring[0]);
                evaluations++;
            } // while
            //Added by HORIA
            if (problem_ instanceof ServerSimulator) {
                ((ServerSimulator) problem_).join();//blocks until all  the offsprings are evaluated
                ((ServerSimulator) problem_).dumpCurrentPopulation("offspring"+System.currentTimeMillis(),offSpringSolutionSet);
            }
            if (outputEveryPopulation) {
					offSpringSolutionSet.printObjectivesToFile(outputPath
							+ System.currentTimeMillis()+".csv");
				}
            //TODO save archive and solution set to a file
            //END added by Horia
            // End Create a offSpring solutionSet
            solutionSet = offSpringSolutionSet;
        } // while

        Ranking ranking = new Ranking(archive);
        return ranking.getSubfront(0);
    } // execute
} // Spea2

