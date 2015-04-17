/**
 * MOCHC_main.java
 *
 * @author Juan J. Durillo
 * @version 1.0
 *
 * This class executes the algorithm described in: A.J. Nebro, E. Alba, G.
 * Molina, F. Chicano, F. Luna, J.J. Durillo "Optimal antenna placement using a
 * new multi-objective chc algorithm". GECCO '07: Proceedings of the 9th annual
 * conference on Genetic and evolutionary computation. London, England. July
 * 2007.
 */
package jmetal.metaheuristics.mochc;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import jmetal.base.*;
import jmetal.base.operator.crossover.*;
import jmetal.base.operator.mutation.*;
import jmetal.base.operator.selection.*;
import jmetal.problems.*;
import jmetal.problems.DTLZ.*;
import jmetal.problems.ZDT.*;
import jmetal.problems.WFG.*;
import jmetal.util.ApplicationConstants;

public class MOCHC_main {

    public static void main(String[] args) {
        try {
            Problem problem = new DTLZ1("Real");
            List<Problem> problems = new LinkedList<Problem>();
            problems.add(new DTLZ1("Real"));
            problems.add(new DTLZ2("Real"));
            problems.add(new DTLZ3("Real"));
            problems.add(new DTLZ4("Real"));
            problems.add(new DTLZ5("Real"));
            problems.add(new DTLZ6("Real"));
            problems.add(new DTLZ7("Real"));
            problems.add(new ZDT1("Real", 100));
            problems.add(new ZDT2("Real", 100));
            problems.add(new ZDT3("Real", 100));
            problems.add(new ZDT4("Real", 100));
            problems.add(new ZDT6("Real", 100));
            for (int i = 0; i < problems.size(); i++) {
                problem = problems.get(i);
                Algorithm algorithm = null;
                algorithm = new MOCHC(problem);

                algorithm.setInputParameter("initialConvergenceCount", 0.25);
                algorithm.setInputParameter("preservedPopulation", 0.05);
                algorithm.setInputParameter("convergenceValue", 0);
                algorithm.setInputParameter("populationSize", 100);
                algorithm.setInputParameter("maxEvaluations", 20000);
                algorithm.setInputParameter("maxNrPopulationsWhereHyperVolumeNoChange", 5);
                algorithm.setInputParameter("hyperVolumeMinChange", 0.001);
                algorithm.setInputParameter("forceMinimumPercentageFeasibleIndividuals", "0");
                algorithm.setInputParameter("forceFeasibleFirstGeneration", "False");

                Operator crossoverOperator;
                Operator mutationOperator;
                Operator parentsSelection;
                Operator newGenerationSelection;

                // Crossover operator
                crossoverOperator = CrossoverFactory.getCrossoverOperator("SBXCrossover");
                //crossoverOperator = CrossoverFactory.getCrossoverOperator("HUXCrossoverInt");
                //crossoverOperator = CrossoverFactory.getCrossoverOperator("SinglePointCrossover");
                crossoverOperator.setParameter("probability", 1.0);

                //parentsSelection = new RandomSelection();
                //newGenerationSelection = new RankingAndCrowdingSelection(problem);
                parentsSelection = SelectionFactory.getSelectionOperator("RandomSelection");
                newGenerationSelection = SelectionFactory.getSelectionOperator("RankingAndCrowdingSelection");
                newGenerationSelection.setParameter("problem", problem);

                // Mutation operator
                mutationOperator = MutationFactory.getMutationOperator("PolynomialMutation");
                //mutationOperator = MutationFactory.getMutationOperator("BitFlipMutation");                    
                mutationOperator.setParameter("probability", 0.35);

                algorithm.addOperator("crossover", crossoverOperator);
                algorithm.addOperator("cataclysmicMutation", mutationOperator);
                algorithm.addOperator("parentSelection", parentsSelection);
                algorithm.addOperator("newGenerationSelection", newGenerationSelection);

                boolean outputEveryPopulation = true;
                algorithm.setInputParameter("outputEveryPopulation",
                        outputEveryPopulation);
                String outputPath = "outputs/" + System.currentTimeMillis();
                if (outputEveryPopulation) {
                    outputPath = ApplicationConstants.OutputFolder
                            + problem.getName()
                            + "/MOCHC/"
                            + "/";
                    boolean created = (new File(outputPath)).mkdirs();
                    algorithm.setInputParameter("outputPath", outputPath);
                }

                // Execute the Algorithm 
                long initTime = System.currentTimeMillis();
                SolutionSet population = algorithm.execute();
                long estimatedTime = System.currentTimeMillis() - initTime;
                System.out.println("Total execution time: " + estimatedTime);

                // Print results
                population.printVariablesToFile("VAR");
                population.printObjectivesToFile("FUN");
            }
        } //try           
        catch (Exception e) {
            System.err.println(e);
            e.printStackTrace();
        } //catch        
    }//main
}
