package jmetal.algorithm.tests.moead;

import static org.junit.Assert.assertTrue;

import java.util.List;

import jmetal.algorithm.multiobjective.moead.AbstractMOEAD;
import jmetal.algorithm.multiobjective.moead.MOEADBuilder;
import org.junit.Ignore;
import org.junit.Test;
import jmetal.core.algorithm.Algorithm;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.crossover.impl.DifferentialEvolutionCrossover;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.operator.mutation.impl.PolynomialMutation;
import jmetal.problem.multiobjective.lz09.LZ09F3;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.legacy.qualityindicator.QualityIndicator;
import jmetal.core.util.legacy.qualityindicator.impl.hypervolume.impl.PISAHypervolume;

public class MOEADDRAIT {

  Algorithm<List<DoubleSolution>> algorithm;

  @Test
  public void shouldTheAlgorithmReturnANumberOfSolutionsWhenSolvingASimpleProblem() {
    LZ09F3 problem = new LZ09F3();

    double cr = 1.0;
    double f = 0.5;
    CrossoverOperator<DoubleSolution> crossover = new DifferentialEvolutionCrossover(cr, f,
            DifferentialEvolutionCrossover.DE_VARIANT.RAND_1_BIN);

    double mutationProbability = 1.0 / problem.numberOfVariables();
    double mutationDistributionIndex = 20.0;
    MutationOperator<DoubleSolution> mutation = new PolynomialMutation(mutationProbability,
        mutationDistributionIndex);

    algorithm = new MOEADBuilder(problem, MOEADBuilder.Variant.MOEADDRA)
        .setCrossover(crossover)
        .setMutation(mutation)
        .setMaxEvaluations(50000)
        .setPopulationSize(300)
        .setResultPopulationSize(100)
        .setNeighborhoodSelectionProbability(0.9)
        .setMaximumNumberOfReplacedSolutions(2)
        .setNeighborSize(20)
        .setFunctionType(AbstractMOEAD.FunctionType.TCHE)
        .setDataDirectory("../resources/weightVectorFiles/moead")
        .build();

    algorithm.run() ;
    List<DoubleSolution> population = algorithm.result();

    assertTrue(population.size() == 100);
  }

  @Ignore("fail when making a deployment")
  @Test
  public void shouldTheHypervolumeHaveAMininumValue() throws Exception {
    LZ09F3 problem = new LZ09F3();

    double cr = 1.0;
    double f = 0.5;
    CrossoverOperator<DoubleSolution> crossover = new DifferentialEvolutionCrossover(cr, f,
            DifferentialEvolutionCrossover.DE_VARIANT.RAND_1_BIN);

    double mutationProbability = 1.0 / problem.numberOfVariables();
    double mutationDistributionIndex = 20.0;
    MutationOperator<DoubleSolution> mutation = new PolynomialMutation(mutationProbability,
        mutationDistributionIndex);

    algorithm = new MOEADBuilder(problem, MOEADBuilder.Variant.MOEADDRA)
        .setCrossover(crossover)
        .setMutation(mutation)
        .setMaxEvaluations(150000)
        .setPopulationSize(300)
        .setResultPopulationSize(100)
        .setNeighborhoodSelectionProbability(0.9)
        .setMaximumNumberOfReplacedSolutions(2)
        .setNeighborSize(20)
        .setFunctionType(AbstractMOEAD.FunctionType.TCHE)
        .setDataDirectory(
            "MOEAD_Weights")
        .build();

    algorithm.run();

    List<DoubleSolution> population = algorithm.result();

    QualityIndicator<List<DoubleSolution>, Double> hypervolume =
        new PISAHypervolume<>("../resources/referenceFrontsCSV/LZ09_F3.csv");

    // Rationale: the default problem is LZ09F", and MOEA/D, configured with standard settings, should
    // return find a front with a hypervolume value higher than 0.96
    double hv = hypervolume.evaluate(population);

    assertTrue(hv > 0.65);
  }
}
