/**
 * NSGAII.java
 *
 * @author Juan J. Durillo
 * @version 1.0
 */
package jmetal.metaheuristics.nsgaII;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import jmetal.base.*;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.*;

import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import ro.ulbsibiu.fadse.environment.parameters.CheckpointFileParameter;
import ro.ulbsibiu.fadse.extended.base.operator.mutation.BitFlipMutationFuzzy;
import ro.ulbsibiu.fadse.extended.base.operator.mutation.BitFlipMutationFuzzyVirtualParameters;
import ro.ulbsibiu.fadse.extended.base.operator.mutation.BitFlipMutationRandomDefuzzifier;
import ro.ulbsibiu.fadse.extended.problems.simulators.ServerSimulator;

/**
 * This class implements the NSGA-II algorithm.
 */
public class NSGAII extends Algorithm {

    /**
     * stores the problem to solve
     */
    private Problem problem_;

    /**
     * Constructor
     *
     * @param problem Problem to solve
     */
    public NSGAII(Problem problem) {
        this.problem_ = problem;
    } // NSGAII
    private SolutionSet population;

    /**
     * Runs the NSGA-II algorithm.
     *
     * @return a <code>SolutionSet</code> that is a set of non dominated
     * solutions as a result of the algorithm execution
     * @throws JMException
     */
    public SolutionSet execute() throws JMException, ClassNotFoundException {
        int populationSize;
        int maxEvaluations;
        int evaluations;

        QualityIndicator indicators; // QualityIndicator object
        int requiredEvaluations; // Use in the example of use of the
        // indicators object (see below)


        SolutionSet offspringPopulation;
        SolutionSet union;

        Operator mutationOperator;
        Operator crossoverOperator;
        Operator selectionOperator;

        Distance distance = new Distance();

        //Read the parameters
        populationSize = ((Integer) getInputParameter("populationSize")).intValue();
        maxEvaluations = ((Integer) getInputParameter("maxEvaluations")).intValue();
        indicators = (QualityIndicator) getInputParameter("indicators");

        boolean outputEveryPopulation = false;
        Object output = getInputParameter("outputEveryPopulation");
        if (output != null) {
            outputEveryPopulation = (Boolean) output;
        }
        String outputPath = (String) getInputParameter("outputPath");

        //Initialize the variables
        population = new SolutionSet(populationSize);
        evaluations = 0;

        requiredEvaluations = 0;

        //Read the operators
        mutationOperator = operators_.get("mutation");
        if (mutationOperator instanceof BitFlipMutationFuzzyVirtualParameters) {
            try {
                ((BitFlipMutationFuzzyVirtualParameters) mutationOperator).x = ((Integer) getInputParameter("initialGeneration")).intValue();
                System.out.println("Initial generation is: "+ ((BitFlipMutationFuzzyVirtualParameters) mutationOperator).x);
            } catch (Exception e) {
                System.out.println("NSGA-II: initial generations start was not set caused by: "+e.getMessage());
            }

        }
        crossoverOperator = operators_.get("crossover");
        selectionOperator = operators_.get("selection");
//***********************************************INITIAL POPULATION****************************************************
        // Create the initial solutionSet
        Solution newSolution;
        //Added by HORIA
        CheckpointFileParameter fileParam = (CheckpointFileParameter) getInputParameter("checkpointFile");
        String file = "";
        if (fileParam != null) {
            file = fileParam.GetCheckpointFile();
        }
        String feasible = (String) getInputParameter("forceFeasibleFirstGeneration");
        int feasiblePercentage = Integer
                .parseInt((String) getInputParameter("forceMinimumPercentageFeasibleIndividuals"));
        if (file != null && !file.equals("")) {
            Logger.getLogger(NSGAII.class.getName()).log(Level.WARNING, "Using a checkpoint file: " + file);
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
                    population.add(newSolution);
                    i++;
                } //while
                if (i < populationSize) {
                    throw new IOException("Checkpoint file does not have enough elements to fill the entire population");
                }
            } catch (IOException ex) {
                Logger.getLogger(NSGAII.class.getName()).log(Level.SEVERE, "Checkpoint file does not have enough elements to fill the entire population [" + i + "<" + populationSize + "]. Filling it with random individuals");
                while (i < populationSize) {
                    newSolution = new Solution(problem_);
                    if (mutationOperator instanceof BitFlipMutationFuzzy
                            || mutationOperator instanceof BitFlipMutationRandomDefuzzifier
                            || mutationOperator instanceof BitFlipMutationFuzzyVirtualParameters) {
                        mutationOperator.execute(newSolution);
                    }
                    problem_.evaluate(newSolution);
                    problem_.evaluateConstraints(newSolution);
                    if (feasible != null && feasible.equals("true")) {//this will skip ind only if they are infeasible because of constrains
                        if (newSolution.getNumberOfViolatedConstraint() > 0) {
                            if (mutationOperator instanceof BitFlipMutationFuzzy) {
                                ((BitFlipMutationFuzzy) mutationOperator).x--;
                            }
                            continue;
                        }
                    }
                    evaluations++;
                    population.add(newSolution);
                    i++;
                }
            }
        } else {
            int i = 0;
            while (i < populationSize) {
                newSolution = new Solution(problem_);
                if (mutationOperator instanceof BitFlipMutationFuzzy
                        || mutationOperator instanceof BitFlipMutationRandomDefuzzifier
                        || mutationOperator instanceof BitFlipMutationFuzzyVirtualParameters) {
                    mutationOperator.execute(newSolution);
                }
                problem_.evaluate(newSolution);
                problem_.evaluateConstraints(newSolution);
                if (feasible != null && feasible.equals("true")) {//this will skip ind only if they are infeasible because of constrains
                    if (newSolution.getNumberOfViolatedConstraint() > 0) {
                        if (mutationOperator instanceof BitFlipMutationFuzzy) {
                            ((BitFlipMutationFuzzy) mutationOperator).x--;
                        }
                        continue;
                    }
                }
                evaluations++;
                population.add(newSolution);
                i++;
            } //for
            //Added by HORIA
        }
        if (problem_ instanceof ServerSimulator) {
            ((ServerSimulator) problem_).join();//blocks until all  the offsprings are evaluated
            ((ServerSimulator) problem_).dumpCurrentPopulation(population);
        }
        //WORKAROUND
        for (int i = 0; i < populationSize; i++) {
            Solution s = population.get(i);
            problem_.evaluate(s);
        }
        if (problem_ instanceof ServerSimulator) {
            ((ServerSimulator) problem_).join();//blocks until all  the offsprings are evaluated
            ((ServerSimulator) problem_).dumpCurrentPopulation("corrected" + System.currentTimeMillis(), population);
            Ranking ranking_temp = new Ranking(population);
            ((ServerSimulator) problem_).dumpCurrentPopulation("pareto" + System.currentTimeMillis(), ranking_temp.getSubfront(0));
        }
        //END WORKAROUND
        //END added by Horia
//***********************************************MAIN ALGORITHM********************************************************
        // Generations ...
        while (evaluations < maxEvaluations) {
            // Create the offSpring solutionSet
            offspringPopulation = new SolutionSet(populationSize);
            Solution[] parents = new Solution[2];
            int nrOfFeasible = 0;
//            for (int i = 0; i < (populationSize / 2); i++) {
            while (offspringPopulation.size() < populationSize) {
                if (evaluations < maxEvaluations) {
                    //obtain parents
                    parents[0] = (Solution) selectionOperator.execute(population);
                    parents[1] = (Solution) selectionOperator.execute(population);
                    Solution[] offSpring = (Solution[]) crossoverOperator.execute(parents);
                    mutationOperator.execute(offSpring[0]);
                    mutationOperator.execute(offSpring[1]);
                    problem_.evaluate(offSpring[0]);
                    problem_.evaluateConstraints(offSpring[0]);
//                    System.out.println("[0] " + offSpring[0].getNumberOfViolatedConstraint() + " " + nrOfFeasible);
                    if (offSpring[0].getNumberOfViolatedConstraint() > 0 && (((nrOfFeasible + 0.0) / populationSize) * 100) < feasiblePercentage) {
                        //infeasible and we still need feasible individuals in the population
//                        System.out.println("[0] INFESIBLE nr of feasible " + nrOfFeasible + " needed " + feasiblePercentage + "violated " + offSpring[0].getNumberOfViolatedConstraint());
                        if (mutationOperator instanceof BitFlipMutationFuzzy) {
                            ((BitFlipMutationFuzzy) mutationOperator).x--;
                        }
                    } else {

                        offspringPopulation.add(offSpring[0]);
                        evaluations += 1;
                        nrOfFeasible++;
                    }
                    problem_.evaluate(offSpring[1]);
                    problem_.evaluateConstraints(offSpring[1]);
//                    System.out.println("[1] " + offSpring[1].getNumberOfViolatedConstraint() + " " + nrOfFeasible);
                    if (offSpring[1].getNumberOfViolatedConstraint() > 0 && (((nrOfFeasible + 0.0) / populationSize) * 100) < feasiblePercentage) {
                        //infeasible and we still need feasible individuals in the population
//                        System.out.println("[1] INFESIBLE nr of feasible " + nrOfFeasible + " needed " + feasiblePercentage + "violated " + offSpring[1].getNumberOfViolatedConstraint());
                        if (mutationOperator instanceof BitFlipMutationFuzzy) {
                            ((BitFlipMutationFuzzy) mutationOperator).x--;
                        }
                    } else {
                        if (offspringPopulation.size() < populationSize) {
                            offspringPopulation.add(offSpring[1]);
                            evaluations += 1;
                            nrOfFeasible++;
                        }
                    }


                } // if
            } // for
            //Added by HORIA
            if (problem_ instanceof ServerSimulator) {
                ((ServerSimulator) problem_).join();//blocks until all  the offsprings are evaluated
                ((ServerSimulator) problem_).dumpCurrentPopulation("offspring" + System.currentTimeMillis(), offspringPopulation);
            }
            //WORKAROUND
            System.out.println("RESEND");
            for (int i = 0; i < populationSize; i++) {
                Solution s = population.get(i);
                problem_.evaluate(s);
            }
            if (problem_ instanceof ServerSimulator) {
                ((ServerSimulator) problem_).join();//blocks until all  the offsprings are evaluated
                ((ServerSimulator) problem_).dumpCurrentPopulation("corrected" + System.currentTimeMillis(), population);
            }
            //END WORKAROUND
            //END added by Horia
            // Create the solutionSet union of solutionSet and offSpring
            union = ((SolutionSet) population).union(offspringPopulation);
            // Ranking the union
            Ranking ranking = new Ranking(union);
            int remain = populationSize;
            int index = 0;
            SolutionSet front = null;
            population.clear();
            // Obtain the next front
            front = ranking.getSubfront(index);
            while ((remain > 0) && (remain >= front.size())) {
                //Assign crowding distance to individuals
                distance.crowdingDistanceAssignment(front, problem_.getNumberOfObjectives());
                //Add the individuals of this front
                for (int k = 0; k
                        < front.size(); k++) {
                    population.add(front.get(k));
                } // for
                //Decrement remain
                remain = remain - front.size();
                //Obtain the next front
                index++;
                if (remain > 0) {
                    front = ranking.getSubfront(index);
                } // if
            } // while
            // Remain is less than front(index).size, insert only the best one
            if (remain > 0) {  // front contains individuals to insert
                distance.crowdingDistanceAssignment(front, problem_.getNumberOfObjectives());
                front.sort(new jmetal.base.operator.comparator.CrowdingComparator());
                for (int k = 0; k
                        < remain; k++) {
                    population.add(front.get(k));
                } // for
                remain = 0;
            } // if
            // This piece of code shows how to use the indicator object into the code
            // of NSGA-II. In particular, it finds the number of evaluations required
            // by the algorithm to obtain a Pareto front with a hypervolume higher
            // than the hypervolume of the true Pareto front.
            if ((indicators != null)
                    && (requiredEvaluations == 0)) {
                double HV = indicators.getHypervolume(population);
                if (HV >= (0.98 * indicators.getTrueParetoFrontHypervolume())) {
                    requiredEvaluations = evaluations;
                } // if
            } // if
            if (problem_ instanceof ServerSimulator) {
                ((ServerSimulator) problem_).dumpCurrentPopulation(population);
                Ranking ranking_temp = new Ranking(population);
                ((ServerSimulator) problem_).dumpCurrentPopulation("pareto" + System.currentTimeMillis(), ranking_temp.getSubfront(0));
            } else {
                if (outputEveryPopulation) {
                    population.printObjectivesToFile(outputPath + System.currentTimeMillis() + ".csv");
                }
            }



        } // while

        // Return as output parameter the required evaluations
        setOutputParameter("evaluations", requiredEvaluations);

        // Return the first non-dominated front
        Ranking ranking = new Ranking(population);


        return ranking.getSubfront(0);


    } // execute
} // NSGA-II

