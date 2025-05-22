package core.algorithm.factory.operators;

import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;

public interface MutationFactoryInterface {
    MutationOperator<DoubleSolution> create(String type, double probability, double distributionIndex);

}
