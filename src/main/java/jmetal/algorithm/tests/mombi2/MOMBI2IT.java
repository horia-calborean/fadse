package jmetal.algorithm.tests.mombi2;

import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;
import jmetal.core.algorithm.Algorithm;
import jmetal.algorithm.multiobjective.mombi.MOMBI2;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.crossover.impl.SBXCrossover;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.operator.mutation.impl.PolynomialMutation;
import jmetal.core.operator.selection.SelectionOperator;
import jmetal.core.operator.selection.impl.BinaryTournamentSelection;
import jmetal.problem.multiobjective.dtlz.DTLZ1;
import jmetal.core.qualityindicator.QualityIndicator;
import jmetal.core.qualityindicator.impl.hypervolume.impl.PISAHypervolume;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.SolutionListUtils;
import jmetal.core.util.VectorUtils;
import jmetal.core.util.comparator.RankingAndCrowdingDistanceComparator;
import jmetal.core.util.evaluator.impl.SequentialSolutionListEvaluator;
import jmetal.core.util.pseudorandom.JMetalRandom;

public class MOMBI2IT {
  Algorithm<List<DoubleSolution>> algorithm;

  @Test
  public void shouldTheAlgorithmReturnANumberOfSolutionsWhenSolvingASimpleProblem() throws Exception {
    DTLZ1 problem = new DTLZ1() ;
    CrossoverOperator<DoubleSolution> crossover;
    MutationOperator<DoubleSolution> mutation;
    SelectionOperator<List<DoubleSolution>, DoubleSolution> selection;

    JMetalRandom randomGenerator = JMetalRandom.getInstance() ;
    randomGenerator.setSeed(1450278534242L);

    double crossoverProbability = 0.9 ;
    double crossoverDistributionIndex = 20.0 ;
    crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex) ;

    double mutationProbability = 1.0 / problem.numberOfVariables() ;
    double mutationDistributionIndex = 20.0 ;
    mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex) ;

    selection = new BinaryTournamentSelection<DoubleSolution>(new RankingAndCrowdingDistanceComparator<>());

    algorithm = new MOMBI2<>(problem,400,crossover,mutation,selection,new SequentialSolutionListEvaluator<>(),
        "../resources/weightVectorFiles/mombi2/weight_03D_12.sld");
    algorithm.run();

    List<DoubleSolution> population = algorithm.result() ;

    /*
    Rationale: the default problem is DTLZ1, and MOMBI2, configured with standard
    settings, should return more than 90 solutions
    */
    assertTrue(population.size() >= 91) ;

    randomGenerator.setSeed(System.currentTimeMillis());
  }

  @Test
  public void shouldTheHypervolumeHaveAMininumValue() throws Exception {
    DTLZ1 problem = new DTLZ1() ;
    CrossoverOperator<DoubleSolution> crossover;
    MutationOperator<DoubleSolution> mutation;
    SelectionOperator<List<DoubleSolution>, DoubleSolution> selection;

    JMetalRandom randomGenerator = JMetalRandom.getInstance() ;
    randomGenerator.setSeed(1450278534242L);

    double crossoverProbability = 0.9 ;
    double crossoverDistributionIndex = 20.0 ;
    crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex) ;

    double mutationProbability = 1.0 / problem.numberOfVariables() ;
    double mutationDistributionIndex = 20.0 ;
    mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex) ;

    selection = new BinaryTournamentSelection<DoubleSolution>(new RankingAndCrowdingDistanceComparator<DoubleSolution>());

    algorithm = new MOMBI2<>(problem,400,crossover,mutation,selection,new SequentialSolutionListEvaluator<DoubleSolution>(),
        "../resources/weightVectorFiles/mombi2/weight_03D_12.sld");
    algorithm.run();

    List<DoubleSolution> population = algorithm.result() ;

    /*
    Rationale: the default problem is DTLZ1, and MOMBI2, configured with standard
    settings, should return 100 solutions
    */
    QualityIndicator hypervolume =
            new PISAHypervolume(
                    VectorUtils.readVectors("../resources/referenceFrontsCSV/DTLZ1.3D.csv", ","));

    // Rationale: the default problem is DTLZ1 (3 objectives), and MOMBI2, configured with standard settings, should
    // return find a front with a hypervolume value higher than 0.96

    double hv = hypervolume.compute(SolutionListUtils.getMatrixWithObjectiveValues(population));

    assertTrue(hv > 0.96) ;

    randomGenerator.setSeed(System.currentTimeMillis());
  }
}
