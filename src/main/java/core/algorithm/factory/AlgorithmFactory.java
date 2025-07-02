package core.algorithm.factory;

import core.algorithm.adapters.WrappedEvolutionaryAlgorithm;
import input.model.InputData;
import input.model.setup.GapSetupParameters;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;

import java.util.HashMap;
import java.util.Map;

public class AlgorithmFactory {

    private static final HashMap<String, AlgorithmFactoryInterface<? extends Solution<?>, ?>> factories = new HashMap<>();

    static {
        factories.put("NSGAII", new NSGAIIAlgorithmFactory<>());
        factories.put("NSGAIII", new NSGAIIIAlgorithmFactory<>());
        //factories.put("CNSGAII", new CNSGAIIAlgorithmFactory());
        // Add more: factories.put("SPEA2", new SPEA2AlgorithmFactory()); etc.
    }

    public static <S extends Solution<?>, R> WrappedEvolutionaryAlgorithm<S, R> createAlgorithm(InputData inputData, Problem<S> problem) {
        @SuppressWarnings("unchecked")
        Map<String, Object> metaheuristic = (HashMap<String, Object>) inputData.get(GapSetupParameters.METAHEURISTIC);
        String name = (String) metaheuristic.get("name");

        @SuppressWarnings("unchecked")
        AlgorithmFactoryInterface<S, R> factory = (AlgorithmFactoryInterface<S, R>) factories.get(name.toUpperCase());
        if (factory == null) {
            throw new IllegalArgumentException("Unknown algorithm: " + name);
        }
        return factory.create(inputData, problem);
    }

}
