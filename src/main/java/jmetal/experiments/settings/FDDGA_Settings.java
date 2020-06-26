package jmetal.experiments.settings;

import jmetal.base.Algorithm;
import jmetal.base.Problem;
import jmetal.base.operator.crossover.Crossover;
import jmetal.base.operator.crossover.CrossoverFactory;
import jmetal.base.operator.mutation.Mutation;
import jmetal.base.operator.mutation.MutationFactory;
import jmetal.base.operator.selection.Selection;
import jmetal.base.operator.selection.SelectionFactory;
import jmetal.experiments.Settings;
import jmetal.metaheuristics.fddga.FDD_GA;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.JMException;

public class FDDGA_Settings extends Settings {
    public int populationSize_ = 100;
    public int maxEvaluations_ = 25000;
    public double mutationProbability_ = 1.0 / problem_.getNumberOfVariables();
    public double crossoverProbability_ = 0.9;
    public double mutationDistributionIndex_ = 20.0;
    public double crossoverDistributionIndex_ = 20.0;
    public int initialGeneration_ = 0;
    public String crossoverOperator_ = "SBXCrossover";
    public String mutationOperator_ = "PolynomialMutation";
    public String selectionOperator_ = "FDDGARouletteWheelSelection";

    /**
     * Constructor
     *
     * @throws JMException
     */
    public FDDGA_Settings(Problem problem) throws JMException {
        super(problem);
    } // FDDGA_Settings


    /**
     * Configure FDD_GA with user-defined parameter settings
     *
     * @return A FDD_GA algorithm object
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
        algorithm = new FDD_GA(problem_);
        //algorithm = new ssNSGAII(problem_) ;

        // Algorithm parameters
        algorithm.setInputParameter("populationSize", populationSize_);
        algorithm.setInputParameter("maxEvaluations", maxEvaluations_);
        algorithm.setInputParameter("initialGeneration", initialGeneration_);
        // Mutation and Crossover for Real codification
        crossover = CrossoverFactory.getCrossoverOperator(crossoverOperator_);
        crossover.setParameter("probability", crossoverProbability_);
        crossover.setParameter("distributionIndex", crossoverDistributionIndex_);

        mutation = MutationFactory.getMutationOperator(mutationOperator_);
        mutation.setParameter("probability", mutationProbability_);
        mutation.setParameter("distributionIndex", crossoverDistributionIndex_);

        // Selection Operator
        selection = (Selection) SelectionFactory.getSelectionOperator(selectionOperator_);

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
}
