package core.algorithm.factory.operators;

import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.BitFlipMutation;
import org.uma.jmetal.operator.mutation.impl.PolynomialMutation;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;

public class MutationFactory implements MutationFactoryInterface{
    @Override
    public MutationOperator<DoubleSolution> create(String type, double probability, double distributionIndex) {
        switch (type.trim()) {
            case "PolynomialMutation":
                return new PolynomialMutation(probability, distributionIndex);
            // Add more mutation operators as needed
            case "BitFlipMutation":
                return new BitFlipMutation(probability);
            default:
                throw new IllegalArgumentException("Unsupported mutation operator: " + type);
        }
    }
}
