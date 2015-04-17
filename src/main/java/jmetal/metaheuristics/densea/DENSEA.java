/**
 * DENSEA.java
 *
 * @author Juanjo Durillo
 */
package jmetal.metaheuristics.densea;

import java.util.Comparator;

import ro.ulbsibiu.fadse.extended.problems.simulators.ServerSimulator;
import jmetal.base.*;
import jmetal.base.operator.comparator.*;
import jmetal.util.*;

public class DENSEA extends Algorithm {

    private Problem problem_;

    /* Create a new instance of DENSEA algorithm */
    public DENSEA(Problem problem) {
        problem_ = problem;
    }

    //Implements the Densea delete duplicate elements
    public void deleteDuplicates(SolutionSet population) {
        Comparator equalIndividuals = new EqualSolutions();
        for (int i = 0; i < population.size() / 2; i++) {
            for (int j = i + 1; j < population.size() / 2; j++) {
                int flag = equalIndividuals.compare(population.get(i), population.get(j));
                if (flag == 0) {
                    Solution aux = population.get(j);
                    population.replace(j, population.get((population.size() / 2) + j));
                    population.replace((population.size() / 2) + j, aux);
                }
            }
        }
    }

    /* Execute the algorithm */
    public SolutionSet execute() throws JMException, ClassNotFoundException {
        int populationSize, maxEvaluations, evaluations;
        SolutionSet population, offspringPopulation, union;
        Operator mutationOperator, crossoverOperator, selectionOperator;
        Distance distance = new Distance();

        //Read the params
        populationSize = ((Integer) this.getInputParameter("populationSize")).intValue();
        maxEvaluations = ((Integer) this.getInputParameter("maxEvaluations")).intValue();

        //Init the variables
        population = new SolutionSet(populationSize);
        evaluations = 0;

        //Read the operators
        mutationOperator = this.operators_.get("mutation");
        crossoverOperator = this.operators_.get("crossover");
        selectionOperator = this.operators_.get("selection");

        //-> Create the initial population
        Solution newIndividual;
        for (int i = 0; i < populationSize; i++) {
            newIndividual = new Solution(problem_);
            problem_.evaluate(newIndividual);
            problem_.evaluateConstraints(newIndividual);
            evaluations++;
            population.add(newIndividual);
        } //for
        //<-

        if (problem_ instanceof ServerSimulator) {
            ((ServerSimulator) problem_).join();//blocks until all  the offsprings are evaluated
            ((ServerSimulator) problem_).dumpCurrentPopulation(population);
        }
        Ranking r;

        while (evaluations < maxEvaluations) {
            SolutionSet P3 = new SolutionSet(populationSize);
            for (int i = 0; i < populationSize / 2; i++) {
                Solution[] parents = new Solution[2];
                Solution[] offSpring;
                parents[0] = (Solution) selectionOperator.execute(population);
                parents[1] = (Solution) selectionOperator.execute(population);
                offSpring = (Solution[]) crossoverOperator.execute(parents);
                mutationOperator.execute(offSpring[0]);
                problem_.evaluate(offSpring[0]);
                problem_.evaluateConstraints(offSpring[0]);
                evaluations++;
                mutationOperator.execute(offSpring[1]);
                problem_.evaluate(offSpring[1]);
                problem_.evaluateConstraints(offSpring[1]);
                evaluations++;
                P3.add(offSpring[0]);
                P3.add(offSpring[1]);
            }
            if (problem_ instanceof ServerSimulator) {
                ((ServerSimulator) problem_).join();//blocks until all  the offsprings are evaluated
                ((ServerSimulator) problem_).dumpCurrentPopulation("offspring" + System.currentTimeMillis(), P3);
            }

            r = new Ranking(P3);
            for (int i = 0; i < r.getNumberOfSubfronts(); i++) {
                distance.crowdingDistanceAssignment(r.getSubfront(i), problem_.getNumberOfObjectives());
            }
            P3.sort(new CrowdingComparator());


            population.sort(new CrowdingComparator());
//            deleteDuplicates(population);
//            deleteDuplicates(P3);
            deleteDuplicates(population);
            deleteDuplicates(P3);
            SolutionSet auxiliar = new SolutionSet(populationSize);
            for (int i = 0; i < (populationSize / 2); i++) {
                auxiliar.add(population.get(i));
            }

            for (int j = 0; j < (populationSize / 2); j++) {
                //auxiliar.add(population.get(j));
                auxiliar.add(P3.get(j));
            }

            population = auxiliar;

            r = new Ranking(population);
            for (int i = 0; i < r.getNumberOfSubfronts(); i++) {
                distance.crowdingDistanceAssignment(r.getSubfront(i), problem_.getNumberOfObjectives());
            }
            if (problem_ instanceof ServerSimulator) {
                ((ServerSimulator) problem_).dumpCurrentPopulation(population);
            }
        }
        r = new Ranking(population);
        return r.getSubfront(0);
    }
}
