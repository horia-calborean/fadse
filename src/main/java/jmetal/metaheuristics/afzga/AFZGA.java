package jmetal.metaheuristics.afzga;

import jmetal.base.Problem;
import jmetal.base.Solution;
import jmetal.base.SolutionSet;
import jmetal.metaheuristics.nsgaII.NSGAII;
import jmetal.util.*;
import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.extended.base.operator.mutation.BitFlipMutationFuzzy;
import ro.ulbsibiu.fadse.extended.base.operator.mutation.BitFlipMutationFuzzyVirtualParameters;
import ro.ulbsibiu.fadse.extended.base.operator.mutation.BitFlipMutationRandomDefuzzifier;
import ro.ulbsibiu.fadse.extended.problems.simulators.ServerSimulator;
import ro.ulbsibiu.fadse.utils.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AFZGA extends NSGAII {
    /**
     * Constructor
     *
     * @param problem Problem to solve
     */
    public AFZGA(Problem problem) {
        super(problem);
    }

    private int nrZones = 4;
    private int supportVectorsPerFront = 5;
    private SolutionSet bestSupportVectors;

    @Override
    public SolutionSet execute() throws JMException, ClassNotFoundException {

        SolutionSet population = InitializeEverything();
        population = CleanPopulation(population, null);
        OutputAndReevaluatePopulation(population);
        //***********************************************MAIN ALGORITHM********************************************************
        // Generations ...
        while (evaluations < maxEvaluations) {
            Logger.getLogger(NSGAII.class.getName()).log(Level.INFO, "Entered NextGeneration with evaluations: " + evaluations);
            SolutionSet offspringPopulation = GenerateOffsprings(population, feasiblePercentage);
            offspringPopulation = CleanPopulation(offspringPopulation, population);

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


    @Override
    protected SolutionSet SelectNextGeneration(SolutionSet union, int populationSize) {
        //Distance distance = new Distance();
        //AfMembership afMembership = new AfMembership();
        ApparentFront af = new ApparentFront(11);

        //int minSupportVectorNumber = union.get(0).numberOfObjectives() + 1;
        int minSupportVectorNumber = 3;
        int defaultSupportVectorNumber = 15;
        //SolutionSet supportVectors = new SolutionSet(minVectors);
        if (bestSupportVectors == null) {
            bestSupportVectors = GenerateInitialSupportVectors(union, defaultSupportVectorNumber);
        }

        ApparentFrontHelper.FitTheFront(af, bestSupportVectors);

        dumpCurrentFront("coefficients_" + System.currentTimeMillis(), af);

        ApparentFrontRanking ranking = new ApparentFrontRanking(af, union, nrZones);

        int remain = populationSize;
        int index = 0;
        SolutionSet front = null;
        SolutionSet population = new SolutionSet(populationSize);
        population.clear();

        for (int i = 0; i < nrZones; i++) {
            OutputPopulation(ranking.getSubfront(i), "afz" + (i + 1) + "_");
        }


        front = ranking.getSubfront(index);
        while ((remain > 0) && (remain >= front.size())) {
            //Assign crowding distance to individuals
            distance.crowdingDistanceAssignment(front, problem_.getNumberOfObjectives());
            //Add the individuals of this front
            for (int k = 0; k < front.size(); k++) {
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
            for (int k = 0; k < remain; k++) {
                population.add(front.get(k));
            } // for
            remain = 0;
        } // if

        selectNextSupportVectors(minSupportVectorNumber, ranking, population);

        OutputPopulation(bestSupportVectors, "supportVectors");

        return population;
    }

    private void selectNextSupportVectors(int minSupportVectorNumber, ApparentFrontRanking ranking, SolutionSet population) {
        bestSupportVectors.clear();
//        for (int i = 0; i < minSupportVectorNumber; i++) {
//                bestSupportVectors.add(population.get(i));
//        }


        for (int i = 0; i < 3; i++) {
            SolutionSet subFront = ranking.getSubfront(i);
            int maxSolutionsFromFront = Math.min(subFront.size(), supportVectorsPerFront);
            for (int j = 0; j < maxSolutionsFromFront; j++) {
                bestSupportVectors.add(subFront.get(j));
            }
        }

        if (bestSupportVectors.size() < minSupportVectorNumber) {
            int i = -1;
            do {
                i++;
                Solution currentSolution = population.get(i);
                if (bestSupportVectors.deepContains(currentSolution)) {
                    continue;
                }
                bestSupportVectors.add(population.get(i));
            } while (bestSupportVectors.size() > 2);
        }
    }

    private SolutionSet GenerateInitialSupportVectors(SolutionSet union, int minNumber) {
        bestSupportVectors = new SolutionSet(minNumber);
        SolutionSet temp = new SolutionSet(union.size());
        for (int i = 0; i < union.size(); i++) {
            temp.add(new Solution(union.get(i)));
        }

        distance.crowdingDistanceAssignment(temp, problem_.getNumberOfObjectives());
        temp.sort(new jmetal.base.operator.comparator.CrowdingComparator());
        for (int k = 0; k < minNumber; k++) {
            bestSupportVectors.add(temp.get(k));
        } // for
        return bestSupportVectors;
    }

    private boolean hasFeasibleHC(Solution solution) {
        double HC = solution.getObjective(1);
        if (HC <= 5000) {
            return true;
        }

        return false;
    }

    private SolutionSet CleanPopulation(SolutionSet population, SolutionSet parentPopulation) throws JMException, ClassNotFoundException {
        SolutionSet cleanPopulation = new SolutionSet(populationSize);
        do {
            if (problem_ instanceof ServerSimulator) {
                ((ServerSimulator) problem_).join();//blocks until all  the offsprings are evaluated
            }

            cleanPopulation = RemoveInfeasibleIndividuals(population);
            population = RefillPopulation(cleanPopulation, feasiblePercentage, parentPopulation);

        } while (cleanPopulation.size() < populationSize);

        return cleanPopulation;
    }

    private SolutionSet RemoveInfeasibleIndividuals(SolutionSet population) {
        SolutionSet cleanPopulation = new SolutionSet(populationSize);

        for (int i = 0; i < population.size(); i++) {
            if (hasFeasibleHC(population.get(i))) {
                cleanPopulation.add(population.get(i));
            }
        }

        return cleanPopulation;
    }

    private SolutionSet RefillPopulation(SolutionSet currentPopulation, int feasiblePercentage, SolutionSet parentPopulation) throws JMException, ClassNotFoundException {
        Logger.getLogger(AFZGA.class.getName()).log(Level.INFO, "Entered RefillPopulation with size: " + currentPopulation.size());

        SolutionSet newPopulation = new SolutionSet(populationSize);
        if (parentPopulation == null) {
            int i = 0;
            Solution newSolution;
            while (i < populationSize - currentPopulation.size()) {
                Logger.getLogger(NSGAII.class.getName()).log(Level.INFO, "#############################While RefillPopulation with currentSize: " + newPopulation.size());
                newSolution = new Solution(problem_);
                if (mutationOperator instanceof BitFlipMutationFuzzy
                        || mutationOperator instanceof BitFlipMutationRandomDefuzzifier
                        || mutationOperator instanceof BitFlipMutationFuzzyVirtualParameters) {
                    mutationOperator.execute(newSolution);
                }
                problem_.evaluate(newSolution);
                problem_.evaluateConstraints(newSolution);
                if (feasible) {
                    if (newSolution.getNumberOfViolatedConstraint() > 0) {
                        if (mutationOperator instanceof BitFlipMutationFuzzy) {
                            ((BitFlipMutationFuzzy) mutationOperator).x--;
                        }
                        continue;
                    }
                }
                evaluations++;
                newPopulation.add(newSolution);
                i++;
            }
        } else {
            Solution[] parents = new Solution[2];
            int nrOfFeasible = 0;

            while (newPopulation.size() < populationSize - currentPopulation.size()) {
                Logger.getLogger(NSGAII.class.getName()).log(Level.INFO, "#############################While RefillPopulation with currentSize: " + newPopulation.size());
                if (evaluations < maxEvaluations) {
                    //obtain parents
                    parents[0] = (Solution) selectionOperator.execute(parentPopulation);
                    parents[1] = (Solution) selectionOperator.execute(parentPopulation);
                    Solution[] offSpring = (Solution[]) crossoverOperator.execute(parents);
                    for (int i = 0; i < offSpring.length; i++) {
                        Solution offs = offSpring[i];
                        mutationOperator.execute(offs);
                        problem_.evaluate(offs);
                        problem_.evaluateConstraints(offs);

                        System.out.println("[0] " + offs.getNumberOfViolatedConstraint() + " " + nrOfFeasible);
                        if (offs.getNumberOfViolatedConstraint() > 0 && (((nrOfFeasible + 0.0) / populationSize) * 100) < feasiblePercentage) {
                            if (mutationOperator instanceof BitFlipMutationFuzzy) {
                                ((BitFlipMutationFuzzy) mutationOperator).x--;
                            }
                        } else {
                            if (newPopulation.size() < populationSize - currentPopulation.size()) {
                                if (!parentPopulation.deepContains(offs) && !newPopulation.deepContains(offs)) {
                                    Logger.getLogger(NSGAII.class.getName()).log(Level.INFO, "While RefillPopulation Added offsprings " + i);
                                    newPopulation.add(offs);
                                    evaluations += 1;
                                    nrOfFeasible++;
                                }
                            }
                        }
                    }

                }
            }
        }

        Logger.getLogger(NSGAII.class.getName()).log(Level.INFO, "Leaving RefillPopulation with a new populationsSize of: " + newPopulation.size());
        return newPopulation.union(currentPopulation);
    }

    private void dumpCurrentFront(String filename, ApparentFront af) {
        if (problem_ instanceof ServerSimulator) {
            Environment environment = ((ServerSimulator) problem_).getEnvironment();

            String result = (new Utils()).generateCSVHeaderForApparentFront(af);
            result += (new Utils()).generateCSVForApparentFront(af);

            try {
                (new File(environment.getResultsFolder())).mkdirs();
                BufferedWriter out = new BufferedWriter(new FileWriter(environment.getResultsFolder() + System.getProperty("file.separator") + filename + ".csv"));
                out.write(result);
                out.close();
            } catch (IOException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
