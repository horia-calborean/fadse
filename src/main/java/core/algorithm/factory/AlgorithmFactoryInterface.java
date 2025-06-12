package core.algorithm.factory;

import core.algorithm.adapters.WrappedEvolutionaryAlgorithm;
import input.model.InputData;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;

public interface AlgorithmFactoryInterface<S extends Solution<?>, R> {
    WrappedEvolutionaryAlgorithm<S, R> create(InputData inputData, Problem<S> problem);
}
