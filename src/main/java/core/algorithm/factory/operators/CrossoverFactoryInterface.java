package core.algorithm.factory.operators;

import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.solution.Solution;

public interface CrossoverFactoryInterface {
    <S extends Solution<?>> CrossoverOperator<S> create(String type, double probability, double distributionIndex);
}
