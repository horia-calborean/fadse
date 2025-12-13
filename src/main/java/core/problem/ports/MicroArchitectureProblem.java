package core.problem.ports;

import core.model.individual.FadseIndividual;
import core.model.objectives.Objective;
import core.network.ClientsRepository;
import input.model.InputData;
import input.model.setup.CommonSetupParameters;
import input.ports.parameter.problem.ProblemParameter;
import org.uma.jmetal.problem.integerproblem.impl.AbstractIntegerProblem;
import org.uma.jmetal.solution.integersolution.IntegerSolution;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 * This version supports embarrassingly parallel evaluation:
 * - evaluate() only SENDS simulations (doesn't wait)
 * - Allows batching multiple solutions before waiting
 * - Proper for parallel evaluation in evolutionary algorithms
 * - Tracks individuals per solution for later aggregation
 * - Separate method for aggregation after batch completion
 */
public abstract class MicroArchitectureProblem extends AbstractIntegerProblem {

    private static final Logger LOGGER = Logger.getLogger(MicroArchitectureProblem.class.getName());

    protected final InputData inputData;
    private final int benchmarkCount;
    private final int objectiveCount;

    // Track individuals per solution for later aggregation
    private final Map<IntegerSolution, List<FadseIndividual>> solutionIndividualsMap;

    public MicroArchitectureProblem(InputData inputData) {
        // Validate input
        this.inputData = Objects.requireNonNull(inputData, "InputData cannot be null");

        // Validate and get design variables
        ProblemParameter<?>[] designVariables = validateAndGetDesignVariables();
        setRangeLimitsFrom(designVariables);

        // Validate and get objectives
        Map<String, Objective> objectives = validateAndGetObjectives();
        this.objectiveCount = objectives.size();
        numberOfObjectives(this.objectiveCount);

        // Validate and get benchmarks
        List<String> benchmarks = validateAndGetBenchmarks();
        this.benchmarkCount = benchmarks.size();

        // Initialize tracking map (thread-safe for parallel evaluation)
        this.solutionIndividualsMap = new ConcurrentHashMap<>();

        LOGGER.log(Level.INFO, String.format(
                "Initialized MicroArchitectureProblem: %d variables, %d objectives, %d benchmarks",
                designVariables.length, this.objectiveCount, this.benchmarkCount
        ));
    }

    /**
     * PARALLEL-FRIENDLY EVALUATION
     *
     * This method ONLY sends simulations - it does NOT wait for results!
     * This allows the caller to evaluate multiple solutions in parallel.
     *
     * Flow:
     * 1. Create individuals for all benchmarks
     * 2. Send all simulations (asynchronous)
     * 3. Store individuals for later aggregation
     * 4. Return immediately (no blocking!)
     *
     * The caller must:
     * 1. Call evaluate() for all solutions in population
     * 2. Call waitForAllSimulations()
     * 3. Call aggregateObjectives() for each solution
     */
    @Override
    public IntegerSolution evaluate(IntegerSolution integerSolution) {
        Objects.requireNonNull(integerSolution, "Solution cannot be null");

        LOGGER.log(Level.FINE, "Dispatching simulations for solution: " + integerSolution.hashCode());

        try {
            // Get validated data
            List<String> benchmarks = validateAndGetBenchmarks();
            ProblemParameter<?>[] designVariables = validateAndGetDesignVariables();
            List<Integer> optimizedValues = integerSolution.variables();

            // Validate variables count
            if (optimizedValues.size() != designVariables.length) {
                throw new IllegalStateException(String.format(
                        "Variable count mismatch: solution has %d, expected %d",
                        optimizedValues.size(), designVariables.length
                ));
            }

            // Create new design variables with optimized values
            ProblemParameter<?>[] newDesignVariables = getNewVariablesFrom(designVariables, optimizedValues);

            // Get ClientsRepository instance
            ClientsRepository clientsRepository = getClientsRepository();

            // Create and send individuals for all benchmarks
            List<FadseIndividual> individuals = new ArrayList<>(benchmarks.size());

            for (String benchmark : benchmarks) {
                FadseIndividual individual = createIndividual(benchmark, newDesignVariables);
                individuals.add(individual);

                // Send for simulation (asynchronous - no waiting!)
                clientsRepository.performSimulation(individual, integerSolution);

                LOGGER.log(Level.FINER, String.format(
                        "Dispatched individual for benchmark '%s': %s",
                        benchmark, individual.hashCode()
                ));
            }

            // Store individuals for later aggregation
            solutionIndividualsMap.put(integerSolution, individuals);

            LOGGER.log(Level.FINE, String.format(
                    "Dispatched %d simulations for solution %s (not waiting)",
                    individuals.size(), integerSolution.hashCode()
            ));

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to dispatch simulations for solution", e);
            // Set bad values immediately on dispatch failure
            setBadObjectiveValues(integerSolution);
        }

        // Return immediately without waiting for simulations!
        // This allows parallel evaluation of multiple solutions
        return integerSolution;
    }

    /**
     * Waits for all pending simulations to complete.
     *
     * This should be called AFTER evaluating all solutions in a population,
     * allowing all simulations to run in parallel.
     *
     * Call order:
     * 1. evaluate(solution1) - dispatches sims
     * 2. evaluate(solution2) - dispatches sims
     * 3. ... (dispatch all)
     * 4. waitForAllSimulations() - wait once for all
     * 5. aggregateObjectives(solution1) - aggregate
     * 6. aggregateObjectives(solution2) - aggregate
     * 7. ... (aggregate all)
     */
    public void waitForAllSimulations() {
        LOGGER.log(Level.INFO, "Waiting for all simulations to complete...");

        ClientsRepository clientsRepository = getClientsRepository();
        clientsRepository.join();

        LOGGER.log(Level.INFO, "All simulations completed");
    }

    /**
     * Aggregates objectives for a solution after simulations are complete.
     *
     * This should be called AFTER waitForAllSimulations() has returned.
     *
     * @param solution The solution to aggregate objectives for
     * @throws IllegalStateException if solution wasn't evaluated or simulations not complete
     */
    public void aggregateObjectives(IntegerSolution solution) {
        Objects.requireNonNull(solution, "Solution cannot be null");

        // Get individuals that were sent for this solution
        List<FadseIndividual> individuals = solutionIndividualsMap.get(solution);

        if (individuals == null) {
            LOGGER.log(Level.SEVERE, "No individuals found for solution - was evaluate() called?");
            setBadObjectiveValues(solution);
            return;
        }

        LOGGER.log(Level.FINE, "Aggregating objectives for solution: " + solution.hashCode());

        try {
            aggregateObjectivesInternal(solution, individuals);
            LOGGER.log(Level.FINE, "Solution objectives aggregated successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to aggregate objectives for solution", e);
            setBadObjectiveValues(solution);
        }
    }

    /**
     * Cleans up tracking data for a solution after objectives are aggregated.
     *
     * Call this after aggregateObjectives() to free memory.
     *
     * @param solution The solution to clean up
     */
    public void cleanupSolution(IntegerSolution solution) {
        solutionIndividualsMap.remove(solution);
    }

    /**
     * Cleans up all tracking data.
     *
     * Call this after processing an entire population.
     */
    public void cleanupAllSolutions() {
        solutionIndividualsMap.clear();
        LOGGER.log(Level.FINE, "Cleaned up all solution tracking data");
    }

    /**
     * Internal method to aggregate objectives (after simulations are complete).
     */
    private void aggregateObjectivesInternal(IntegerSolution solution, List<FadseIndividual> individuals) {
        // Initialize objective sums
        double[] objectiveSums = new double[objectiveCount];
        int validIndividualCount = 0;

        // Sum all objective values from all benchmarks
        for (FadseIndividual individual : individuals) {
            if (!individual.isFeasible()) {
                LOGGER.log(Level.WARNING, "Individual is not feasible: " + individual.hashCode());
                continue;
            }

            List<Objective> objectives = individual.getObjectives();

            // Validate objectives
            if (objectives == null || objectives.size() != objectiveCount) {
                LOGGER.log(Level.SEVERE, String.format(
                        "Individual has wrong number of objectives: expected %d, got %d",
                        objectiveCount, objectives != null ? objectives.size() : 0
                ));
                continue;
            }

            // Add to sum
            for (int j = 0; j < objectives.size(); j++) {
                Objective objective = objectives.get(j);
                if (objective == null) {
                    LOGGER.log(Level.SEVERE, "Null objective at index " + j);
                    continue;
                }

                double value = objective.getValue();

                // Validate value
                if (Double.isNaN(value) || Double.isInfinite(value)) {
                    LOGGER.log(Level.SEVERE, String.format(
                            "Invalid objective value: %f at index %d", value, j
                    ));
                    continue;
                }

                objectiveSums[j] += value;
            }

            validIndividualCount++;
        }

        // Calculate averages
        if (validIndividualCount == 0) {
            LOGGER.log(Level.SEVERE, "No valid individuals found, setting bad objective values");
            setBadObjectiveValues(solution);
            return;
        }

        if (validIndividualCount != benchmarkCount) {
            LOGGER.log(Level.WARNING, String.format(
                    "Only %d out of %d benchmarks produced valid results",
                    validIndividualCount, benchmarkCount
            ));
        }

        // Set averaged objectives
        for (int j = 0; j < objectiveCount; j++) {
            double average = objectiveSums[j] / validIndividualCount;
            solution.objectives()[j] = average;

            LOGGER.log(Level.FINER, String.format(
                    "Objective %d: sum=%.2f, average=%.2f",
                    j, objectiveSums[j], average
            ));
        }

        // CRITICAL: Validate that objectives are not zero after averaging
        // Zero objectives typically indicate failed simulations (clients never send [0, 0])
        boolean hasZeroObjective = false;
        for (int j = 0; j < objectiveCount; j++) {
            if (solution.objectives()[j] == 0.0) {
                hasZeroObjective = true;
                break;
            }
        }

        if (hasZeroObjective) {
            LOGGER.log(Level.SEVERE, String.format(
                    "Solution has zero objectives after aggregation (sum: %s, count: %d) - likely failed simulations. Setting to MAX_VALUE.",
                    java.util.Arrays.toString(objectiveSums), validIndividualCount
            ));
            setBadObjectiveValues(solution);
        }
    }

    /**
     * Creates a FadseIndividual with proper error handling
     */
    private FadseIndividual createIndividual(String benchmark, ProblemParameter<?>[] designVariables) {
        Objects.requireNonNull(benchmark, "Benchmark cannot be null");
        Objects.requireNonNull(designVariables, "Design variables cannot be null");

        try {
            FadseIndividual individual = new FadseIndividual(inputData, benchmark);
            individual.setParameters(designVariables);
            return individual;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to create individual for benchmark: " + benchmark, e);
            throw new RuntimeException("Failed to create individual", e);
        }
    }

    /**
     * Sets bad values for objectives when evaluation fails
     */
    protected void setBadObjectiveValues(IntegerSolution solution) {
        for (int j = 0; j < solution.objectives().length; j++) {
            solution.objectives()[j] = Double.MAX_VALUE;
        }
    }

    private ProblemParameter<?>[] validateAndGetDesignVariables() {
        Object designVariablesObj = inputData.get(CommonSetupParameters.DESIGN_VARIABLES);
        if (designVariablesObj == null) {
            throw new IllegalStateException("Design variables not configured");
        }
        if (!(designVariablesObj instanceof ProblemParameter[])) {
            throw new IllegalStateException("Design variables must be ProblemParameter[]");
        }
        @SuppressWarnings("unchecked")
        ProblemParameter<?>[] designVariables = (ProblemParameter<?>[]) designVariablesObj;
        if (designVariables.length == 0) {
            throw new IllegalStateException("No design variables configured");
        }
        return designVariables;
    }

    private Map<String, Objective> validateAndGetObjectives() {
        Object objectivesObj = inputData.get(CommonSetupParameters.OBJECTIVES);
        if (objectivesObj == null) {
            throw new IllegalStateException("Objectives not configured");
        }
        if (!(objectivesObj instanceof Map)) {
            throw new IllegalStateException("Objectives must be a Map");
        }
        @SuppressWarnings("unchecked")
        Map<String, Objective> objectives = (Map<String, Objective>) objectivesObj;
        if (objectives.isEmpty()) {
            throw new IllegalStateException("No objectives configured");
        }
        return objectives;
    }

    private List<String> validateAndGetBenchmarks() {
        Object benchmarksObj = inputData.get(CommonSetupParameters.BENCHMARKS);
        if (benchmarksObj == null) {
            throw new IllegalStateException("Benchmarks not configured");
        }
        if (!(benchmarksObj instanceof List)) {
            throw new IllegalStateException("Benchmarks must be a List");
        }
        @SuppressWarnings("unchecked")
        List<String> benchmarks = (List<String>) benchmarksObj;
        if (benchmarks.isEmpty()) {
            throw new IllegalStateException("No benchmarks configured");
        }
        return benchmarks;
    }

    private ClientsRepository getClientsRepository() {
        try {
            ClientsRepository repository = ClientsRepository.getInstance(inputData);
            if (repository == null) {
                throw new IllegalStateException("ClientsRepository.getInstance returned null");
            }
            return repository;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to get ClientsRepository instance", e);
            throw new IllegalStateException("Cannot access ClientsRepository", e);
        }
    }

    protected void setRangeLimitsFrom(ProblemParameter<?>[] designVariables) {
        Objects.requireNonNull(designVariables, "Design variables cannot be null");
        if (designVariables.length == 0) {
            throw new IllegalArgumentException("Design variables array is empty");
        }

        List<Integer> lowerLimits = new ArrayList<>(designVariables.length);
        List<Integer> upperLimits = new ArrayList<>(designVariables.length);

        IntStream.range(0, designVariables.length).forEach((i) -> {
            ProblemParameter<?> variable = designVariables[i];
            if (variable == null) {
                throw new IllegalStateException("Design variable at index " + i + " is null");
            }

            Object lowerBound = variable.getLowerBound();
            Object upperBound = variable.getUpperBound();
            if (lowerBound == null || upperBound == null) {
                throw new IllegalStateException("Design variable bounds cannot be null at index " + i);
            }
            if (!(lowerBound instanceof Number) || !(upperBound instanceof Number)) {
                throw new IllegalStateException("Design variable bounds must be Numbers at index " + i);
            }

            Integer lower = ((Number) lowerBound).intValue();
            Integer upper = ((Number) upperBound).intValue();
            if (lower > upper) {
                throw new IllegalStateException(String.format(
                        "Invalid bounds at index %d: lower (%d) > upper (%d)", i, lower, upper
                ));
            }

            lowerLimits.add(lower);
            upperLimits.add(upper);
        });

        variableBounds(lowerLimits, upperLimits);
    }

    protected ProblemParameter<?>[] getNewVariablesFrom(
            ProblemParameter<?>[] designVariables,
            List<Integer> optimizedValues) {

        Objects.requireNonNull(designVariables, "Design variables cannot be null");
        Objects.requireNonNull(optimizedValues, "Optimized values cannot be null");
        if (designVariables.length != optimizedValues.size()) {
            throw new IllegalArgumentException(String.format(
                    "Size mismatch: designVariables=%d, optimizedValues=%d",
                    designVariables.length, optimizedValues.size()
            ));
        }

        ProblemParameter<?>[] newDesignVariables = new ProblemParameter<?>[designVariables.length];

        for (int i = 0; i < designVariables.length; i++) {
            if (designVariables[i] == null) {
                throw new IllegalStateException("Design variable at index " + i + " is null");
            }
            newDesignVariables[i] = designVariables[i].clone();
            if (newDesignVariables[i] == null) {
                throw new IllegalStateException("Clone returned null at index " + i);
            }
            Integer value = optimizedValues.get(i);
            if (value == null) {
                throw new IllegalStateException("Optimized value at index " + i + " is null");
            }
            newDesignVariables[i].setValueFromDouble(value);
        }

        return newDesignVariables;
    }
}

