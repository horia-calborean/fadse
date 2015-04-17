/**
 * MOCHC.java
 *
 * @author Juan J. Durillo
 * @version 1.0
 */
package jmetal.metaheuristics.mochc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import jmetal.base.*;
import jmetal.util.archive.*;
import jmetal.base.operator.comparator.CrowdingComparator;
import jmetal.base.variable.Binary;
import jmetal.util.JMException;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import ro.ulbsibiu.fadse.environment.parameters.CheckpointFileParameter;
import ro.ulbsibiu.fadse.extended.problems.simulators.ServerSimulator;
import ro.ulbsibiu.fadse.extended.qualityIndicator.MetricsUtil;
import ro.ulbsibiu.fadse.extended.qualityIndicator.TwoSetHypervolumeDifferenceResult;

/**
 *
 * Class implementing the CHC algorithm.
 */
public class MOCHC extends Algorithm {

    /**
     * Stores the problem to solve
     */
    private Problem problem_;

    /**
     * Constructor Creates a new instance of MOCHC
     */
    public MOCHC(Problem problem) {
        problem_ = problem;
    }

    /**
     * Compares two solutionSets to determine if both are equals
     *
     * @param solutionSet A <code>SolutionSet</code>
     * @param newSolutionSet A <code>SolutionSet</code>
     * @return true if both are cotains the same solutions, false in other case
     */
    public boolean equals(SolutionSet solutionSet, SolutionSet newSolutionSet) {
        boolean found;
        for (int i = 0; i < solutionSet.size(); i++) {
            int j = 0;
            found = false;
            while (j < newSolutionSet.size()) {
                if (solutionSet.get(i).equals(newSolutionSet.get(j))) {
                    found = true;
                }
                j++;
            }
            if (!found) {
                return false;
            }
        }
        return true;
    } // equals

    /**
     * Calculate the hamming distance between two solutions
     *
     * @param solutionOne A <code>Solution</code>
     * @param solutionTwo A <code>Solution</code>
     * @return the hamming distance between solutions
     */
    public int hammingDistance(Solution solutionOne, Solution solutionTwo) {
        int distance = 0;
        for (int i = 0; i < problem_.getNumberOfVariables(); i++) {
            try {
                if ((double) solutionOne.getDecisionVariables()[i].getValue() != (double) solutionTwo.getDecisionVariables()[i].getValue()) {
                    distance++;
                }
            } catch (JMException ex) {
                Logger.getLogger(MOCHC.class.getName()).log(Level.SEVERE, "O crepat prin hamming distance", ex);
            }
        }
        return distance;
    } // hammingDistance

    /**
     * Runs of the MOCHC algorithm.
     *
     * @return a <code>SolutionSet</code> that is a set of non dominated
     * solutions as a result of the algorithm execution
     */
    public SolutionSet execute() throws JMException, ClassNotFoundException {
        int iterations;
        int populationSize;
        int convergenceValue;
        int maxEvaluations;
        int minimumDistance;
        int evaluations;
        int maxNrPopulationsWhereHyperVolumeNoChange;

        Comparator crowdingComparator = new CrowdingComparator();

        Operator crossover;
        Operator parentSelection;
        Operator newGenerationSelection;
        Operator cataclysmicMutation;

        double preservedPopulation;
        double initialConvergenceCount;
        boolean condition = false;
        double hyperVolumeMinChange = 0;
        SolutionSet solutionSet, offspringPopulation, newPopulation;

        // Read parameters
        initialConvergenceCount =
                ((Double) getInputParameter("initialConvergenceCount")).doubleValue();
        preservedPopulation =
                ((Double) getInputParameter("preservedPopulation")).doubleValue();
        convergenceValue =
                ((Integer) getInputParameter("convergenceValue")).intValue();
        populationSize =
                ((Integer) getInputParameter("populationSize")).intValue();
        maxEvaluations =
                ((Integer) getInputParameter("maxEvaluations")).intValue();
        maxNrPopulationsWhereHyperVolumeNoChange =
                ((Integer) getInputParameter("maxNrPopulationsWhereHyperVolumeNoChange")).intValue();
        hyperVolumeMinChange = ((Double) getInputParameter("hyperVolumeMinChange")).doubleValue();

        boolean outputEveryPopulation = false;
        Object output = getInputParameter("outputEveryPopulation");
        if (output != null) {
            outputEveryPopulation = (Boolean) output;
        }
        String outputPath = (String) getInputParameter("outputPath");

        iterations = 0;
        evaluations = 0;

        int nrObjectives = problem_.getNumberOfObjectives();
        int hyperVolumeNoChangeCount = 0;

        double[] maxObjectives = new double[nrObjectives];
        //Calculate the maximum problem sizes
        Solution aux = new Solution(problem_);
        int size = problem_.getNumberOfVariables();

        minimumDistance = (int) Math.floor(initialConvergenceCount * size + 1);

        solutionSet = new SolutionSet(populationSize);

        // Read operators
        crossover = (Operator) getOperator("crossover");
        cataclysmicMutation = (Operator) getOperator("cataclysmicMutation");
        parentSelection = (Operator) getOperator("parentSelection");
        newGenerationSelection = (Operator) getOperator("newGenerationSelection");
        // Create the initial solutionSet
        Solution newSolution;
        // Added by HORIA
        CheckpointFileParameter fileParam = (CheckpointFileParameter) getInputParameter("checkpointFile");
        String file = "";
        if (fileParam != null) {
            file = fileParam.GetCheckpointFile();
        }
        String feasible = (String) getInputParameter("forceFeasibleFirstGeneration");
        int feasiblePercentage = Integer
                .parseInt((String) getInputParameter("forceMinimumPercentageFeasibleIndividuals"));
        if (file != null && !file.equals("")) {
            Logger.getLogger(MOCHC.class.getName()).log(Level.WARNING,
                    "Using a checkpoint file: " + file);
            int i = 0;
            try {
                BufferedReader input = new BufferedReader(new FileReader(file));

                String line = null; // not declared within while loop
                line = input.readLine();// skip the headder
                while ((line = input.readLine()) != null && i < populationSize) {
                    newSolution = new Solution(problem_);

                    StringTokenizer tokenizer = new StringTokenizer(line, ",");
                    for (int j = 0; j < problem_.getNumberOfVariables(); j++) {
                        newSolution.getDecisionVariables()[j].setValue(Double
                                .valueOf(tokenizer.nextToken()));
                    }
                    problem_.evaluate(newSolution);
                    problem_.evaluateConstraints(newSolution);
                    evaluations++;
                    solutionSet.add(newSolution);
                    i++;
                } // while
                if (i < populationSize) {
                    throw new IOException(
                            "Checkpoint file does not have enough elements to fill the entire population");
                }
            } catch (IOException ex) {
                Logger.getLogger(MOCHC.class.getName()).log(
                        Level.SEVERE,
                        "Checkpoint file does not have enough elements to fill the entire population ["
                        + i + "<" + populationSize
                        + "]. Filling it with random individuals");
                while (i < populationSize) {
                    newSolution = new Solution(problem_);

                    problem_.evaluate(newSolution);
                    problem_.evaluateConstraints(newSolution);

                    evaluations++;
                    solutionSet.add(newSolution);
                    i++;
                }
            }
        } else {
            for (int i = 0; i < populationSize; i++) {
                Solution solution = new Solution(problem_);
//            for(int ii = 0;ii<solution.numberOfVariables();ii++){
//                solution.getDecisionVariables()[ii].setValue(5);
//            }
                problem_.evaluate(solution);
                problem_.evaluateConstraints(solution);
                evaluations++;
                solutionSet.add(solution);
                System.out.println("Current Population size is: " + solutionSet.size());
            }
            // Added by HORIA
        }


        if (problem_ instanceof ServerSimulator) {
            ((ServerSimulator) problem_).join();//blocks until all  the offsprings are evaluated
            ((ServerSimulator) problem_).dumpCurrentPopulation(solutionSet);
        }
        while (!condition) {
            offspringPopulation = new SolutionSet(populationSize);
            for (int i = 0; i < solutionSet.size() / 2; i++) {
                Solution[] parents = (Solution[]) parentSelection.execute(solutionSet);
                int hammingDistance = hammingDistance(parents[0], parents[1]);
                //Equality condition between solutions
                System.out.println("Hamming distance is: " + hammingDistance + " while minimum distance is: " + minimumDistance);
                if (hammingDistance(parents[0], parents[1]) >= (minimumDistance)) {
                    Solution[] offspring = (Solution[]) crossover.execute(parents);
                    problem_.evaluate(offspring[0]);
                    problem_.evaluateConstraints(offspring[0]);
                    problem_.evaluate(offspring[1]);
                    problem_.evaluateConstraints(offspring[1]);
                    evaluations += 2;
                    offspringPopulation.add(offspring[0]);
                    offspringPopulation.add(offspring[1]);
                    System.out.println("Current Population size is: " + offspringPopulation.size());
                }
            }
            if (problem_ instanceof ServerSimulator) {
                ((ServerSimulator) problem_).join();//blocks until all  the offsprings are evaluated
                ((ServerSimulator) problem_).dumpCurrentPopulation("offspring" + System.currentTimeMillis(), offspringPopulation);
            }
            SolutionSet union = solutionSet.union(offspringPopulation);
            newGenerationSelection.setParameter("populationSize", populationSize);
            newPopulation = (SolutionSet) newGenerationSelection.execute(union);

            TwoSetHypervolumeDifferenceResult p = MetricsUtil.computeHypervolumeTwoSetDifferenceForTwoSets(solutionSet, newPopulation, problem_.getNumberOfObjectives(), populationSize, maxObjectives);
            if (p.CombinedHyperVolume21 < p.FirstHyperVolume * hyperVolumeMinChange) {
                hyperVolumeNoChangeCount++;
            } else {
                hyperVolumeNoChangeCount = 0;
            }

            System.out.println("CombinedHyperVolume is: " + p.CombinedHyperVolume21);
            System.out.println("FirstHyperVolume is: " + p.FirstHyperVolume);
            System.out.println("HyperVolumeNoChangeCount is: " + hyperVolumeNoChangeCount);
            System.out.println("Minimum distance is: " + minimumDistance);

            if (equals(solutionSet, newPopulation)) {
                minimumDistance--;
            }
            if (minimumDistance <= convergenceValue || hyperVolumeNoChangeCount >= maxNrPopulationsWhereHyperVolumeNoChange) {
                System.out.println("Cataclysmic mutation happened!!!!!");
                //minimumDistance = (int) (1.0 / size * (1 - 1.0 / size) * size);
                minimumDistance = (int) (0.35 * (1 - 0.35) * size);
                hyperVolumeNoChangeCount = 0;
                int preserve = (int) Math.floor(preservedPopulation * populationSize);
                newPopulation = new SolutionSet(populationSize);
                solutionSet.sort(crowdingComparator);
                for (int i = 0; i < preserve; i++) {
                    newPopulation.add(new Solution(solutionSet.get(i)));
                }
                for (int i = preserve; i < populationSize; i++) {
                    Solution solution = new Solution(solutionSet.get(i));                    
                    cataclysmicMutation.execute(solution);
                    problem_.evaluate(solution);
                    problem_.evaluateConstraints(solution);                    
                    newPopulation.add(solution);
                    //System.out.println("Hux " + newPopulation.size());
                }
                if (problem_ instanceof ServerSimulator) {
                    ((ServerSimulator) problem_).join();//blocks until all  the offsprings are evaluated                
                }            
            }
            iterations++;

            solutionSet = newPopulation;
            if (problem_ instanceof ServerSimulator) {
                ((ServerSimulator) problem_).dumpCurrentPopulation(solutionSet);
            } else {
                if (outputEveryPopulation) {
                    solutionSet.printObjectivesToFile(outputPath
                            + System.currentTimeMillis() + ".csv");
                }
            }
            if (evaluations >= maxEvaluations) {
                condition = true;
            }
        }


        CrowdingArchive archive;
        archive = new CrowdingArchive(populationSize, problem_.getNumberOfObjectives());
        for (int i = 0; i < solutionSet.size(); i++) {
            archive.add(solutionSet.get(i));
        }

        return archive;
    } // execute
}  // MOCHC

