package core.algorithm.factory;

import core.algorithm.adapters.NSWOA;
import core.algorithm.adapters.WrappedEvolutionaryAlgorithm;
import input.model.InputData;
import input.model.setup.GapSetupParameters;
import org.uma.jmetal.algorithm.impl.AbstractEvolutionaryAlgorithm;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import java.util.HashMap;
import java.util.List;

/**
 * Factory for creating NSWOA algorithm instances.
 *
 * This factory creates NSWOA algorithm instances wrapped with WrappedEvolutionaryAlgorithm
 * for parallel evaluation support with MicroArchitectureProblem.
 *
 * The refactored NSWOA now follows the standard jMetal pattern and is fully compatible
 * with WrappedEvolutionaryAlgorithm, just like NSGA-II and other standard algorithms.
 *
 * Configuration parameters (from metaheuristic config):
 * - populationSize: Number of search agents (whales)
 * - maxEvaluations: Maximum number of evaluations (population size * generations)
 *
 * Example JSON configuration:
 * {
 *   "algorithmType": "NSWOA",
 *   "populationSize": 100,
 *   "maxEvaluations": 25000
 * }
 *
 * @param <S> Solution type (typically IntegerSolution for MicroArchitectureProblem)
 *
 * @author FADSE Team
 * @version 2.0 (Updated for refactored NSWOA)
 */
public class NSWOAAlgorithmFactory<S extends Solution<?>> implements AlgorithmFactoryInterface<S, List<S>> {

    /**
     * Create a WrappedEvolutionaryAlgorithm containing NSWOA for parallel execution.
     *
     * The refactored NSWOA follows the standard jMetal pattern, so it can be wrapped
     * just like NSGA-II or any other standard evolutionary algorithm.
     *
     * @param inputData Input configuration data containing metaheuristic parameters
     * @param problem The optimization problem to solve
     * @return WrappedEvolutionaryAlgorithm instance with NSWOA ready for execution
     * @throws IllegalArgumentException if required parameters are missing
     */
    @Override
    @SuppressWarnings("unchecked")
    public WrappedEvolutionaryAlgorithm<S, List<S>> create(InputData inputData, Problem<S> problem) {

        // Extract configuration parameters
        @SuppressWarnings("unchecked")
        HashMap<String, Object> metaheuristicData =
            (HashMap<String, Object>) inputData.get(GapSetupParameters.METAHEURISTIC_DATA);
        String csvPath = (String) inputData.get(GapSetupParameters.OUTPUT_PATH);

        // Validate required parameters
        if (metaheuristicData == null) {
            throw new IllegalArgumentException("Metaheuristic data is required");
        }
        if (csvPath == null) {
            throw new IllegalArgumentException("Output path is required");
        }

        // Get algorithm parameters
        Integer populationSize = (Integer) metaheuristicData.get("populationSize");
        Integer maxEvaluations = (Integer) metaheuristicData.get("maxEvaluations");

        // Validate parameters
        if (populationSize == null) {
            throw new IllegalArgumentException("populationSize is required in metaheuristic data");
        }
        if (maxEvaluations == null) {
            throw new IllegalArgumentException("maxEvaluations is required in metaheuristic data");
        }
        if (populationSize <= 0) {
            throw new IllegalArgumentException("populationSize must be positive, got: " + populationSize);
        }
        if (maxEvaluations <= 0) {
            throw new IllegalArgumentException("maxEvaluations must be positive, got: " + maxEvaluations);
        }

        // Log configuration
        System.out.println("Creating NSWOA algorithm with configuration:");
        System.out.println("  Population size: " + populationSize);
        System.out.println("  Max evaluations: " + maxEvaluations);
        System.out.println("  Output path: " + csvPath);
        System.out.println("  Problem: " + problem.name());
        System.out.println("  Objectives: " + problem.numberOfObjectives());
        System.out.println("  Variables: " + problem.numberOfVariables());

        // Create the NSWOA algorithm (refactored to follow jMetal pattern)
        AbstractEvolutionaryAlgorithm<S, List<S>> nswoa = new NSWOA<>(
            problem,
            new SequentialSolutionListEvaluator<>(),  // Will be intercepted by wrapper
            maxEvaluations,
            populationSize
        );

        // Wrap it with WrappedEvolutionaryAlgorithm for parallel evaluation
        // This now works perfectly because NSWOA follows the standard jMetal pattern!
        return new WrappedEvolutionaryAlgorithm<>(nswoa, csvPath, inputData);
    }

    /**
     * Get the name of the algorithm this factory creates.
     *
     * @return Algorithm name
     */
    public String getAlgorithmName() {
        return "NSWOA";
    }

    /**
     * Get a description of the algorithm this factory creates.
     *
     * @return Algorithm description
     */
    public String getAlgorithmDescription() {
        return "Non-dominated Sorting Whale Optimization Algorithm (Refactored for jMetal compatibility)";
    }
}

