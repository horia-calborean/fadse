package core.algorithm.adapters;

import core.network.ClientsRepository;
import core.problem.ports.MicroArchitectureProblem;
import core.qualityindicator.EpsilonIndicator;
import core.qualityindicator.HypervolumeIndicator;
import core.qualityindicator.SpreadIndicator;
import input.model.InputData;
import org.uma.jmetal.algorithm.impl.AbstractEvolutionaryAlgorithm;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.util.SolutionListUtils;
import output.application.CsvUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TRULY PARALLEL wrapper for evolutionary algorithms.
 *
 * This version implements true embarrassingly parallel evaluation:
 * - Dispatches ALL simulations for ALL solutions first
 * - Waits ONCE for all simulations to complete
 * - Aggregates ALL results after completion
 *
 * Works with any Problem type, but optimized for MicroArchitectureProblem.
 */
public class WrappedEvolutionaryAlgorithm<S, R> extends AbstractEvolutionaryAlgorithm<S, R> {

    private static final Logger LOGGER = Logger.getLogger(WrappedEvolutionaryAlgorithm.class.getName());

    protected final AbstractEvolutionaryAlgorithm<S, R> algorithm;
    protected final Map<String, Method> methodsDictionary;
    protected final ClientsRepository clientsRepository;
    protected final String csvPath;
    protected final InputData inputData;

    /**
     * Creates a wrapped evolutionary algorithm with parallel evaluation support.
     *
     * The problem is automatically extracted from the algorithm instance.
     *
     * @param algorithm The jMetal algorithm to wrap (must have a problem configured)
     * @param path Directory path for output files
     * @param inputData Configuration data
     * @throws IllegalArgumentException if any parameter is null
     * @throws IllegalStateException if problem cannot be extracted from algorithm
     */
    public WrappedEvolutionaryAlgorithm(
            AbstractEvolutionaryAlgorithm<S, R> algorithm,
            String path,
            InputData inputData) {

        // Validate inputs
        this.algorithm = Objects.requireNonNull(algorithm, "Algorithm cannot be null");
        Objects.requireNonNull(path, "Path cannot be null");
        this.inputData = Objects.requireNonNull(inputData, "InputData cannot be null");

        this.csvPath = path + "fadse.xlsx";
        this.methodsDictionary = getMethods(algorithm);

        // Initialize ClientsRepository
        try {
            this.clientsRepository = ClientsRepository.getInstance(inputData);
            if (clientsRepository == null) {
                throw new IllegalStateException("ClientsRepository.getInstance returned null");
            }
            LOGGER.log(Level.INFO, "ClientsRepository initialized successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize ClientsRepository", e);
            throw new IllegalStateException("Cannot initialize ClientsRepository", e);
        }

        LOGGER.log(Level.INFO, String.format(
                "WrappedEvolutionaryAlgorithm initialized: algorithm=%s, output=%s, PARALLEL MODE ENABLED",
                algorithm.getClass().getSimpleName(), csvPath
        ));
    }

    @Override
    public void run() {
        LOGGER.log(Level.INFO, "Starting PARALLEL evolutionary algorithm execution");
        cleanupOutputFile();

        try {
            runEvolutionaryAlgorithm();
            LOGGER.log(Level.INFO, "PARALLEL evolutionary algorithm execution completed successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Evolutionary algorithm execution failed", e);
            throw new RuntimeException("Algorithm execution failed", e);
        }
    }

    /**
     * Executes the main evolutionary algorithm loop with parallel evaluation.
     */
    private void runEvolutionaryAlgorithm() {
        // Create and evaluate initial population (in parallel!)
        population = createInitialPopulation();
        LOGGER.log(Level.INFO, "Initial population created: " + population.size() + " solutions");

        CsvUtils.writeExcel((List<? extends Solution<?>>) population, "initial pop non-evaluated", csvPath);

        // PARALLEL EVALUATION
        population = evaluatePopulationInParallel(population);

        // Validate initial population after evaluation
        int invalidInitialCount = 0;
        for (S solution : population) {
            if (solution instanceof IntegerSolution) {
                IntegerSolution intSolution = (IntegerSolution) solution;
                if (hasInvalidObjectives(intSolution)) {
                    LOGGER.log(Level.SEVERE, String.format(
                            "Initial population has solution with invalid objectives: %s",
                            Arrays.toString(intSolution.objectives())
                    ));
                    invalidInitialCount++;
                }
            }
        }

        if (invalidInitialCount > 0) {
            LOGGER.log(Level.SEVERE, String.format(
                    "WARNING: %d out of %d solutions in initial population have invalid objectives!",
                    invalidInitialCount, population.size()
            ));
        }

        CsvUtils.writeExcel((List<? extends Solution<?>>) population, "initial pop evaluated", csvPath);
        LOGGER.log(Level.INFO, "Initial population evaluated in PARALLEL");

        // Initialize progress
        initProgress();

        // Initialize quality indicators
        int numberOfObjectives = getNumberOfObjectives(population);
        HypervolumeIndicator<Solution<?>> hvIndicator = new HypervolumeIndicator<>(numberOfObjectives);
        SpreadIndicator<Solution<?>> spreadIndicator = new SpreadIndicator<>(numberOfObjectives);
        EpsilonIndicator<Solution<?>> epsIndicator = new EpsilonIndicator<>(numberOfObjectives);

        // Set fixed reference point from initial population for comparable hypervolume across generations
        // Using 1.5x margin to allow for improvements beyond initial population
        @SuppressWarnings("unchecked")
        List<Solution<?>> initialSolutionList = (List<Solution<?>>) population;
        hvIndicator.setFixedReferencePointFromPopulation(initialSolutionList, 2.15);
        LOGGER.log(Level.INFO, "Fixed reference point set for hypervolume calculation (comparable across generations)");

        // Set ideal point from initial population for epsilon indicator
        epsIndicator.setIdealPointFromPopulation(initialSolutionList);
        LOGGER.log(Level.INFO, "Ideal point set for epsilon indicator (tracks convergence to best known values)");

        // Main evolutionary loop
        int generation = 0;
        while (!isStoppingConditionReached()) {
            LOGGER.log(Level.INFO, "Starting generation " + generation + " (PARALLEL mode)");

            // Selection
            List<S> matingPopulation = selection(population);
            LOGGER.log(Level.INFO, "Mating population selected: " + matingPopulation.size() + " solutions");

            // Reproduction
            List<S> offspringPopulation = reproduction(matingPopulation);
            LOGGER.log(Level.INFO, "Offspring population created: " + offspringPopulation.size() + " solutions");

            // PARALLEL EVALUATION - all offspring evaluated simultaneously!
            offspringPopulation = evaluatePopulationInParallel(offspringPopulation);
            LOGGER.log(Level.INFO, "Offspring evaluated in PARALLEL for generation " + generation);

            // Replacement
            population = replacement(population, offspringPopulation);
            LOGGER.log(Level.INFO, "Replacement done for generation " + generation);

            // Update progress
            updateProgress();

            // Calculate and record quality indicators
            recordQualityIndicators(generation, hvIndicator, spreadIndicator, epsIndicator);

            // Export population
            CsvUtils.writeExcel((List<? extends Solution<?>>) population, "pop after gen " + generation, csvPath);

            LOGGER.log(Level.INFO, "Generation " + generation + " completed (PARALLEL mode)");
            generation++;
        }

        LOGGER.log(Level.INFO, "Evolution completed after " + generation + " generations (PARALLEL mode)");
    }

    /**
     * PARALLEL POPULATION EVALUATION
     *
     * This is the key method that achieves embarrassingly parallel execution:
     *
     * Phase 1: Dispatch all simulations for all solutions (no waiting)
     * Phase 2: Wait once for all simulations to complete
     * Phase 3: Aggregate results for all solutions
     *
     * This maximizes parallelism and client utilization!
     */
    private List<S> evaluatePopulationInParallel(List<S> population) {
        int populationSize = population.size();
        LOGGER.log(Level.INFO, String.format(
                "=== PARALLEL EVALUATION: %d solutions ===", populationSize
        ));

        long startTime = System.currentTimeMillis();

        // Get problem from algorithm
        Problem<S> problem = getProblemFromAlgorithm();

        // Check if it's a parallel-enabled problem
        boolean isParallelProblem = problem instanceof MicroArchitectureProblem;

        // ========== PHASE 1: DISPATCH ALL SIMULATIONS ==========
        LOGGER.log(Level.INFO, "Phase 1: Dispatching simulations for all solutions...");
        long dispatchStart = System.currentTimeMillis();

        int dispatchedCount = 0;
        for (S solution : population) {
            try {
                // Call problem.evaluate() which dispatches simulations
                problem.evaluate(solution);
                dispatchedCount++;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to dispatch simulations for solution", e);
            }
        }

        long dispatchTime = System.currentTimeMillis() - dispatchStart;
        LOGGER.log(Level.INFO, String.format(
                "Phase 1 complete: Dispatched simulations for %d solutions in %dms (%.1f sol/sec)",
                dispatchedCount, dispatchTime, dispatchedCount * 1000.0 / dispatchTime
        ));

        // ========== PHASE 2: WAIT FOR ALL SIMULATIONS ==========
        if (isParallelProblem) {
            LOGGER.log(Level.INFO, "Phase 2: Waiting for ALL simulations to complete...");
            long waitStart = System.currentTimeMillis();

            // Single wait for all simulations - this is where parallelism happens!
            MicroArchitectureProblem parallelProblem = (MicroArchitectureProblem) problem;
            parallelProblem.waitForAllSimulations();

            long waitTime = System.currentTimeMillis() - waitStart;
            LOGGER.log(Level.INFO, String.format(
                    "Phase 2 complete: All simulations finished in %dms", waitTime
            ));

            // ========== PHASE 3: AGGREGATE ALL RESULTS ==========
            LOGGER.log(Level.INFO, "Phase 3: Aggregating objectives for all solutions...");
            long aggregateStart = System.currentTimeMillis();

            int aggregatedCount = 0;
            int fixedCount = 0;
            for (S solution : population) {
                try {
                    if (solution instanceof IntegerSolution) {
                        parallelProblem.aggregateObjectives((IntegerSolution) solution);
                        aggregatedCount++;

                        // CRITICAL FIX: Validate objectives after aggregation
                        // Detect solutions with zero or invalid objectives
                        IntegerSolution intSolution = (IntegerSolution) solution;
                        if (hasInvalidObjectives(intSolution)) {
                            LOGGER.log(Level.SEVERE, String.format(
                                    "Solution %d has invalid objectives after aggregation: %s - setting to MAX_VALUE",
                                    intSolution.hashCode(),
                                    Arrays.toString(intSolution.objectives())
                            ));
                            // Set bad fitness values to prevent these from dominating the population
                            for (int j = 0; j < intSolution.objectives().length; j++) {
                                intSolution.objectives()[j] = Double.MAX_VALUE;
                            }
                            fixedCount++;
                        }
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to aggregate objectives for solution", e);
                }
            }

            long aggregateTime = System.currentTimeMillis() - aggregateStart;
            LOGGER.log(Level.INFO, String.format(
                    "Phase 3 complete: Aggregated %d solutions in %dms (%d had invalid objectives and were fixed)",
                    aggregatedCount, aggregateTime, fixedCount
            ));

            // Cleanup
            parallelProblem.cleanupAllSolutions();
        } else {
            LOGGER.log(Level.INFO, "Phase 2-3: Problem is not parallel-enabled, using standard evaluation");
        }

        // Final batch finalization (error recovery, cleanup)
        clientsRepository.join();

        long totalTime = System.currentTimeMillis() - startTime;
        LOGGER.log(Level.INFO, String.format(
                "=== PARALLEL EVALUATION COMPLETE: %d solutions in %dms (%.1f sol/sec) ===",
                populationSize, totalTime, populationSize * 1000.0 / totalTime
        ));

        return population;
    }

    /**
     * Extracts the Problem instance from the algorithm using reflection.
     *
     * @return The problem instance
     * @throws IllegalStateException if problem cannot be extracted
     */
    @SuppressWarnings("unchecked")
    private Problem<S> getProblemFromAlgorithm() {
        try {
            // Try to get 'problem' field from algorithm
            Field problemField = findFieldInHierarchy(algorithm.getClass(), "problem");
            if (problemField != null) {
                problemField.setAccessible(true);
                Object problemObj = problemField.get(algorithm);
                if (problemObj instanceof Problem) {
                    LOGGER.log(Level.FINE, "Successfully extracted problem from algorithm");
                    return (Problem<S>) problemObj;
                }
            }

            // Fallback: try getProblem() method
            Method getProblemMethod = findMethodInHierarchy(algorithm.getClass(), "getProblem");
            if (getProblemMethod != null) {
                getProblemMethod.setAccessible(true);
                Object problemObj = getProblemMethod.invoke(algorithm);
                if (problemObj instanceof Problem) {
                    LOGGER.log(Level.FINE, "Successfully extracted problem via getProblem() method");
                    return (Problem<S>) problemObj;
                }
            }

            throw new IllegalStateException("Could not find problem in algorithm");
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOGGER.log(Level.SEVERE, "Failed to extract problem from algorithm", e);
            throw new IllegalStateException("Cannot access problem from algorithm", e);
        }
    }

    /**
     * Finds a field in the class hierarchy.
     */
    private Field findFieldInHierarchy(Class<?> clazz, String fieldName) {
        while (clazz != null && !Object.class.equals(clazz)) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

    /**
     * Finds a method in the class hierarchy.
     */
    private Method findMethodInHierarchy(Class<?> clazz, String methodName) {
        while (clazz != null && !Object.class.equals(clazz)) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getName().equals(methodName) && method.getParameterCount() == 0) {
                    return method;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    /**
     * Checks if a solution has invalid objectives that indicate evaluation failure.
     *
     * Invalid objectives include:
     * - Any objective that is exactly 0.0 (uninitialized or failed aggregation)
     * - Any objective that is NaN
     * - Any objective that is negative (objectives should be positive in most problems)
     *
     * @param solution The solution to check
     * @return true if the solution has invalid objectives
     */
    private boolean hasInvalidObjectives(IntegerSolution solution) {
        if (solution == null || solution.objectives() == null) {
            return true;
        }

        for (double objective : solution.objectives()) {
            // Check for uninitialized (0.0), NaN, or negative values
            if (objective == 0.0 || Double.isNaN(objective) || objective < 0.0) {
                return true;
            }
        }

        return false;
    }

    private int getNumberOfObjectives(List<S> population) {
        if (population == null || population.isEmpty()) {
            throw new IllegalStateException("Population is empty");
        }

        List<? extends Solution<?>> nonDominated = SolutionListUtils.getNonDominatedSolutions(
                (List<? extends Solution<?>>) population
        );

        if (nonDominated == null || nonDominated.isEmpty()) {
            throw new IllegalStateException("No non-dominated solutions found");
        }

        Solution<?> firstSolution = nonDominated.get(0);
        if (firstSolution.objectives() == null || firstSolution.objectives().length == 0) {
            throw new IllegalStateException("First solution has no objectives");
        }

        int numberOfObjectives = firstSolution.objectives().length;
        LOGGER.log(Level.INFO, "Number of objectives: " + numberOfObjectives);

        return numberOfObjectives;
    }

    private void recordQualityIndicators(
            int generation,
            HypervolumeIndicator<Solution<?>> hvIndicator,
            SpreadIndicator<Solution<?>> spreadIndicator,
            EpsilonIndicator<Solution<?>> epsIndicator) {

        try {
            @SuppressWarnings("unchecked")
            List<Solution<?>> paretoFront = SolutionListUtils.getNonDominatedSolutions((List<Solution<?>>) population);

            // Check for solutions with all objectives equal to 0
            int zeroObjectivesInPopulation = 0;
            int zeroObjectivesInPareto = 0;

            for (Solution<?> solution : (List<Solution<?>>) population) {
                if (solution.objectives()[0] == 0 || solution.objectives()[1] == 0) {
                    zeroObjectivesInPopulation++;
                }
            }

            for (Solution<?> solution : paretoFront) {
                if (solution.objectives()[0] == 0 || solution.objectives()[1] == 0) {
                    zeroObjectivesInPareto++;
                }
            }

            if (zeroObjectivesInPopulation > 0 || zeroObjectivesInPareto > 0) {
                LOGGER.log(Level.WARNING, String.format(
                        "Generation %d: Found %d solutions with all-zero objectives in population (%d total), " +
                                "%d in Pareto front (%d total)",
                        generation, zeroObjectivesInPopulation, population.size(),
                        zeroObjectivesInPareto, paretoFront.size()
                ));
            }

            double hvValue = hvIndicator.calculateNormalizedHypervolume(paretoFront);
            double spreadValue = spreadIndicator.calculateSpread(paretoFront);
            double epsilonValue = epsIndicator.calculateEpsilon(paretoFront);

            CsvUtils.appendValue("Hypervolume", generation, hvValue, csvPath);
            CsvUtils.appendValue("Spread", generation, spreadValue, csvPath);
            CsvUtils.appendValue("Epsilon", generation, epsilonValue, csvPath);

            LOGGER.log(Level.FINE, String.format(
                    "Generation %d indicators: HV=%.4f, Spread=%.4f, Epsilon=%.4f",
                    generation, hvValue, spreadValue, epsilonValue
            ));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to calculate quality indicators", e);
        }
    }

    private void cleanupOutputFile() {
        Path pathObj = Paths.get(csvPath);
        try {
            boolean deleted = Files.deleteIfExists(pathObj);
            if (deleted) {
                LOGGER.log(Level.INFO, "Deleted existing output file: " + csvPath);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to delete output file: " + csvPath, e);
        }
    }

    @Override
    public R result() {
        @SuppressWarnings("unchecked")
        R result = (R) SolutionListUtils.getNonDominatedSolutions(
                (List<? extends Solution<?>>) population
        );
        LOGGER.log(Level.INFO, "Final result computed");
        return result;
    }

    @Override
    public String name() {
        return invokeMethod("name", String.class);
    }

    @Override
    public String description() {
        return invokeMethod("description", String.class) + " (PARALLEL)";
    }

    @Override
    public List<S> createInitialPopulation() {
        return invokeMethod("createInitialPopulation", List.class);
    }

    @Override
    public void initProgress() {
        invokeMethodVoid("initProgress");
    }

    @Override
    public List<S> selection(List<S> population) {
        return invokeMethod("selection", List.class, population);
    }

    @Override
    public List<S> reproduction(List<S> population) {
        return invokeMethod("reproduction", List.class, population);
    }

    @Override
    public List<S> evaluatePopulation(List<S> population) {
        // Log who is calling this method for debugging
        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
        String callerMethod = caller.getMethodName();
        String callerClass = caller.getClassName();

        LOGGER.log(Level.INFO, String.format(
            "evaluatePopulation() called with %d solutions from %s.%s()",
            population.size(), callerClass, callerMethod
        ));

        // CRITICAL: Skip evaluation if called from reproduction()
        // We explicitly evaluate offspring AFTER reproduction completes
        // to maintain the embarrassingly parallel pattern (dispatch all, wait once)
        if ("reproduction".equals(callerMethod)) {
            LOGGER.log(Level.INFO, String.format(
                "Skipping evaluation during reproduction() - will evaluate explicitly afterward (%d solutions)",
                population.size()
            ));
            return population; // Return unevaluated - we'll evaluate later
        }

        // For all other callers, use our parallel implementation
        return evaluatePopulationInParallel(population);
    }

    @Override
    public List<S> replacement(List<S> population, List<S> offspringPopulation) {
        return invokeMethod("replacement", List.class, population, offspringPopulation);
    }

    @Override
    public void updateProgress() {
        invokeMethodVoid("updateProgress");
    }

    @Override
    public boolean isStoppingConditionReached() {
        return invokeMethod("isStoppingConditionReached", Boolean.class);
    }

    private void invokeMethodVoid(String methodName) {
        Method method = methodsDictionary.get(methodName);
        if (method == null) {
            LOGGER.log(Level.SEVERE, "Method '" + methodName + "' not found. Available methods: " +
                methodsDictionary.keySet());
            throw new IllegalStateException("Method '" + methodName + "' not found in dictionary");
        }
        try {
            method.invoke(algorithm);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            LOGGER.log(Level.SEVERE, String.format(
                "Failed to invoke void method '%s': %s - %s",
                methodName,
                cause != null ? cause.getClass().getName() : "unknown",
                cause != null ? cause.getMessage() : "no message"
            ), e);
            throw new RuntimeException("Failed to invoke method: " + methodName +
                " (cause: " + (cause != null ? cause.getMessage() : "unknown") + ")", e);
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, "Access denied to method: " + methodName, e);
            throw new RuntimeException("Cannot access method: " + methodName, e);
        }
    }

    private <T> T invokeMethod(String methodName, Class<T> returnType) {
        Method method = methodsDictionary.get(methodName);
        if (method == null) {
            LOGGER.log(Level.SEVERE, "Method '" + methodName + "' not found. Available methods: " +
                methodsDictionary.keySet());
            throw new IllegalStateException("Method '" + methodName + "' not found in dictionary");
        }
        try {
            LOGGER.log(Level.FINE, String.format(
                "Invoking method: %s with 0 arguments", method.getName()
            ));
            Object result = method.invoke(algorithm);
            @SuppressWarnings("unchecked")
            T typedResult = (T) result;
            return typedResult;
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            LOGGER.log(Level.SEVERE, String.format(
                "Failed to invoke method '%s': %s - %s",
                methodName,
                cause != null ? cause.getClass().getName() : "unknown",
                cause != null ? cause.getMessage() : "no message"
            ), e);
            throw new RuntimeException("Failed to invoke method: " + methodName +
                " (cause: " + (cause != null ? cause.getMessage() : "unknown") + ")", e);
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, "Access denied to method: " + methodName, e);
            throw new RuntimeException("Cannot access method: " + methodName, e);
        }
    }

    private <T> T invokeMethod(String methodName, Class<T> returnType, Object... args) {
        Method method = methodsDictionary.get(methodName);
        if (method == null) {
            LOGGER.log(Level.SEVERE, "Method '" + methodName + "' not found. Available methods: " +
                methodsDictionary.keySet());
            throw new IllegalStateException("Method '" + methodName + "' not found in dictionary");
        }

        try {
            LOGGER.log(Level.FINE, String.format(
                "Invoking method: %s with %d arguments (expected params: %d)",
                method.getName(), args.length, method.getParameterCount()
            ));

            Object result = method.invoke(algorithm, args);
            @SuppressWarnings("unchecked")
            T typedResult = (T) result;
            return typedResult;
        } catch (InvocationTargetException e) {
            // CRITICAL: Extract and log the actual cause of the exception
            Throwable cause = e.getCause();
            LOGGER.log(Level.SEVERE, String.format(
                "Failed to invoke method '%s' with %d args: %s - %s",
                methodName,
                args.length,
                cause != null ? cause.getClass().getName() : "unknown",
                cause != null ? cause.getMessage() : "no message"
            ), e);

            // Include the cause in the exception message
            throw new RuntimeException("Failed to invoke method: " + methodName +
                " (cause: " + (cause != null ? cause.getClass().getSimpleName() + ": " + cause.getMessage() : "unknown") + ")", e);
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, "Access denied to method: " + methodName, e);
            throw new RuntimeException("Cannot access method: " + methodName, e);
        }
    }

    private Map<String, Method> getMethods(AbstractEvolutionaryAlgorithm<S, R> aea) {
        Map<String, Method> methods = new HashMap<>();

        Class<?> currentClass = aea.getClass();
        while (currentClass != null && !Object.class.equals(currentClass)) {
            for (Method method : currentClass.getDeclaredMethods()) {
                String methodName = method.getName();
                if (methodName.startsWith("run") || methodName.startsWith("main")) {
                    continue;
                }
                method.setAccessible(true);

                // Only add if not already present (child class methods take precedence)
                if (!methods.containsKey(methodName)) {
                    methods.put(methodName, method);
                    LOGGER.log(Level.FINE, String.format(
                        "Registered method: %s with %d parameters from class %s",
                        methodName, method.getParameterCount(), currentClass.getSimpleName()
                    ));
                }
            }
            currentClass = currentClass.getSuperclass();
        }

        LOGGER.log(Level.INFO, String.format(
            "Retrieved %d methods from algorithm %s: %s",
            methods.size(), aea.getClass().getSimpleName(), methods.keySet()
        ));

        return methods;
    }
}

