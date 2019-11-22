package jmetal.metaheuristics.afzga;

import jmetal.base.Problem;
import jmetal.base.Solution;
import jmetal.base.SolutionSet;
import jmetal.metaheuristics.nsgaII.NSGAII;
import jmetal.util.ApparentFront;
import jmetal.util.ApparentFrontHelper;
import jmetal.util.ApparentFrontRanking;
import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.extended.problems.simulators.ServerSimulator;
import ro.ulbsibiu.fadse.utils.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
