package jmetal.algorithm.singleobjective.evolutionstrategy;

import jmetal.core.algorithm.Algorithm;
import jmetal.core.algorithm.AlgorithmBuilder;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.problem.Problem;
import jmetal.core.solution.Solution;
import jmetal.core.util.errorchecking.JMetalException;

/**
 * Class implementing a (mu , lambda) Evolution Strategy (lambda must be divisible by mu)
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class EvolutionStrategyBuilder<S extends Solution<?>> implements AlgorithmBuilder<Algorithm<S>> {
  public enum EvolutionStrategyVariant {ELITIST, NON_ELITIST}
  private Problem<S> problem;
  private int mu;
  private int lambda;
  private int maxEvaluations;
  private MutationOperator<S> mutation;
  private EvolutionStrategyVariant variant ;

  public EvolutionStrategyBuilder(Problem<S> problem, MutationOperator<S> mutationOperator,
      EvolutionStrategyVariant variant) {
    this.problem = problem;
    this.mu = 1;
    this.lambda = 10;
    this.maxEvaluations = 250000;
    this.mutation = mutationOperator;
    this.variant = variant ;
  }

  public EvolutionStrategyBuilder<S> setMu(int mu) {
    this.mu = mu;

    return this;
  }

  public EvolutionStrategyBuilder<S> setLambda(int lambda) {
    this.lambda = lambda;

    return this;
  }

  public EvolutionStrategyBuilder<S> setMaxEvaluations(int maxEvaluations) {
    this.maxEvaluations = maxEvaluations;

    return this;
  }

  @Override public Algorithm<S> build() {
    if (variant == EvolutionStrategyVariant.ELITIST) {
      return new ElitistEvolutionStrategy<>(problem, mu, lambda, maxEvaluations, mutation);
    } else if (variant == EvolutionStrategyVariant.NON_ELITIST) {
      return new NonElitistEvolutionStrategy<>(problem, mu, lambda, maxEvaluations, mutation);
    } else {
      throw new JMetalException("Unknown variant: " + variant) ;
    }
  }

  /* Getters */
  public int getMu() {
    return mu;
  }

  public int getLambda() {
    return lambda;
  }

  public int getMaxEvaluations() {
    return maxEvaluations;
  }

  public MutationOperator<S> getMutation() {
    return mutation;
  }
}
