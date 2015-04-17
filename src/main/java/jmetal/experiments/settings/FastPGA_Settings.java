/**
 * NSGAII_Settings.java
 *
 * @author Antonio J. Nebro
 * @version 1.0
 *
 * NSGAII_Settings class of algorithm NSGAII
 */
package jmetal.experiments.settings;

import jmetal.base.*;
import jmetal.base.operator.comparator.FPGAFitnessComparator;
import jmetal.base.operator.crossover.Crossover;
import jmetal.base.operator.mutation.Mutation;
import jmetal.base.operator.crossover.CrossoverFactory;
import jmetal.base.operator.mutation.MutationFactory;
import jmetal.base.operator.selection.BinaryTournament;
import jmetal.base.operator.selection.Selection;
import jmetal.base.operator.selection.SelectionFactory;
import jmetal.experiments.Settings;
import jmetal.metaheuristics.densea.DENSEA;
import jmetal.metaheuristics.fastPGA.FastPGA;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.JMException;

/**
 * @author Antonio J. Nebro
 */
public class FastPGA_Settings extends Settings {

    public int maxPopSize_ = 100;
    public int initialPopulationSize_ = 100;
    public int maxEvaluations_ = 25000;
    public double a_ = 20.0;
    public double b_ = 1.0;
    public double c_ = 20.0;
    public double d_ = 0.0;
    public int termination_ = 1;
    public double mutationProbability_ = 1.0 / problem_.getNumberOfVariables();
    public double crossoverProbability_ = 1.0;
    public double mutationDistributionIndex_ = 20.0;
    public double crossoverDistributionIndex_ = 20.0;
    public String crossoverOperator_ = "SBXCrossover";
    public String mutationOperator_ = "PolynomialMutation";

    /**
     * Constructor
     * @throws JMException
     */
    public FastPGA_Settings(Problem problem) throws JMException {
        super(problem);
    } // NSGAII_Settings

    /**
     * Configure NSGAII with user-defined parameter settings
     * @return A NSGAII algorithm object
     * @throws jmetal.util.JMException
     */
    public Algorithm configure() throws JMException {
        Algorithm algorithm;
        Selection selection;
        Crossover crossover;
        Mutation mutation;

        QualityIndicator indicators;

        // Creating the algorithm. There are two choices: NSGAII and its steady-
        // state variant ssNSGAII
        algorithm = new FastPGA(problem_);

        algorithm.setInputParameter("maxPopSize", maxPopSize_);
        algorithm.setInputParameter("initialPopulationSize", initialPopulationSize_);
        algorithm.setInputParameter("maxEvaluations", maxEvaluations_);
        algorithm.setInputParameter("a", a_);
        algorithm.setInputParameter("b", b_);
        algorithm.setInputParameter("c", c_);
        algorithm.setInputParameter("d", d_);

        algorithm.setInputParameter("termination", termination_);
        // Mutation and Crossover for Real codification
        crossover = CrossoverFactory.getCrossoverOperator(crossoverOperator_);
        crossover.setParameter("probability", crossoverProbability_);
        crossover.setParameter("distributionIndex", crossoverDistributionIndex_);

        mutation = MutationFactory.getMutationOperator(mutationOperator_);
        mutation.setParameter("probability", mutationProbability_);
        mutation.setParameter("distributionIndex", crossoverDistributionIndex_);

        // Selection Operator
        selection = new BinaryTournament(new FPGAFitnessComparator());

        // Add the operators to the algorithm
        algorithm.addOperator("crossover", crossover);
        algorithm.addOperator("mutation", mutation);
        algorithm.addOperator("selection", selection);

        // Creating the indicator object
        if ((paretoFrontFile_ != null) && (!paretoFrontFile_.equals(""))) {
            indicators = new QualityIndicator(problem_, paretoFrontFile_);
            algorithm.setInputParameter("indicators", indicators);
        } // if

        return algorithm;
    } // configure
} // NSGAII_Settings

