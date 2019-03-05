package jmetal.metaheuristics.afzga;

import jmetal.base.Problem;
import jmetal.base.SolutionSet;
import jmetal.metaheuristics.nsgaII.NSGAII;
import jmetal.util.AfMembership;
import jmetal.util.ApparentFront;
import jmetal.util.ApparentFrontRanking;
import jmetal.util.Ranking;
import ro.ulbsibiu.fadse.extended.problems.simulators.ServerSimulator;

public class AFZGA extends NSGAII {
    /**
     * Constructor
     *
     * @param problem Problem to solve
     */
    public AFZGA(Problem problem) {
        super(problem);
    }
    

    @Override
    protected SolutionSet SelectNextGeneration(SolutionSet union, int populationSize) {
        //Distance distance = new Distance();
        AfMembership afMembership = new AfMembership();
        ApparentFront af = new ApparentFront(11);
        // Ranking the union
        ApparentFrontRanking ranking = new ApparentFrontRanking(af, union, 3);
        //Ranking ranking = new Ranking(union);
        int remain = populationSize;
        int index = 0;
        SolutionSet front = null;
        SolutionSet population = new SolutionSet(populationSize);
        population.clear();
        // Obtain the next front
        front = ranking.getSubfront(index);


        int minVectors = front.get(0).numberOfObjectives() + 1;
        //if we have at least N+1 individuals on the first front
        if (front.size() >= minVectors) {
            af.fit(front);
        } else {
            SolutionSet supportVectors = new SolutionSet();

            for (int i = 0; i < front.size(); i++) {
                supportVectors.add(front.get(i));
            }

            int l = index + 1;
            SolutionSet nextFront = ranking.getSubfront(l);
            while (supportVectors.size() < minVectors) {
                int necessary = minVectors - supportVectors.size();
                int size = nextFront.size() <= necessary ? nextFront.size() : necessary;
                for (int i = 0; i < size; i++) {
                    supportVectors.add(nextFront.get(i));
                }
                l++;
            }

            af.fit(supportVectors);
        }

        while ((remain > 0) && (remain >= front.size())) {
            for (int k = 0; k
                    < front.size(); k++) {
                //Assign afr membership to individual
                double membership = afMembership.compute(af, front.get(k));
                front.get(k).setAfrMembership(membership);

                //Add individual to the population
                population.add(front.get(k));
            }

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
            distance.crowdingDistanceAssignment(front, problem_.getNumberOfObjectives());
            front.sort(new jmetal.base.operator.comparator.CrowdingComparator());
            for (int k = 0; k < remain; k++) {
                population.add(front.get(k));
            } // for
            remain = 0;
        }
        return population;
    }

    @Override
    protected void DoEndRoundOutputs(SolutionSet population) {
        if(problem_ instanceof ServerSimulator){
            OutputPopulation(population, "filled");
            Ranking ranking_temp = new Ranking(population);
            OutputPopulation(ranking_temp.getSubfront(0), "pareto");

            ApparentFront af = new ApparentFront(11);
            ApparentFrontRanking ranking = new ApparentFrontRanking(af, population, 3);

            SolutionSet zone1Solutions = ranking.getSubfront(0);
            SolutionSet zone2Solutions = ranking.getSubfront(1);
            SolutionSet zoneSolutions = zone1Solutions.size() > 0 ? zone1Solutions : zone2Solutions;

            OutputPopulation(zoneSolutions, "zone");
            OutputPopulation(zone1Solutions, "zone1");
            OutputPopulation(zone2Solutions, "zone2");
        } else {
            if (outputEveryPopulation) {
                population.printObjectivesToFile(outputPath + System.currentTimeMillis() + ".csv");
            }
        }
    }
}
