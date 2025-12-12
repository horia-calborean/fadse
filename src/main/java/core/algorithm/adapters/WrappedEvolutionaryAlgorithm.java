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

        CsvUtils.writeExcel((List<? extends Solution<?>>) population, "initial pop evaluated", csvPath);
        LOGGER.log(Level.INFO, "Initial population evaluated in PARALLEL");

        // Initialize progress
        initProgress();

        // Initialize quality indicators
        int numberOfObjectives = getNumberOfObjectives(population);
        HypervolumeIndicator<Solution<?>> hvIndicator = new HypervolumeIndicator<>(numberOfObjectives);
        SpreadIndicator<Solution<?>> spreadIndicator = new SpreadIndicator<>(numberOfObjectives);
        EpsilonIndicator<Solution<?>> epsIndicator = new EpsilonIndicator<>(numberOfObjectives);

        // Main evolutionary loop
        int generation = 0;
        while (!isStoppingConditionReached()) {
            LOGGER.log(Level.INFO, "Starting generation " + generation + " (PARALLEL mode)");

            // Selection
            List<S> matingPopulation = selection(population);

            // Reproduction
            List<S> offspringPopulation = reproduction(matingPopulation);
            LOGGER.log(Level.FINE, "Offspring population created: " + offspringPopulation.size() + " solutions");

            // PARALLEL EVALUATION - all offspring evaluated simultaneously!
            offspringPopulation = evaluatePopulationInParallel(offspringPopulation);

            LOGGER.log(Level.INFO, "Offspring evaluated in PARALLEL for generation " + generation);

            // Replacement
            population = replacement(population, offspringPopulation);

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
            for (S solution : population) {
                try {
                    if (solution instanceof IntegerSolution) {
                        parallelProblem.aggregateObjectives((IntegerSolution) solution);
                        aggregatedCount++;
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to aggregate objectives for solution", e);
                }
            }

            long aggregateTime = System.currentTimeMillis() - aggregateStart;
            LOGGER.log(Level.INFO, String.format(
                    "Phase 3 complete: Aggregated %d solutions in %dms",
                    aggregatedCount, aggregateTime
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
            List<Solution<?>> solutionList = (List<Solution<?>>) population;

            double hvValue = hvIndicator.calculateNormalizedHypervolume(solutionList);
            double spreadValue = spreadIndicator.calculateSpread(solutionList);
            double epsilonValue = epsIndicator.calculateEpsilon(solutionList);

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
        // Use our parallel implementation instead of the wrapped algorithm's
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
            throw new IllegalStateException("Method '" + methodName + "' not found");
        }
        try {
            method.invoke(algorithm);
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOGGER.log(Level.SEVERE, "Failed to invoke method: " + methodName, e);
            throw new RuntimeException("Failed to invoke method: " + methodName, e);
        }
    }

    private <T> T invokeMethod(String methodName, Class<T> returnType) {
        Method method = methodsDictionary.get(methodName);
        if (method == null) {
            throw new IllegalStateException("Method '" + methodName + "' not found");
        }
        try {
            Object result = method.invoke(algorithm);
            @SuppressWarnings("unchecked")
            T typedResult = (T) result;
            return typedResult;
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOGGER.log(Level.SEVERE, "Failed to invoke method: " + methodName, e);
            throw new RuntimeException("Failed to invoke method: " + methodName, e);
        }
    }

    private <T> T invokeMethod(String methodName, Class<T> returnType, Object... args) {
        Method method = methodsDictionary.get(methodName);
        if (method == null) {
            throw new IllegalStateException("Method '" + methodName + "' not found");
        }
        try {
            Object result = method.invoke(algorithm, args);
            @SuppressWarnings("unchecked")
            T typedResult = (T) result;
            return typedResult;
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOGGER.log(Level.SEVERE, "Failed to invoke method: " + methodName, e);
            throw new RuntimeException("Failed to invoke method: " + methodName, e);
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
                methods.putIfAbsent(methodName, method);
            }
            currentClass = currentClass.getSuperclass();
        }

        LOGGER.log(Level.FINE, "Retrieved " + methods.size() + " methods from algorithm");
        return methods;
    }
}

