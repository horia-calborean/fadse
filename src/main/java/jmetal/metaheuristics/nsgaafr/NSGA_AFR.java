package jmetal.metaheuristics.nsgaafr;

import jmetal.base.Problem;
import jmetal.base.SolutionSet;
import jmetal.metaheuristics.nsgaII.NSGAII;
import jmetal.util.AfMembership;
import jmetal.util.ApparentFront;
import jmetal.util.GapObjectivesNormalizer;
import jmetal.util.Ranking;

import java.util.logging.Level;
import java.util.logging.Logger;

public class NSGA_AFR extends NSGAII {
    /**
     * Constructor
     *
     * @param problem Problem to solve
     */
    public NSGA_AFR(Problem problem) {

        super(problem);
    }


    @Override
    protected SolutionSet SelectNextGeneration(SolutionSet union, int populationSize) {
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

        ApparentFront af = new ApparentFront(11);
        int minVectors = front.get(0).numberOfObjectives() + 1;

        SolutionSet supportVectors;
        supportVectors = ComputeSupportVectors(ranking, index, front, minVectors);

        FitTheFront(af, supportVectors);

        GapObjectivesNormalizer normalizer;

        while ((remain > 0) && (remain >= front.size())) {
            //turn to maximization problem
            normalizer = new GapObjectivesNormalizer(front);
            normalizer.scaleObjectives();

            for (int k = 0; k
                    < front.size(); k++) {
                //Assign afr membership to individual
                double membership = afMembership.compute(af, front.get(k));
                front.get(k).setAfrMembership(membership);

                //Add individual to the population
                population.add(front.get(k));
            }

            //restore to minimization problem
            normalizer.restoreObjectives();

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
            normalizer = new GapObjectivesNormalizer(front);
            normalizer.scaleObjectives();

            for (int k = 0; k < front.size(); k++) {
                //Assign afr membership to individual
                double membership = afMembership.compute(af, front.get(k));
                front.get(k).setAfrMembership(membership);
            }

            //restore to minimization problem
            normalizer.restoreObjectives();

            front.sort(new jmetal.base.operator.comparator.AfrMembershipComparator());
            for (int k = 0; k
                    < remain; k++) {
                population.add(front.get(k));
            }
            remain = 0;
        }

        Logger.getLogger(NSGA_AFR.class.getName()).log(Level.INFO, "Leaving SelectNextGeneration with populationsize of: " + population.size());

        return population;
    }

    private void FitTheFront(ApparentFront af, SolutionSet supportVectors) {
        //turn to maximization problem
        GapObjectivesNormalizer normalizer = new GapObjectivesNormalizer(supportVectors);
        normalizer.scaleObjectives();

        af.fit(supportVectors);

        //restore to minimization problem
        normalizer.restoreObjectives();
    }

    private SolutionSet ComputeSupportVectors(Ranking ranking, int index, SolutionSet front, int minVectors) {
        SolutionSet supportVectors;//if we have at least N+1 individuals on the first front
        if (front.size() >= minVectors) {
            supportVectors = front;
        } else {
            supportVectors = new SolutionSet(minVectors);

            for (int i = 0; i < front.size(); i++) {
                supportVectors.add(front.get(i));
            }

            int l = index + 1;

            while (supportVectors.size() < minVectors) {
                SolutionSet nextFront = ranking.getSubfront(l);
                int necessary = minVectors - supportVectors.size();
                int size = nextFront.size() <= necessary ? nextFront.size() : necessary;
                for (int i = 0; i < size; i++) {
                    supportVectors.add(nextFront.get(i));
                }
                l++;
            }

        }
        return supportVectors;
    }
}
