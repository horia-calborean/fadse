package core.algorithm.factory;

import core.algorithm.adapters.WrappedEvolutionaryAlgorithm;
import input.model.InputData;
import input.model.setup.GapSetupParameters;
import org.uma.jmetal.problem.Problem;

import java.util.HashMap;

public class AlgorithmFactory {

    private static final HashMap<String, AlgorithmFactoryInterface> factories = new HashMap<>();

    static {
        factories.put("NSGAII", new NSGAIIAlgorithmFactory());
        factories.put("NSGAIII", new NSGAIIIAlgorithmFactory());
        //factories.put("CNSGAII", new CNSGAIIAlgorithmFactory());
        // Add more: factories.put("SPEA2", new SPEA2AlgorithmFactory()); etc.
    }

    public static WrappedEvolutionaryAlgorithm<?, ?> createAlgorithm(InputData inputData, Problem problem) {
        HashMap<String, Object> metaheuristic = (HashMap<String, Object>) inputData.get(GapSetupParameters.METAHEURISTIC);
        String name = (String) metaheuristic.get("name");

        AlgorithmFactoryInterface factory = factories.get(name.toUpperCase());
        if (factory == null) {
            throw new IllegalArgumentException("Unknown algorithm: " + name);
        }
        return factory.create(inputData, problem);
    }

}
