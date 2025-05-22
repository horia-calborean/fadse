package core.algorithm.factory.operators;

import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.SBXCrossover;
import org.uma.jmetal.operator.crossover.impl.SinglePointCrossover;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;

public class CrossoverFactory implements CrossoverFactoryInterface{

    @Override
    public CrossoverOperator<DoubleSolution> create(String type, double probability, double distributionIndex) {
        switch (type.trim()) {
            case "SBXCrossover":
                return new SBXCrossover(probability, distributionIndex);
            case "SinglePointCrossover":
                return new SinglePointCrossover(probability);
            default:
                //TODO: add every crossover possibility and watch out for parameters -> George
                throw new IllegalArgumentException("Unsupported crossover operator: " + type);
        }
    }
}
