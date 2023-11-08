package jmetal.algorithm.tests.smsemoa;

import static org.junit.Assert.assertTrue;

import java.util.List;

import jmetal.algorithm.multiobjective.smsemoa.SMSEMOABuilder;
import org.junit.Test;
import jmetal.core.algorithm.Algorithm;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.crossover.impl.SBXCrossover;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.operator.mutation.impl.PolynomialMutation;
import jmetal.core.operator.selection.impl.RandomSelection;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.problem.multiobjective.zdt.ZDT1;
import jmetal.problem.multiobjective.zdt.ZDT4;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.legacy.qualityindicator.QualityIndicator;
import jmetal.core.util.legacy.qualityindicator.impl.hypervolume.Hypervolume;
import jmetal.core.util.legacy.qualityindicator.impl.hypervolume.impl.PISAHypervolume;

public class SMSEMOAIT {
  Algorithm<List<DoubleSolution>> algorithm;

  @Test
  public void shouldTheAlgorithmReturnANumberOfSolutionsWhenSolvingASimpleProblem()
      throws Exception {
    ZDT4 problem = new ZDT4();
    CrossoverOperator<DoubleSolution> crossover;
    MutationOperator<DoubleSolution> mutation;

    double crossoverProbability = 0.9;
    double crossoverDistributionIndex = 20.0;
    crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);

    double mutationProbability = 1.0 / problem.numberOfVariables();
    double mutationDistributionIndex = 20.0;
    mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

    Hypervolume<DoubleSolution> hypervolumeImplementation;
    hypervolumeImplementation = new PISAHypervolume<>();
    hypervolumeImplementation.setOffset(100.0);

    algorithm =
        new SMSEMOABuilder<>(problem, crossover, mutation)
            .setSelectionOperator(new RandomSelection<DoubleSolution>())
            .setMaxEvaluations(25000)
            .setPopulationSize(100)
            .setHypervolumeImplementation(hypervolumeImplementation)
            .build();

    algorithm.run();

    List<DoubleSolution> population = algorithm.result();

    /*
    Rationale: the default problem is ZDT4, and usually SMSEMOA, configured with standard
    settings, should return 100 solutions
    */
    assertTrue(population.size() >= 98);
  }

  @Test
  public void shouldTheHypervolumeHaveAMinimumValue() throws Exception {
    DoubleProblem problem = new ZDT1();

    CrossoverOperator<DoubleSolution> crossover;
    MutationOperator<DoubleSolution> mutation;

    double crossoverProbability = 0.9;
    double crossoverDistributionIndex = 20.0;
    crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);

    double mutationProbability = 1.0 / problem.numberOfVariables();
    double mutationDistributionIndex = 20.0;
    mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

    Hypervolume<DoubleSolution> hypervolumeImplementation;
    hypervolumeImplementation = new PISAHypervolume<>();
    hypervolumeImplementation.setOffset(100.0);

    algorithm =
        new SMSEMOABuilder<>(problem, crossover, mutation)
            .setSelectionOperator(new RandomSelection<DoubleSolution>())
            .setMaxEvaluations(25000)
            .setPopulationSize(100)
            .setHypervolumeImplementation(hypervolumeImplementation)
            .build();

    algorithm.run();

    List<DoubleSolution> population = algorithm.result();

    QualityIndicator<List<DoubleSolution>, Double> hypervolume =
        new PISAHypervolume<>("../resources/referenceFrontsCSV/ZDT1.csv");

    // Rationale: the default problem is ZDT1, and SMSEMOA, configured with standard settings,
    // should
    // return find a front with a hypervolume value higher than 0.65

    double hv = (Double) hypervolume.evaluate(population);

    assertTrue(hv > 0.65);
  }
}
