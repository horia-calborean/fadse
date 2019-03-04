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

import org.hsqldb.Server;
import ro.ulbsibiu.fadse.environment.parameters.CheckpointFileParameter;
import ro.ulbsibiu.fadse.extended.base.operator.mutation.BitFlipMutationFuzzy;
import ro.ulbsibiu.fadse.extended.base.operator.mutation.BitFlipMutationFuzzyVirtualParameters;
import ro.ulbsibiu.fadse.extended.base.operator.mutation.BitFlipMutationRandomDefuzzifier;
import ro.ulbsibiu.fadse.extended.problems.simulators.ServerSimulator;

/**
 * This class implements the NSGA-II algorithm.
 */
public class NSGAII extends Algorithm {
    protected int populationSize;
    protected int maxEvaluations;
    protected int evaluations;
    protected String outputPath;
    protected String checkPointFile = "";
    protected boolean feasible = false;
    protected int feasiblePercentage;
    protected QualityIndicator indicators; // QualityIndicator object
    protected int requiredEvaluations; // Use in the example of use of the
    // indicators object (see below)

    //SolutionSet offspringPopulation;
    //SolutionSet union;
    //private SolutionSet population;

    protected Operator mutationOperator;
    protected Operator crossoverOperator;
    protected Operator selectionOperator;

    protected Distance distance = new Distance();
    protected boolean outputEveryPopulation = false;

    /**
     * stores the problem to solve
     */
    protected Problem problem_;

    /**
     * Constructor
     *
     * @param problem Problem to solve
     */
    public NSGAII(Problem problem) {
        this.problem_ = problem;
    } // NSGAII


    /**
     * Runs the NSGA-II algorithm.
     *
     * @return a <code>SolutionSet</code> that is a set of non dominated
     * solutions as a result of the algorithm execution
     * @throws JMException
     */
    public SolutionSet execute() throws JMException, ClassNotFoundException {

        SolutionSet population = InitializeEverything();

        //***********************************************MAIN ALGORITHM********************************************************
        // Generations ...
        while (evaluations < maxEvaluations) {
            SolutionSet offspringPopulation = GenerateOffsprings(population, feasiblePercentage);
            JoinAndOutputPopulation(offspringPopulation, "offspring");
            ReEvaluatePopulation(population);
            JoinAndOutputPopulation(population, "corrected");

            // Create the solutionSet union of solutionSet and offSpring
            SolutionSet union = ((SolutionSet) population).union(offspringPopulation);
            population = SelectNextGeneration(union, populationSize);
            DoIndicatorExtraStuff(population);

            DoEndRoundOutputs(population);

        } // while

        // Return as output parameter the required evaluations
        setOutputParameter("evaluations", requiredEvaluations);

        // Return the first non-dominated front
        Ranking ranking = new Ranking(population);

        return ranking.getSubfront(0);


    } // execute

    protected void DoEndRoundOutputs(SolutionSet population) {
        if(problem_ instanceof ServerSimulator){
            OutputPopulation(population, "filled");
            Ranking ranking_temp = new Ranking(population);
            OutputPopulation(ranking_temp.getSubfront(0), "pareto");
        } else {
            if (outputEveryPopulation) {
                population.printObjectivesToFile(outputPath + System.currentTimeMillis() + ".csv");
            }
        }
    }

    protected SolutionSet InitializeEverything() throws ClassNotFoundException, JMException {
        ReadParameters();
        ReadOperators();

        //Initialize the variables
        evaluations = 0;
        requiredEvaluations = 0;

        //***********************************************INITIAL POPULATION****************************************************
        SolutionSet population = CreateInitialPopulation();
        JoinAndOutputPopulation(population, "filled");
        ReEvaluatePopulation(population);
        JoinAndOutputPopulation(population, "corrected");

        Ranking ranking_temp = new Ranking(population);
        OutputPopulation(ranking_temp.getSubfront(0), "pareto");
        return population;
    }

    protected void DoIndicatorExtraStuff(SolutionSet population) {
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
    }

    protected SolutionSet SelectNextGeneration(SolutionSet union, int populationSize) {
        // Ranking the union
        Ranking ranking = new Ranking(union);
        int index = 0;
        int remain = populationSize;
        SolutionSet front = null;
        SolutionSet population = new SolutionSet(populationSize);
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

        return population;
    }

    protected void ReadParameters() {
        //Read the parameters
        populationSize = ((Integer) getInputParameter("populationSize")).intValue();
        maxEvaluations = ((Integer) getInputParameter("maxEvaluations")).intValue();
        indicators = (QualityIndicator) getInputParameter("indicators");

        Object output = getInputParameter("outputEveryPopulation");
        if (output != null) {
            outputEveryPopulation = (Boolean) output;
        }

        CheckpointFileParameter fileParam = (CheckpointFileParameter) getInputParameter("checkpointFile");

        if (fileParam != null) {
            checkPointFile = fileParam.GetCheckpointFile();
        }
        String feasibleString = (String) getInputParameter("forceFeasibleFirstGeneration");

        feasible = feasibleString != null && feasibleString.equals("true");
        feasiblePercentage = Integer
                .parseInt((String) getInputParameter("forceMinimumPercentageFeasibleIndividuals"));

        outputPath = (String) getInputParameter("outputPath");
    }

    protected void ReEvaluatePopulation(SolutionSet population) throws JMException {
        //WORKAROUND
        System.out.println("RESEND");
        for (int i = 0; i < populationSize; i++) {
            Solution s = population.get(i);
            problem_.evaluate(s);
        }
    }

    protected void JoinAndOutputPopulation(SolutionSet population, String populationName) {
        //Added by HORIA
        if (problem_ instanceof ServerSimulator) {
            ((ServerSimulator) problem_).join();//blocks until all  the offsprings are evaluated
            OutputPopulation(population, populationName);
        }
    }

    protected void OutputPopulation(SolutionSet population, String populationName) {
        if (problem_ instanceof ServerSimulator) {
            ((ServerSimulator) problem_).dumpCurrentPopulation(populationName + System.currentTimeMillis(), population);
        }
    }

    protected SolutionSet GenerateOffsprings(SolutionSet population, int feasiblePercentage) throws JMException {
        // Create the offSpring solutionSet
        SolutionSet offspringPopulation = new SolutionSet(populationSize);
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
        return offspringPopulation;
    }

    protected SolutionSet CreateInitialPopulation() throws ClassNotFoundException, JMException {
        SolutionSet population = new SolutionSet(populationSize);
        // Create the initial solutionSet
        Solution newSolution;
        //Added by HORIA
        String file = checkPointFile;
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
                    if (feasible) {//this will skip ind only if they are infeasible because of constrains
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
                if (feasible) {//this will skip ind only if they are infeasible because of constrains
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
        return population;
    }

    protected void ReadOperators() {
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
    }
} // NSGA-II

