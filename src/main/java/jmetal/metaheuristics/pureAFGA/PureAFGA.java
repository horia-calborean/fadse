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

    private int virtualSupportVectorsPerAxis = 5;

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

        AsignAfrMembership(population);

        Ranking ranking_temp = new Ranking(population);
        OutputPopulation(ranking_temp.getSubfront(0), "pareto");

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
        SyntheticObjectivesNormalizer normalizer = new SyntheticObjectivesNormalizer(supportVectors);
        normalizer.scaleObjectives();

        af.fit(supportVectors);

        af.dumpCurrentFront(problem_, "coefficients_" + System.currentTimeMillis());

        //restore to minimization problem
        normalizer.restoreObjectives();
    }

    private void AsignAfrMembership(SolutionSet population) {
        AfMembership afMembership = new AfMembership();

        // compute Apparent Front using the entire population
        ApparentFront af = new ApparentFront(4);
        SolutionSet supportVectors = new SolutionSet(population.size() + 2 * virtualSupportVectorsPerAxis);

        SolutionSet xVectors = VirtualSupportVectors.getVirtualSupportVectorsCloseToX(population, virtualSupportVectorsPerAxis);
        for (int i = 0; i < xVectors.size(); i++) {
            supportVectors.add(xVectors.get(i));
        }

        SolutionSet yVectors = VirtualSupportVectors.getVirtualSupportVectorsCloseToY(population, virtualSupportVectorsPerAxis);
        for (int i = 0; i < yVectors.size(); i++) {
            supportVectors.add(yVectors.get(i));
        }

        for (int i = 0; i < population.size(); i++) {
            supportVectors.add(population.get(i));
        }

        OutputPopulation(supportVectors, "supportVectors");

        FitTheFront(af, supportVectors);

        // turn to maximization problem
        SyntheticObjectivesNormalizer normalizer = new SyntheticObjectivesNormalizer(population);
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
