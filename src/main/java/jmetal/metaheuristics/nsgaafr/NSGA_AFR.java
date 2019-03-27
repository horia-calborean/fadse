package jmetal.metaheuristics.nsgaafr;

import jmetal.base.*;
import jmetal.metaheuristics.nsgaII.NSGAII;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.*;
import ro.ulbsibiu.fadse.environment.Population;
import ro.ulbsibiu.fadse.environment.parameters.CheckpointFileParameter;
import ro.ulbsibiu.fadse.extended.base.operator.mutation.BitFlipMutationFuzzy;
import ro.ulbsibiu.fadse.extended.base.operator.mutation.BitFlipMutationFuzzyVirtualParameters;
import ro.ulbsibiu.fadse.extended.base.operator.mutation.BitFlipMutationRandomDefuzzifier;
import ro.ulbsibiu.fadse.extended.problems.simulators.ServerSimulator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NSGA_AFR extends NSGAII {
    /**
     * Constructor
     *
     * @param problem Problem to solve
     */

    ObjectivesTranslator objectivesTranslator;

    public NSGA_AFR(Problem problem) {
        super(problem);

        IObjectiveTranslationFunction hwFunction = new IObjectiveTranslationFunction() {
            public double translate(double hardwareComplexity) {
                return (10000 - hardwareComplexity) / 10000;
            }
        };

        IObjectiveTranslationFunction ipcFunction = new IObjectiveTranslationFunction() {
            public double translate(double ipc) {
                return (5 - 1/ipc) / 5;
            }
        };

        List<IObjectiveTranslationFunction> objectiveTranslations = new ArrayList<IObjectiveTranslationFunction>();
        objectiveTranslations.add(hwFunction);
        objectiveTranslations.add(ipcFunction);

        objectivesTranslator = new ObjectivesTranslator(objectiveTranslations);
    }


    @Override
    protected SolutionSet SelectNextGeneration(SolutionSet union, int populationSize) {
        //Distance distance = new Distance();
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
            for (int k = 0; k < front.size(); k++) {
                //Assign afr membership to individual
                double membership = afMembership.compute(af, front.get(k));
                front.get(k).setAfrMembership(membership);
            }

            front.sort(new jmetal.base.operator.comparator.AfrMembershipComparator());
            for (int k = 0; k
                    < remain; k++) {
                population.add(front.get(k));
            }
            remain = 0;
        }
        return population;
    }
}
