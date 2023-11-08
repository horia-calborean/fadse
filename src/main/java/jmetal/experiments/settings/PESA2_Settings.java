/**
 * SPEA2_Settings.java
 *
 * @author Antonio J. Nebro
 * @version 1.0
 *
 * NSGAII_Settings class of algorithm NSGAII
 */
package jmetal.experiments.settings;

import jmetal.metaheuristics.spea2.*;
import jmetal.base.*;
import jmetal.base.operator.crossover.CrossoverFactory;
import jmetal.base.operator.mutation.MutationFactory;

import jmetal.experiments.Settings;
import jmetal.metaheuristics.pesa2.PESA2;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.JMException;

public class PESA2_Settings extends Settings {

    public int populationSize_ = 10;
    public int archiveSize_ = 100;
    public int bisections_ = 5;
    public int maxEvaluations_ = 25000;
    public double mutationProbability_ = 1.0 / problem_.getNumberOfVariables();
    public double crossoverProbability_ = 0.9;
    public double distributionIndexForCrossover_ = 20.0;
    public double distributionIndexForMutation_ = 20.0;
    public String crossoverOperator_ = "SBXCrossover";
    public String mutationOperator_ = "PolynomialMutation";

    /**
     * Constructor
     */
    public PESA2_Settings(Problem problem) {
        super(problem);
    } // SPEA2_Settings

    /**
     * Configure SPEA2 with default parameter settings
     * @return an algorithm object
     * @throws jmetal.util.JMException
     */
    public Algorithm configure() throws JMException {
        Algorithm algorithm;
        Operator crossover;         // Crossover operator
        Operator mutation;         // Mutation operator

        QualityIndicator indicators;

        // Creating the problem
        algorithm = new PESA2(problem_);

        // Algorithm parameters
        algorithm.setInputParameter("populationSize", populationSize_);
        algorithm.setInputParameter("archiveSize", archiveSize_);
        algorithm.setInputParameter("maxEvaluations", maxEvaluations_);
        algorithm.setInputParameter("bisections", bisections_);

        // Mutation and crossover for real codification
         crossover = CrossoverFactory.getCrossoverOperator(crossoverOperator_);
        crossover.setParameter("probability", crossoverProbability_);
        crossover.setParameter("distributionIndex", distributionIndexForCrossover_);

        mutation = MutationFactory.getMutationOperator(mutationOperator_);
        mutation.setParameter("probability", mutationProbability_);
        mutation.setParameter("distributionIndex", distributionIndexForMutation_);


        // Add the operators to the algorithm
        algorithm.addOperator("crossover", crossover);
        algorithm.addOperator("mutation", mutation);

        // Creating the indicator object
        if (!paretoFrontFile_.equals("")) {
            indicators = new QualityIndicator(problem_, paretoFrontFile_);
            algorithm.setInputParameter("indicators", indicators);
        } // if

        return algorithm;
    } // configure
} // SPEA2_Settings

