package core.algorithm.factory.operators;

import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.SBXCrossover;
import org.uma.jmetal.operator.crossover.impl.SinglePointCrossover;
import org.uma.jmetal.solution.Solution;

/**
 * Factory for creating crossover operators.
 *
 * Note:
 * - SBXCrossover is intended for DoubleSolution
 * - SinglePointCrossover is intended for BinarySolution
 *
 * The caller must ensure the correct combination of operator and solution type.
 */
@SuppressWarnings("unchecked")
public class CrossoverFactory implements CrossoverFactoryInterface {

    @Override
    public <S extends Solution<?>> CrossoverOperator<S> create(String type, double probability, double distributionIndex) {
        return switch (type.trim()) {
            case "SBXCrossover" ->
                    (CrossoverOperator<S>) new SBXCrossover(probability, distributionIndex);

            case "SinglePointCrossover" ->
                    (CrossoverOperator<S>) new SinglePointCrossover(probability);

            default -> throw new IllegalArgumentException("Unsupported crossover operator: " + type);
        };
    }
}
