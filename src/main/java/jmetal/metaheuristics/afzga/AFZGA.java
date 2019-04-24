package jmetal.metaheuristics.afzga;

import jmetal.base.Problem;
import jmetal.base.Solution;
import jmetal.base.SolutionSet;
import jmetal.metaheuristics.nsgaII.NSGAII;
import jmetal.util.*;
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

    private int nrZones = 3;

    private SolutionSet bestSupportVectors;

    @Override
    protected SolutionSet SelectNextGeneration(SolutionSet union, int populationSize) {
        //Distance distance = new Distance();
        //AfMembership afMembership = new AfMembership();
        ApparentFront af = new ApparentFront(11);

        int minVectors = union.get(0).numberOfObjectives() + 1;
        //SolutionSet supportVectors = new SolutionSet(minVectors);
        if(bestSupportVectors == null){
            bestSupportVectors = GenerateSupportVectors(union, minVectors);
        }

        ApparentFrontHelper.FitTheFront(af, bestSupportVectors);

        ApparentFrontRanking ranking = new ApparentFrontRanking(af, union, nrZones);

        int remain = populationSize;
        int index = 0;
        SolutionSet front = null;
        SolutionSet population = new SolutionSet(populationSize);
        population.clear();

        for(int i=0;i<nrZones;i++) {
            OutputPopulation(ranking.getSubfront(i), "afz"+(i+1)+"_");
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

        bestSupportVectors.clear();
        for(int i=0;i<minVectors;i++){
            bestSupportVectors.add(population.get(i));
        }

        OutputPopulation(bestSupportVectors, "supportVectors");

        return population;
    }

    private SolutionSet GenerateSupportVectors(SolutionSet union, int minVectors) {
        bestSupportVectors = new SolutionSet(minVectors);
        SolutionSet temp = new SolutionSet(union.size());
        for(int i=0;i<union.size();i++){
            temp.add(new Solution(union.get(i)));
        }

        distance.crowdingDistanceAssignment(temp, problem_.getNumberOfObjectives());
        temp.sort(new jmetal.base.operator.comparator.CrowdingComparator());
        for (int k = 0; k < minVectors; k++) {
            bestSupportVectors.add(temp.get(k));
        } // for
        return bestSupportVectors;
    }
}
