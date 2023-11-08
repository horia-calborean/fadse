package jmetal.algorithm.multiobjective.gwasfga;

import java.util.List;
import jmetal.algorithm.multiobjective.gwasfga.util.GWASFGARanking;
import jmetal.algorithm.multiobjective.mombi.util.ASFWASFGA;
import jmetal.algorithm.multiobjective.mombi.util.AbstractUtilityFunctionsSet;
import jmetal.algorithm.multiobjective.wasfga.WASFGA;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.operator.selection.SelectionOperator;
import jmetal.core.problem.Problem;
import jmetal.core.solution.Solution;
import jmetal.core.util.evaluator.SolutionListEvaluator;
import jmetal.core.util.ranking.Ranking;

/**
 * This class executes the GWASFGA algorithm described in:
 * Saborido, R., Ruiz, A. B. and Luque, M. (2015). Global WASF-GA: An Evolutionary Algorithm in
 * Multiobjective Optimization to Approximate the whole Pareto Optimal Front.
 * Evolutionary Computation Accepted for publication.
 *
 * @author Juanjo Durillo
 */
public class GWASFGA<S extends Solution<?>> extends WASFGA<S> {
  private final AbstractUtilityFunctionsSet<S> achievementScalarizingUtopia;
  private final AbstractUtilityFunctionsSet<S> achievementScalarizingNadir;
  private static final long serialVersionUID = 1L;

  public GWASFGA(Problem<S> problem, int populationSize, int maxIterations, CrossoverOperator<S> crossoverOperator,
                 MutationOperator<S> mutationOperator, SelectionOperator<List<S>, S> selectionOperator,
                 SolutionListEvaluator<S> evaluator, double epsilon, String weightVectorsFileName) {
    super(problem, populationSize, maxIterations, crossoverOperator, mutationOperator, selectionOperator, evaluator, epsilon,
            null, weightVectorsFileName);

    setMaxPopulationSize(populationSize);

    int halfVectorSize = super.weights.length  / 2;
    int evenVectorsSize    = (super.weights.length%2==0) ? halfVectorSize : (halfVectorSize+1);

    double [][] evenVectors = new double[evenVectorsSize][getProblem().numberOfObjectives()];
    double [][] oddVectors = new double[halfVectorSize][getProblem().numberOfObjectives()];

    int index = 0;
    for (int i = 0; i < super.weights.length; i = i + 2)
      evenVectors[index++] = super.weights[i];

    index = 0;
    for (int i = 1; i < super.weights.length; i = i + 2)
      oddVectors[index++] = super.weights[i];

    this.achievementScalarizingNadir  =  createUtilityFunction(this.getNadirPoint(), evenVectors);
    this.achievementScalarizingUtopia =  createUtilityFunction(this.getReferencePoint(), oddVectors);
  }

  public GWASFGA(Problem<S> problem, int populationSize, int maxIterations, CrossoverOperator<S> crossoverOperator,
                 MutationOperator<S> mutationOperator, SelectionOperator<List<S>, S> selectionOperator,
                 SolutionListEvaluator<S> evaluator, double epsilon) {
    this(problem, populationSize, maxIterations, crossoverOperator, mutationOperator, selectionOperator, evaluator, epsilon,
             "");
  }

  private AbstractUtilityFunctionsSet<S> createUtilityFunction(List<Double> referencePoint, double[][] weights) {
    return new ASFWASFGA<>(weights,referencePoint);
  }

  protected Ranking<S> computeRanking(List<S> solutionList) {
    Ranking<S> ranking = new GWASFGARanking<>(this.achievementScalarizingUtopia, this.achievementScalarizingNadir);
    ranking.compute(solutionList);
    return ranking;
  }

  @Override public String name() {
    return "GWASFGA" ;
  }

  @Override public String description() {
    return "Global Weighting Achievement Scalarizing Function Genetic Algorithm" ;
  }
}
