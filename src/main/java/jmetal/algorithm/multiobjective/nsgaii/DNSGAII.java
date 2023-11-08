package jmetal.algorithm.multiobjective.nsgaii;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.operator.selection.SelectionOperator;
import jmetal.core.operator.selection.impl.RankingAndDirScoreSelection;
import jmetal.core.problem.Problem;
import jmetal.core.solution.Solution;
import jmetal.core.util.evaluator.SolutionListEvaluator;

/**
 * Created at 10:45 am, 2019/1/29 This algorithm (DIR-Enhanced NSGA-II [DNSGA-II]) is according to
 * "Cai X, Sun H, Fan Z. A diversity indicator based on reference vectors for many-objective
 * optimization[J]. Information Sciences, 2018, 430-431:467-486.".
 *
 * @author sunhaoran <nuaa_sunhr@yeah.net>
 */
@SuppressWarnings("serial")
public class DNSGAII<S extends Solution<?>> extends NSGAII<S> {

  private double[][] referenceVectors;

  public DNSGAII(
      Problem<S> problem,
      int maxEvaluations,
      int populationSize,
      int matingPoolSize,
      int offspringPopulationSize,
      CrossoverOperator<S> crossoverOperator,
      MutationOperator<S> mutationOperator,
      SelectionOperator<List<S>, S> selectionOperator,
      Comparator<S> dominanceComparator,
      SolutionListEvaluator<S> evaluator) {
    super(
        problem,
        maxEvaluations,
        populationSize,
        matingPoolSize,
        offspringPopulationSize,
        crossoverOperator,
        mutationOperator,
        selectionOperator,
        dominanceComparator,
        evaluator);
  }

  public void setReferenceVectors(double[][] referenceVectors) {
    this.referenceVectors = referenceVectors;
  }

  @Override
  protected List<S> replacement(List<S> population, List<S> offspringPopulation) {
    List<S> jointPopulation = new ArrayList<>();
    jointPopulation.addAll(population);
    jointPopulation.addAll(offspringPopulation);
    RankingAndDirScoreSelection<S> rankingAndDirScoreSelection =
        new RankingAndDirScoreSelection<>(
            getMaxPopulationSize(), dominanceComparator, referenceVectors);
    return rankingAndDirScoreSelection.execute(jointPopulation);
  }

  @Override
  public String name() {
    return "D-NSGA-II";
  }

  @Override
  public String description() {
    return "DIR based NSGA-II";
  }
}
