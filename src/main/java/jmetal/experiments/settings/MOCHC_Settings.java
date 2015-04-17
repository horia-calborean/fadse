/**
 * CSGAII_Settings.java
 *
 * @author Radu Chis
 * @version 1.0
 *
 * CSGAII_Settings class of algorithm NSGAII
 */
package jmetal.experiments.settings;

import jmetal.metaheuristics.mochc.MOCHC;
import jmetal.base.*;
import jmetal.base.operator.crossover.CrossoverFactory;
import jmetal.base.operator.mutation.MutationFactory;
import jmetal.base.operator.selection.SelectionFactory;
import jmetal.experiments.Settings;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.JMException;

/**
 * @author Radu Chis
 */
public class MOCHC_Settings extends Settings {
  public int populationSize_                 = 100   ; 
  public int maxEvaluations_                 = 60000 ;
  public int convergenceValue_                 = 3 ;
  public double mutationProbability_         = 0.35 ;
  public double preservedPopulation_         = 0.05 ;
  public double crossoverProbability_         = 1 ;
  public double initialConvergenceCount_         = 0.25 ;    
  public String crossoverOperator_ = "SinglePointCrossover";
  public String mutationOperator_ = "BitFlipMutation";
  public String newGenerationSelectionOperator_ = "RankingAndCrowdingSelection";  
  public String parentsSelection_ = "RandomSelection";
  public double reductionRate_  = 0.5  ;
  public double hyperVolumeMinChange_ = 0.001;
  public int maxNrPopulationsWhereHyperVolumeNoChange_ = 5;
  /**
   * Constructor
   * @throws JMException 
   */
  public MOCHC_Settings(Problem problem) throws JMException {
    super(problem) ;
  } // CNSGAII_Settings

  
  /**
   * Configure NSGAII with user-defined parameter settings
   * @return A NSGAII algorithm object
   * @throws jmetal.util.JMException
   */
  public Algorithm configure() throws JMException {
    Algorithm algorithm ;
    Operator crossoverOperator      ;
    Operator mutationOperator       ;
    Operator parentsSelection       ;
    Operator newGenerationSelection ;
    
    QualityIndicator indicators ;
    
    // Creating the algorithm. There are two choices: NSGAII and its steady-
    // state variant ssNSGAII
    algorithm = new MOCHC(problem_) ;
    //algorithm = new ssNSGAII(problem_) ;
    
    // Algorithm parameters
    algorithm.setInputParameter("initialConvergenceCount",initialConvergenceCount_);
    algorithm.setInputParameter("preservedPopulation",preservedPopulation_);
    algorithm.setInputParameter("convergenceValue",convergenceValue_);
    algorithm.setInputParameter("populationSize",populationSize_);
    algorithm.setInputParameter("maxEvaluations",maxEvaluations_);
    algorithm.setInputParameter("populationSize",populationSize_);
    algorithm.setInputParameter("maxEvaluations",maxEvaluations_);
    algorithm.setInputParameter("hyperVolumeMinChange",hyperVolumeMinChange_);
    algorithm.setInputParameter("maxNrPopulationsWhereHyperVolumeNoChange",maxNrPopulationsWhereHyperVolumeNoChange_);
    
    // Mutation and Crossover for Real codification
    crossoverOperator = CrossoverFactory.getCrossoverOperator(crossoverOperator_);
    crossoverOperator.setParameter("probability",crossoverProbability_);
    //crossover.setParameter("distributionIndex",crossoverDistributionIndex_);

    mutationOperator = MutationFactory.getMutationOperator(mutationOperator_);
    mutationOperator.setParameter("probability",mutationProbability_);
    //mutation.setParameter("distributionIndex",crossoverDistributionIndex_);

    // Selection Operator
    parentsSelection = SelectionFactory.getSelectionOperator(parentsSelection_) ; 

    newGenerationSelection = SelectionFactory.getSelectionOperator(newGenerationSelectionOperator_) ;   
    newGenerationSelection.setParameter("problem", problem_) ;  
    newGenerationSelection.setParameter("populationSize", populationSize_) ;  
    
    // Add the operators to the algorithm
    algorithm.addOperator("crossover",crossoverOperator);
    algorithm.addOperator("cataclysmicMutation",mutationOperator);
    algorithm.addOperator("parentSelection",parentsSelection);
    algorithm.addOperator("newGenerationSelection",newGenerationSelection);
    
   // Creating the indicator object
   if ((paretoFrontFile_!=null) && (!paretoFrontFile_.equals(""))) {
      indicators = new QualityIndicator(problem_, paretoFrontFile_);
      algorithm.setInputParameter("indicators", indicators) ;  
   } // if
   
    return algorithm ;
  } // configure
} // CSGAII_Settings
