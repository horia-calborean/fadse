package core.algorithm.factory.operators;

import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;

public interface CrossoverFactoryInterface
{
    CrossoverOperator<DoubleSolution> create(String type, double probability, double distributionIndex);

}
