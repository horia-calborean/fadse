package jmetal.core.operator.crossover.impl;

import java.util.List;
import jmetal.core.operator.crossover.CrossoverOperator;
import jmetal.core.solution.Solution;

/**
 * Created by FlapKap on 27-05-2017.
 */
@SuppressWarnings("serial")
public class TwoPointCrossover<T> implements CrossoverOperator<Solution<T>> {
  NPointCrossover<T> operator;

  public TwoPointCrossover(double probability) {
    this.operator = new NPointCrossover<>(probability, 2);
  }

  @Override
  public List<Solution<T>> execute(List<Solution<T>> solutions) {
    return operator.execute(solutions);
  }

  @Override
  public double crossoverProbability() {
    return operator.crossoverProbability() ;
  }

  @Override
  public int numberOfRequiredParents() {
    return operator.numberOfRequiredParents();
  }

  @Override
  public int numberOfGeneratedChildren() {
    return 2;
  }
}
