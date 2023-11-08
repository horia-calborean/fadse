package jmetal.algorithm.tests.moead;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import jmetal.algorithm.multiobjective.moead.AbstractMOEAD;
import jmetal.algorithm.multiobjective.moead.MOEADBuilder;
import org.junit.Test;
import jmetal.core.algorithm.Algorithm;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.crossover.impl.DifferentialEvolutionCrossover;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.operator.mutation.impl.PolynomialMutation;
import jmetal.problem.multiobjective.lz09.LZ09F2;
import jmetal.problem.multiobjective.lz09.LZ09F6;
import jmetal.core.qualityindicator.QualityIndicator;
import jmetal.core.qualityindicator.impl.hypervolume.impl.PISAHypervolume;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.SolutionListUtils;
import jmetal.core.util.VectorUtils;
import jmetal.core.util.pseudorandom.JMetalRandom;

public class MOEADIT {

  Algorithm<List<DoubleSolution>> algorithm;

  @Test
  public void shouldTheAlgorithmReturnANumberOfSolutionsWhenSolvingASimpleProblem() {
    LZ09F2 problem = new LZ09F2();

    double cr = 1.0;
    double f = 0.5;
    CrossoverOperator<DoubleSolution> crossover = new DifferentialEvolutionCrossover(cr, f,
            DifferentialEvolutionCrossover.DE_VARIANT.RAND_1_BIN);

    double mutationProbability = 1.0 / problem.numberOfVariables();
    double mutationDistributionIndex = 20.0;
    MutationOperator<DoubleSolution> mutation = new PolynomialMutation(mutationProbability,
        mutationDistributionIndex);

    algorithm = new MOEADBuilder(problem, MOEADBuilder.Variant.MOEAD)
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

    algorithm.run() ;
    List<DoubleSolution> population = algorithm.result();

    assertTrue(population.size() == 100);
  }

  @Test
  public void shouldTheHypervolumeHaveAMinimumValueWhenSolvingTheLZ09F2Instance() throws IOException {
    LZ09F2 problem = new LZ09F2();

    double cr = 1.0;
    double f = 0.5;
    CrossoverOperator<DoubleSolution> crossover = new DifferentialEvolutionCrossover(cr, f,
            DifferentialEvolutionCrossover.DE_VARIANT.RAND_1_BIN);

    double mutationProbability = 1.0 / problem.numberOfVariables();
    double mutationDistributionIndex = 20.0;
    MutationOperator<DoubleSolution> mutation = new PolynomialMutation(mutationProbability,
        mutationDistributionIndex);

    algorithm = new MOEADBuilder(problem, MOEADBuilder.Variant.MOEAD)
        .setCrossover(crossover)
        .setMutation(mutation)
        .setMaxEvaluations(150000)
        .setPopulationSize(300)
        .setResultPopulationSize(100)
        .setNeighborhoodSelectionProbability(0.9)
        .setMaximumNumberOfReplacedSolutions(2)
        .setNeighborSize(20)
        .setFunctionType(AbstractMOEAD.FunctionType.TCHE)
        .build();

    algorithm.run();

    List<DoubleSolution> population = algorithm.result();

    QualityIndicator hypervolume =
            new jmetal.core.qualityindicator.impl.hypervolume.impl.PISAHypervolume(
                    VectorUtils.readVectors("../resources/referenceFrontsCSV/LZ09_F2.csv", ","));

    // Rationale: the default problem is LZ09F2", and MOEA/D, configured with standard settings, should
    // return find a front with a hypervolume value higher than 0.96

    double hv = hypervolume.compute(SolutionListUtils.getMatrixWithObjectiveValues(population));

    assertTrue(hv > 0.65);
  }

  @Test
  public void shouldTheHypervolumeHaveAMinimumValueWhenSolvingTheLZ09F6Instance() throws Exception {
    LZ09F6 problem = new LZ09F6();

    JMetalRandom.getInstance().setSeed(1);

    double cr = 1.0;
    double f = 0.5;
    CrossoverOperator<DoubleSolution> crossover = new DifferentialEvolutionCrossover(cr, f,
            DifferentialEvolutionCrossover.DE_VARIANT.RAND_1_BIN);

    double mutationProbability = 1.0 / problem.numberOfVariables();
    double mutationDistributionIndex = 20.0;
    MutationOperator<DoubleSolution> mutation = new PolynomialMutation(mutationProbability,
            mutationDistributionIndex);

    algorithm = new MOEADBuilder(problem, MOEADBuilder.Variant.MOEAD)
            .setCrossover(crossover)
            .setMutation(mutation)
            .setMaxEvaluations(150000)
            .setPopulationSize(300)
            .setResultPopulationSize(100)
            .setNeighborhoodSelectionProbability(0.9)
            .setMaximumNumberOfReplacedSolutions(2)
            .setNeighborSize(20)
            .setFunctionType(AbstractMOEAD.FunctionType.TCHE)
            .setDataDirectory("../resources/weightVectorFiles/moead")
            .build();

    algorithm.run();

    List<DoubleSolution> population = algorithm.result();

    QualityIndicator hypervolume =
            new PISAHypervolume(
                    VectorUtils.readVectors("../resources/referenceFrontsCSV/LZ09_F6.csv", ","));

    // Rationale: the default problem is LZ09F6", and MOEA/D, configured with standard settings, should
    // return find a front with a hypervolume value higher than 0.35
    double hv = hypervolume.compute(SolutionListUtils.getMatrixWithObjectiveValues(population));

    assertTrue(hv > 0.35);

    JMetalRandom.getInstance().setSeed(System.currentTimeMillis());
  }
}
