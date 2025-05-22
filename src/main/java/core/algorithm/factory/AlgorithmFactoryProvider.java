package core.algorithm.factory;

import core.algorithm.adapters.WrappedEvolutionaryAlgorithm;
import input.model.InputData;
import input.model.setup.GapSetupParameters;
import org.uma.jmetal.algorithm.impl.AbstractEvolutionaryAlgorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.algorithm.multiobjective.spea2.SPEA2Builder;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.SBXCrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.PolynomialMutation;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;

import java.util.HashMap;
import java.util.List;

public class AlgorithmFactoryProvider {

    private static final HashMap<String, AlgorithmFactory> factories = new HashMap<>();

    static {
        factories.put("NSGAII", new NSGAIIAlgorithmFactory());
        factories.put("CNSGAII", new CNSGAIIAlgorithmFactory());
        // Add more: factories.put("SPEA2", new SPEA2AlgorithmFactory()); etc.
    }

    public static WrappedEvolutionaryAlgorithm<?, ?> getAlgorithm(InputData inputData, Problem problem) {
        HashMap<String, Object> metaheuristic = (HashMap<String, Object>) inputData.get(GapSetupParameters.METAHEURISTIC);
        String name = (String) metaheuristic.get("name");

        AlgorithmFactory factory = factories.get(name.toUpperCase());
        if (factory == null) {
            throw new IllegalArgumentException("Unknown algorithm: " + name);
        }
        return factory.create(inputData, problem);
    }

//    public static AbstractEvolutionaryAlgorithm<DoubleSolution, List<DoubleSolution>> createAlgorithm(InputData inputData, Problem<DoubleSolution> problem) {
//        String algorithmName = (String) ((HashMap) inputData.get(GapSetupParameters.METAHEURISTIC)).get("name");
//
//        switch (algorithmName.toUpperCase()) {
//            case "NSGAII":
//                return createNSGAII(inputData, problem);
//            case "SPEA2":
//                return createSPEA2(inputData, problem);
//            default:
//                throw new IllegalArgumentException("Unsupported algorithm: " + algorithmName);
//        }
//    }

//    private static AbstractEvolutionaryAlgorithm<DoubleSolution, List<DoubleSolution>> createCNSGAII(InputData inputData, Problem<DoubleSolution> problem) {
//        int populationSize = getInt(inputData, "populationSize", 100);
//        int generations = getInt(inputData, "numberOfGenerations", 250);
//        double crossoverProbability = getDouble(inputData, "crossoverProbability", 0.9);
//        double crossoverDistributionIndex = getDouble(inputData, "crossoverDistributionIndex", 20.0);
//        double mutationDistributionIndex = getDouble(inputData, "mutationDistributionIndex", 20.0);
//        double mutationProbability = 1.0 / problem.numberOfVariables();
//
//        CrossoverOperator<DoubleSolution> crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);
//        MutationOperator<DoubleSolution> mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);
//        SelectionOperator<List<DoubleSolution>, DoubleSolution> selection =
//                new BinaryTournamentSelection<>(new RankingAndCrowdingDistanceComparator<>());
//
//        return new NSGAIIBuilder<>(problem, crossover, mutation, populationSize)
//                .setSelectionOperator(selection)
//                .setMaxEvaluations(populationSize * generations)
//                .build();
//    }
//
//    private static AbstractEvolutionaryAlgorithm<DoubleSolution, List<DoubleSolution>> createNSGAII(InputData inputData, Problem<DoubleSolution> problem) {
//        int populationSize = getInt(inputData, "populationSize", 100);
//        int generations = getInt(inputData, "numberOfGenerations", 250);
//        double crossoverProbability = getDouble(inputData, "crossoverProbability", 0.9);
//        double crossoverDistributionIndex = getDouble(inputData, "crossoverDistributionIndex", 20.0);
//        double mutationDistributionIndex = getDouble(inputData, "mutationDistributionIndex", 20.0);
//        double mutationProbability = 1.0 / problem.numberOfVariables();
//
//        CrossoverOperator<DoubleSolution> crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);
//        MutationOperator<DoubleSolution> mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);
//        SelectionOperator<List<DoubleSolution>, DoubleSolution> selection =
//                new BinaryTournamentSelection<>(new RankingAndCrowdingDistanceComparator<>());
//
//        return new NSGAIIBuilder<>(problem, crossover, mutation, populationSize)
//                .setSelectionOperator(selection)
//                .setMaxEvaluations(populationSize * generations)
//                .build();
//    }
//
//    private static AbstractEvolutionaryAlgorithm<DoubleSolution, List<DoubleSolution>> createSPEA2(InputData inputData, Problem<DoubleSolution> problem) {
//        int populationSize = getInt(inputData, "populationSize", 100);
//        int generations = getInt(inputData, "numberOfGenerations", 250);
//        double crossoverProbability = getDouble(inputData, "crossoverProbability", 0.9);
//        double crossoverDistributionIndex = getDouble(inputData, "crossoverDistributionIndex", 20.0);
//        double mutationDistributionIndex = getDouble(inputData, "mutationDistributionIndex", 20.0);
//        double mutationProbability = 1.0 / problem.numberOfVariables();
//
//        CrossoverOperator<DoubleSolution> crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);
//        MutationOperator<DoubleSolution> mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);
//        SelectionOperator<List<DoubleSolution>, DoubleSolution> selection =
//                new BinaryTournamentSelection<>(new RankingAndCrowdingDistanceComparator<>());
//
//        return new SPEA2Builder<>(problem, crossover, mutation)
//                .setSelectionOperator(selection)
//                .setMaxIterations(generations)
//                .setPopulationSize(populationSize)
//                .build();
//    }
//
//    // Utility methods for type-safe parameter extraction with defaults
//    private static int getInt(InputData inputData, String key, int defaultValue) {
//        Object value = "";//inputData.get(key);
//        return (value instanceof Number) ? ((Number) value).intValue() : defaultValue;
//    }
//
//    private static double getDouble(InputData inputData, String key, double defaultValue) {
//        Object value = "";//inputData.get(key);
//        return (value instanceof Number) ? ((Number) value).doubleValue() : defaultValue;
//    }
}
