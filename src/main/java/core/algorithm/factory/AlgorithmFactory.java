package core.algorithm.factory;

import core.algorithm.adapters.WrappedEvolutionaryAlgorithm;
import input.model.InputData;
import org.uma.jmetal.problem.Problem;

public interface AlgorithmFactory {
    WrappedEvolutionaryAlgorithm<?, ?> create(InputData inputData, Problem problem);

}
