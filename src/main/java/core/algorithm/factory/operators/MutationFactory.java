package core.algorithm.factory.operators;

import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.BitFlipMutation;
import org.uma.jmetal.operator.mutation.impl.PolynomialMutation;
import org.uma.jmetal.solution.Solution;

/**
 * Factory for creating mutation operators.
 * Note: Caller must ensure the selected mutation is compatible with the solution type:
 * - PolynomialMutation: use with DoubleSolution
 * - BitFlipMutation: use with BinarySolution
 * etc
 */
@SuppressWarnings("unchecked")
public class MutationFactory implements MutationFactoryInterface {

    @Override
    public <S extends Solution<?>> MutationOperator<S> create(String type, double probability, double distributionIndex) {
        return switch (type.trim()) {
            case "PolynomialMutation" -> (MutationOperator<S>) new PolynomialMutation(probability, distributionIndex);
            case "BitFlipMutation" -> (MutationOperator<S>) new BitFlipMutation(probability);
            default -> throw new IllegalArgumentException("Unsupported mutation operator: " + type);
        };
    }
}
