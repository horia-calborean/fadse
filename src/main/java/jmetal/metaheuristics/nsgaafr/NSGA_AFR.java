package jmetal.metaheuristics.nsgaafr;

import jmetal.base.Problem;
import jmetal.base.Solution;
import jmetal.base.SolutionSet;
import jmetal.metaheuristics.nsgaII.NSGAII;
import jmetal.util.*;
import ro.ulbsibiu.fadse.extended.problems.simulators.ServerSimulator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NSGA_AFR extends NSGAII {

    String outputFolder = null;

    /**
     * Constructor
     *
     * @param problem Problem to solve
     */
    public NSGA_AFR(Problem problem) {

        super(problem);

        if (problem_ instanceof ServerSimulator) {
            outputFolder = ((ServerSimulator) problem_).getEnvironment().getResultsFolder();
        }
    }

    @Override
    protected SolutionSet SelectNextGeneration(SolutionSet union, int populationSize) {
        long startTime = System.currentTimeMillis();
        Logger.getLogger(NSGA_AFR.class.getName()).log(Level.INFO, "Entered SelectNextGeneration with populationsize of: " + populationSize);
        AfMembership afMembership = new AfMembership();
        // Ranking the union
        Ranking ranking = new Ranking(union);
        int remain = populationSize;
        int index = 0;
        SolutionSet front = null;
        SolutionSet population = new SolutionSet(populationSize);
        population.clear();
        // Obtain the next front
        front = ranking.getSubfront(index);

        ApparentFront af = new ApparentFront(4, outputFolder);
        int minVectors = front.get(0).numberOfObjectives() + 1;

        SupportVectorsHelper supportVectorsHelper = new SupportVectorsHelper();
//        SolutionSet initialSupportVectors = supportVectorsHelper.getNSGAAFRSupportVectors(ranking, index, front, minVectors);
//        SolutionSet marginalSupportVectors = supportVectorsHelper.getSupportVectorsCloseToAxis(union, ranking, 3, 5, 20, 20);
//
//        SolutionSet supportVectors = supportVectorsHelper.combine(initialSupportVectors, marginalSupportVectors);

        SolutionSet supportVectors = supportVectorsHelper.getNSGAAFRSupportVectors(ranking, index, front, minVectors);

        OutputPopulation(supportVectors, "supportVectors");

        ApparentFrontHelper.FitTheFront(af, supportVectors, null);

        af.dumpCurrentFront(problem_, "coefficients_" + System.currentTimeMillis());

        SyntheticObjectivesNormalizer normalizer;
        normalizer = new SyntheticObjectivesNormalizer(500);
        normalizer.scaleObjectives(union);

        for (int i = 0; i < union.size(); i++) {
            Solution currentSolution = union.get(i);
            double membership = afMembership.compute(af, currentSolution);
            currentSolution.setAfrMembership(membership);
        }
        if (problem_ instanceof ServerSimulator) {
            ((ServerSimulator) problem_).dumpCurrentPopulation("unionMaximization" + System.currentTimeMillis(), union);
        }

        normalizer.restoreObjectives(union);


        while ((remain > 0) && (remain >= front.size())) {
            //turn to maximization problem
            normalizer = new SyntheticObjectivesNormalizer(500);
            normalizer.scaleObjectives(front);

            for (int k = 0; k < front.size(); k++) {
                //Assign afr membership to individual
                double membership = afMembership.compute(af, front.get(k));
                front.get(k).setAfrMembership(membership);

                //Add individual to the population
                population.add(front.get(k));
            }

            //restore to minimization problem
            normalizer.restoreObjectives(front);

            //Decrement remain
            remain = remain - front.size();

            //Obtain the next front
            index++;
            if (remain > 0) {
                front = ranking.getSubfront(index);
            }
        }
        // Remain is less than front(index).size, insert only the best ones
        if (remain > 0) {  // front contains individuals to insert
            //turn to maximization problem
            normalizer = new SyntheticObjectivesNormalizer(500);
            normalizer.scaleObjectives(front);

            for (int k = 0; k < front.size(); k++) {
                //Assign afr membership to individual
                double membership = afMembership.compute(af, front.get(k));
                front.get(k).setAfrMembership(membership);
            }

            //restore to minimization problem
            normalizer.restoreObjectives(front);

            front.sort(new jmetal.base.operator.comparator.AfrMembershipComparator());
            for (int k = 0; k
                    < remain; k++) {
                population.add(front.get(k));
            }
            remain = 0;
        }

        Logger.getLogger(NSGA_AFR.class.getName()).log(Level.INFO, "Leaving SelectNextGeneration with populationsize of: " + population.size());

        if (outputFolder != null) {
            String fileName = outputFolder + System.getProperty("file.separator") + "nsgaAfr" + System.currentTimeMillis() + ".csv";

            String str = "";

            for (double coeff : af.getCoefficients()) {
                str += "Coeffs: " + coeff + ",";
            }

            str += "\n";
            str += "FrontNR,AfrMemberShip,CrowdingDistance\n";

            for (int i = 0; i < ranking.getNumberOfSubfronts(); i++) {
                SolutionSet currentFront = ranking.getSubfront(i);
                distance.crowdingDistanceAssignment(currentFront, problem_.getNumberOfObjectives());
                for (int j = 0; j < currentFront.size(); j++) {
                    Solution currentSolution = currentFront.get(j);
                    str += i + "," + currentSolution.getAfrMembership() + "," + currentSolution.getCrowdingDistance() + "\n";
                }
            }

            long endTime = System.currentTimeMillis();
            long duration = (endTime - startTime);
            str = "Duration in SelectNextPopulation: " + duration + "\n" + str;
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
                writer.write(str);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return population;
    }

}
