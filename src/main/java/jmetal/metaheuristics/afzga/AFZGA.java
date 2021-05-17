package jmetal.metaheuristics.afzga;

import jmetal.base.Problem;
import jmetal.base.Solution;
import jmetal.base.SolutionSet;
import jmetal.metaheuristics.nsgaII.NSGAII;
import jmetal.util.*;
import ro.ulbsibiu.fadse.extended.base.operator.mutation.BitFlipMutationFuzzy;
import ro.ulbsibiu.fadse.extended.base.operator.mutation.BitFlipMutationFuzzyVirtualParameters;
import ro.ulbsibiu.fadse.extended.base.operator.mutation.BitFlipMutationRandomDefuzzifier;
import ro.ulbsibiu.fadse.extended.problems.simulators.ServerSimulator;

import java.nio.file.Paths;
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
    private int nrZonesUsedForSupportVectors = 3;
    private int supportVectorsPerFront = 2;
    private int supportVectorsPerAxis = 5;
    private int defaultSupportVectorsNumber =  nrZonesUsedForSupportVectors * supportVectorsPerFront + 2 * supportVectorsPerAxis;
    private SolutionSet bestSupportVectors;
    private String resultsFolder;

    @Override
    public SolutionSet execute() throws JMException, ClassNotFoundException {

        SolutionSet population = InitializeEverything();

        resultsFolder = "results" + System.currentTimeMillis();
        super.outputPath = Paths.get(super.outputPath, resultsFolder).toAbsolutePath().toString();

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
        double reductionRate = 0.1;
        //Distance distance = new Distance();
        //AfMembership afMembership = new AfMembership();
        ApparentFront af = new ApparentFront(11, outputPath);

        //int minSupportVectorNumber = union.get(0).numberOfObjectives() + 1;
        int minSupportVectorNumber = 3;
        //SolutionSet supportVectors = new SolutionSet(minVectors);
        if (bestSupportVectors == null) {
            bestSupportVectors = GenerateInitialSupportVectors(union, defaultSupportVectorsNumber);
        }

        ApparentFrontHelper.FitTheFront(af, bestSupportVectors);

        af.dumpCurrentFront(problem_, "coefficients_" + System.currentTimeMillis());

        ApparentFrontRanking ranking = new ApparentFrontRanking(af, union, nrZones);

        int remain = populationSize;
        int index = 0;
        SolutionSet front = null;
        SolutionSet population = new SolutionSet(populationSize);
        population.clear();
        // Obtain the next front
        front = ranking.getSubfront(index);

        for (int i = 0; i < nrZones; i++) {
            OutputPopulation(ranking.getSubfront(i), "afz" + (i + 1) + "_");
        }

       // OutputPopulation(bestSupportVectors, "supportVectors");
        String currentMilis = System.currentTimeMillis() + "";
        OutputPopulationSynthetic(bestSupportVectors, "supportVectors"+currentMilis);
        int nrFronts = ranking.getNumberOfSubfronts();
        int currentFrontNr = (int) ((populationSize * (1 - reductionRate) * Math.pow(reductionRate,
                index)) / (1 - Math.pow(reductionRate, nrFronts)) + 1);

        while (remain > 0 && index < nrFronts) {
            // Assign crowding distance to individuals
            distance.crowdingDistanceAssignment(front,
                    problem_.getNumberOfObjectives());
            front.sort(new jmetal.base.operator.comparator.CrowdingComparator());

            int k;
            for (k = 0; k < remain && k < currentFrontNr && k < front.size(); k++) {
                population.add(front.get(k));
            }

            for (int i = k - 1; i >= 0; i--) {
                front.remove(i);
            }

            // for
            // Decrement remain
            remain = remain - k;

            // Obtain the next front
            index++;
            currentFrontNr = (int) ((populationSize * (1 - reductionRate) * Math.pow(reductionRate,
                    index)) / (1 - Math.pow(reductionRate, nrFronts))) + 1;
            if (remain > 0) {
                try {
                    front = ranking.getSubfront(index);
                } catch (Exception ex) {
                    break;
                }
            }

        } // while

        index = 0;

        do {
            front = ranking.getSubfront(index);
            for (int i = 0; i < front.size() && remain > 0; i++) {
                population.add(front.get(i));
                remain--;
            }
            index++;
        } while (remain > 0);

        selectNextSupportVectors(minSupportVectorNumber, ranking, population);

        return population;
    }

    private void selectNextSupportVectors(int minSupportVectorNumber, ApparentFrontRanking ranking, SolutionSet population) {
        bestSupportVectors.clear();

        SupportVectorsHelper supportVectorsHelper = new SupportVectorsHelper();
        SolutionSet initialSupportVectors = supportVectorsHelper.getAFZGASupportVectors(ranking, nrZonesUsedForSupportVectors, supportVectorsPerFront + 1);
        SolutionSet marginalSupportVectors = supportVectorsHelper.getSupportVectorsCloseToAxis(population, ranking, nrZonesUsedForSupportVectors, supportVectorsPerAxis, 40, 20);
        
        String currentMillis = System.currentTimeMillis() + "";
        OutputPopulationSynthetic(marginalSupportVectors, "marginal"+currentMillis);
        OutputPopulationSynthetic(initialSupportVectors, "initial"+currentMillis);
        
        bestSupportVectors = supportVectorsHelper.combine(marginalSupportVectors, initialSupportVectors);

        if (bestSupportVectors.size() < minSupportVectorNumber) {
            int i = -1;
            do {
                i++;
                Solution currentSolution = population.get(i);
                if (bestSupportVectors.deepContains(currentSolution)) {
                    continue;
                }
                bestSupportVectors.add(population.get(i));
            } while (bestSupportVectors.size() < minSupportVectorNumber);
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
                Logger.getLogger(AFZGA.class.getName()).log(Level.INFO, "#############################While RefillPopulation with currentSize: " + newPopulation.size());
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
                                    Logger.getLogger(AFZGA.class.getName()).log(Level.INFO, "While RefillPopulation Added offsprings " + i);
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

        Logger.getLogger(AFZGA.class.getName()).log(Level.INFO, "Leaving RefillPopulation with a new populationsSize of: " + newPopulation.size());
        return newPopulation.union(currentPopulation);
    }
}
