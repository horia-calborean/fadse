package jmetal.metaheuristics.pureAFGA;

import jmetal.base.Problem;
import jmetal.base.SolutionSet;
import jmetal.metaheuristics.nsgaII.NSGAII;
import jmetal.util.*;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PureAFGA extends NSGAII {
    /**
     * Constructor
     *
     * @param problem Problem to solve
     */
    public PureAFGA(Problem problem) {
        super(problem);
    }

    @Override
    protected SolutionSet InitializeEverything() throws JMException, ClassNotFoundException {
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

        AsignAfrMembership(population);
        return population;
    }

    @Override
    protected SolutionSet SelectNextGeneration(SolutionSet union, int populationSize) {
        Logger.getLogger(PureAFGA.class.getName()).log(Level.INFO, "Entered SelectNextGeneration with populationsize of: " + populationSize);

        AsignAfrMembership(union);

        // sort union by AF membership
        union.sort(new jmetal.base.operator.comparator.AfrMembershipComparator());

        SolutionSet population = new SolutionSet(populationSize);

        // add first populationSize individuals to the population
        for (int k = 0; k < populationSize; k++) {
            population.add(union.get(k));
        }

        Logger.getLogger(PureAFGA.class.getName()).log(Level.INFO, "Leaving SelectNextGeneration with populationsize of: " + population.size());

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

    private void AsignAfrMembership(SolutionSet population) {
        AfMembership afMembership = new AfMembership();

        // compute Apparent Front using the entire population
        ApparentFront af = new ApparentFront(11);
        FitTheFront(af, population);

        // turn to maximization problem
        GapObjectivesNormalizer normalizer = new GapObjectivesNormalizer(population);
        normalizer.scaleObjectives();

        for (int k = 0; k < population.size(); k++) {
            //Assign afr membership to individual
            double membership = afMembership.compute(af, population.get(k));
            population.get(k).setAfrMembership(membership);
        }

        // restore to minimization problem
        normalizer.restoreObjectives();
    }
}
