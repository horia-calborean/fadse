package jmetal.algorithm.tests.artificialdecisionmaker;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.Ignore;
import org.junit.Test;
import jmetal.core.algorithm.Algorithm;
import jmetal.algorithm.multiobjective.wasfga.WASFGA;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.crossover.impl.SBXCrossover;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.operator.mutation.impl.PolynomialMutation;
import jmetal.core.operator.selection.SelectionOperator;
import jmetal.core.operator.selection.impl.BinaryTournamentSelection;
import jmetal.core.problem.Problem;
import jmetal.problem.multiobjective.dtlz.DTLZ1;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.artificialdecisionmaker.InteractiveAlgorithm;
import jmetal.core.util.artificialdecisionmaker.impl.ArtificialDecisionMakerDecisionTree;
import jmetal.core.util.artificialdecisionmaker.impl.ArtificiallDecisionMakerBuilder;
import jmetal.core.util.comparator.RankingAndCrowdingDistanceComparator;
import jmetal.core.util.evaluator.impl.SequentialSolutionListEvaluator;
import jmetal.core.util.point.impl.IdealPoint;
import jmetal.core.util.point.impl.NadirPoint;

public class ArtificiallDecisionMakerIT {
  Algorithm<List<DoubleSolution>> algorithm;

  @Ignore
  @Test
  public void shouldTheAlgorithmReturnANumberOfSolutionsWhenSolvingASimpleProblem() throws Exception {
    Problem<DoubleSolution> problem;
    InteractiveAlgorithm<DoubleSolution, List<DoubleSolution>> algorithmRun;
    CrossoverOperator<DoubleSolution> crossover;
    MutationOperator<DoubleSolution> mutation;
    SelectionOperator<List<DoubleSolution>, DoubleSolution> selection;
    int numberIterations = 1;
    int numberObjectives = 3;
    int numberVariables = 7;
    String weightsName = "";
    int populationSize = 100;

    problem = new DTLZ1(numberVariables, numberObjectives);

    double crossoverProbability = 0.9;
    double crossoverDistributionIndex = 20.0;
    crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);

    double mutationProbability = 1.0 / problem.numberOfVariables();
    double mutationDistributionIndex = 20.0;
    mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

    selection = new BinaryTournamentSelection<DoubleSolution>(
        new RankingAndCrowdingDistanceComparator<DoubleSolution>());

    IdealPoint idealPoint = new IdealPoint(problem.numberOfObjectives());
    idealPoint.update(problem.createSolution().objectives());
    NadirPoint nadirPoint = new NadirPoint(problem.numberOfObjectives());
    nadirPoint.update(problem.createSolution().objectives());
    double considerationProbability = 0.1;
    List<Double> rankingCoeficient = new ArrayList<>();
    for (int i = 0; i < problem.numberOfObjectives(); i++) {
      rankingCoeficient.add(1.0 / problem.numberOfObjectives());
    }

    for (int cont = 0; cont < numberIterations; cont++) {
      List<Double> referencePoint = new ArrayList<>();

      double epsilon = 0.01;
      List<Double> asp = new ArrayList<>();
      for (int i = 0; i < problem.numberOfObjectives(); i++) {
        asp.add(0.0);//initialize asp to ideal
        referencePoint.add(0.0);//initialization
      }

      algorithmRun = new WASFGA<>(problem, populationSize, 200, crossover, mutation,
          selection, new SequentialSolutionListEvaluator<>(), epsilon, referencePoint, weightsName);

      algorithm = new ArtificiallDecisionMakerBuilder<>(problem, algorithmRun)
          .setConsiderationProbability(considerationProbability)
          .setMaxEvaluations(11)
          .setTolerance(0.001)
          .setAsp(asp)
          .build();
      algorithm.run();

      List<Double> referencePoints = ((ArtificialDecisionMakerDecisionTree<DoubleSolution>) algorithm)
          .getReferencePoints();

      assertTrue(referencePoints.size() >= numberObjectives * numberIterations);
    }
  }

}
