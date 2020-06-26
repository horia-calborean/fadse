package jmetal.metaheuristics.fddga;

import jmetal.base.Problem;
import jmetal.base.Solution;
import jmetal.base.SolutionSet;
import jmetal.base.operator.comparator.FitnessComparator;
import jmetal.metaheuristics.nsgaII.NSGAII;
import jmetal.util.FPDRanking;
import jmetal.util.JMException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class FDD_GA extends NSGAII {

    SolutionSet habitat;
    SolutionSet alphaSet;

    FPDRanking fpdRanking = new FPDRanking();

    double p = 0.5;

    /**
     * Constructor
     *
     * @param problem Problem to solve
     */
    public FDD_GA(Problem problem) {
        super(problem);
    }

    @Override
    public SolutionSet execute() throws JMException, ClassNotFoundException {

        SolutionSet population = InitializeEverything();
        habitat = new SolutionSet(populationSize);
        alphaSet = new SolutionSet(populationSize);
        //***********************************************MAIN ALGORITHM********************************************************
        // Generations ...
        int t = 0;
        while (evaluations < maxEvaluations && t < 100) {
            Logger.getLogger(NSGAII.class.getName()).log(Level.INFO, "Entered NextGeneration with evaluations: " + evaluations);

            fpdRanking.rankSolutionSet(population);
            addToAlphaSet(population);
            addToHabitat(population);

            SolutionSet offspringPopulation = GenerateOffsprings(population, feasiblePercentage);
            JoinAndOutputPopulation(offspringPopulation, "habitat");
            ReEvaluatePopulation(population);
            JoinAndOutputPopulation(population, "corrected");

            // replace population by habitat
            population.clear();

            for (int i = 0; i < populationSize; i++) {
                population.add(habitat.get(i));
            }

            DoIndicatorExtraStuff(population);

            DoEndRoundOutputs(population);

            t++;

        } // while

        // Return as output parameter the required evaluations
        setOutputParameter("evaluations", requiredEvaluations);

        OutputPopulation(alphaSet, "alphaSet");

        return population;


    } // execute

    @Override
    protected SolutionSet GenerateOffsprings(SolutionSet population, int feasiblePercentage) throws JMException {
        Logger.getLogger(FDD_GA.class.getName()).log(Level.INFO, "Entered GenerateOffsprings with feasiblePercentage: " + feasiblePercentage);
        //int nrOfFeasible = 0;

        // Create the offSpring solutionSet
        int poolSize = (int) ((1 - p) * populationSize);
        selectionOperator.setParameter("PoolSize", (poolSize));
        SolutionSet matingPool = (SolutionSet) selectionOperator.execute(population);

        int j = 0;
        for (int i = 0; i < poolSize && habitat.size() < populationSize; i++) {
            if (evaluations < maxEvaluations) {
                Solution[] parents = new Solution[2];
                parents[0] = matingPool.get(j++);
                parents[1] = matingPool.get(j++);

                Solution[] offSpring = (Solution[]) crossoverOperator.execute(parents);
                Solution offs = offSpring[0];
                mutationOperator.execute(offs);
                problem_.evaluate(offs);

                if (habitat.size() < populationSize) {
                    Logger.getLogger(FDD_GA.class.getName()).log(Level.INFO, "While GenerateOffsprings Added offsprings " + i);
                    habitat.add(offs);
                    evaluations += 1;
                }


//                for (int k = 0; k < offSpring.length; k++) {
//                    Solution offs = offSpring[k];
//                    mutationOperator.execute(offs);
//                    problem_.evaluate(offs);
//                    problem_.evaluateConstraints(offs);
//
//                    System.out.println("[0] " + offs.getNumberOfViolatedConstraint() + " " + nrOfFeasible);
//                    if (offs.getNumberOfViolatedConstraint() > 0 && (((nrOfFeasible + 0.0) / populationSize) * 100) < feasiblePercentage) {
//                        //infeasible and we still need feasible individuals in the population
////                        System.out.println("[0] INFESIBLE nr of feasible " + nrOfFeasible + " needed " + feasiblePercentage + "violated " + offSpring[0].getNumberOfViolatedConstraint());
//                        if (mutationOperator instanceof BitFlipMutationFuzzy) {
//                            ((BitFlipMutationFuzzy) mutationOperator).x--;
//                        }
//                    } else {
//                        if (habitat.size() < populationSize) {
//                            Logger.getLogger(FDD_GA.class.getName()).log(Level.INFO, "While GenerateOffsprings Added offsprings " + i);
//                            habitat.add(offs);
//                            evaluations += 1;
//                            nrOfFeasible++;
//                        }
//                    }
//                }
            }
        }

        Logger.getLogger(FDD_GA.class.getName()).log(Level.INFO, "Leaving GenerateOffsprings with a populationsSize of: " + habitat.size());
        return habitat;
    }

    private void addToAlphaSet(SolutionSet population) {
        population.sort(new FitnessComparator());

        Solution bestSolution = population.get(0);

        for (int i = 0; i < alphaSet.size(); i++) {
            if (bestSolution.getFitness() >= alphaSet.get(i).getFitness()) {
                return;
            }
        }

        for (int i = 0; i < alphaSet.size(); i++) {
            if (alphaSet.get(i).getFitness() > bestSolution.getFitness()) {
                alphaSet.remove(i);
            }
        }

        alphaSet.add(bestSolution);
    }

    private void addToHabitat(SolutionSet population) {
        habitat.clear();

        population.sort(new FitnessComparator());
        for (int i = 0; i < p * populationSize; i++) {
            habitat.add(population.get(i));
        }
    }
}
