package core.algorithm.adapters;

import org.uma.jmetal.algorithm.impl.AbstractEvolutionaryAlgorithm;
import org.uma.jmetal.operator.selection.impl.RankingAndCrowdingSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.problem.doubleproblem.DoubleProblem;
import org.uma.jmetal.problem.integerproblem.IntegerProblem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.bounds.Bounds;
import org.uma.jmetal.util.comparator.dominanceComparator.impl.DefaultDominanceComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Non-dominated Sorting Whale Optimization Algorithm (NSWOA)
 *
 * This class implements NSWOA, a multi-objective optimization algorithm that combines
 * the Whale Optimization Algorithm (WOA) with non-dominated sorting and crowding distance
 * selection mechanisms from NSGA-II.
 *
 * The algorithm mimics the hunting behavior of humpback whales:
 * - Exploration phase (|A| >= 1): Whales search for prey using random positions
 * - Exploitation phase (|A| < 1): Whales attack prey using bubble-net feeding method
 *   - Shrinking encircling mechanism (p < 0.5)
 *   - Spiral updating position (p >= 0.5)
 *
 * The non-dominated sorting ensures that the algorithm maintains a diverse Pareto front
 * for multi-objective optimization problems.
 *
 * This implementation follows the standard jMetal AbstractEvolutionaryAlgorithm pattern,
 * making it fully compatible with WrappedEvolutionaryAlgorithm for parallel execution.
 *
 * The algorithm is generic and works with any solution type (IntegerSolution, DoubleSolution,
 * BinarySolution, etc.) by operating on the solution's variables through the generic
 * Solution interface.
 *
 * @param <SolutionType> The type of solution to be optimized (must extend Solution)
 *
 * @author FADSE Team
 * @version 2.0 (Refactored to follow jMetal pattern and support all solution types)
 */
public class NSWOA<SolutionType extends Solution<?>> extends AbstractEvolutionaryAlgorithm<SolutionType, List<SolutionType>>
{
    protected Problem<SolutionType> problem;
    protected final SolutionListEvaluator<SolutionType> evaluator;
    protected final int maxEvaluations;
    protected int evaluations;
    protected final int populationSize;

    private final Random random;
    private final Comparator<SolutionType> dominanceComparator;

    /**
     * Constructor for NSWOA algorithm
     *
     * @param problem The multi-objective optimization problem to solve
     * @param evaluator Evaluator for solutions
     * @param maxEvaluations Maximum number of evaluations (population size * generations)
     * @param populationSize Population size (number of whales/search agents)
     */
    public NSWOA(Problem<SolutionType> problem,
                 SolutionListEvaluator<SolutionType> evaluator,
                 int maxEvaluations,
                 int populationSize) {
        this.problem = problem;
        this.evaluator = evaluator;
        this.maxEvaluations = maxEvaluations;
        this.populationSize = populationSize;
        this.evaluations = 0;
        this.random = new Random();
        this.dominanceComparator = new DefaultDominanceComparator<>();
    }

    /**
     * Create the initial population of search agents (whales) with random solutions.
     * Standard jMetal method.
     *
     * @return List of initialized search agents
     */
    @Override
    protected List<SolutionType> createInitialPopulation() {
        List<SolutionType> population = new ArrayList<>(populationSize);
        for (int i = 0; i < populationSize; i++) {
            population.add(problem.createSolution());
        }
        return population;
    }

    /**
     * Evaluate the population using the evaluator.
     * Standard jMetal method.
     *
     * @param population Population to evaluate
     * @return Evaluated population with objective values computed
     */
    @Override
    protected List<SolutionType> evaluatePopulation(List<SolutionType> population) {
        return evaluator.evaluate(population, problem);
    }

    /**
     * Initialize progress tracking (evaluation counter).
     * Standard jMetal method.
     */
    @Override
    protected void initProgress() {
        evaluations = populationSize; // Initial population already evaluated
    }

    /**
     * Update progress tracking (increment evaluation counter).
     * Standard jMetal method.
     */
    @Override
    protected void updateProgress() {
        evaluations += populationSize; // One generation of offspring evaluated
    }

    /**
     * Check if the stopping condition has been reached.
     * Standard jMetal method.
     *
     * @return true if evaluations >= maxEvaluations, false otherwise
     */
    @Override
    protected boolean isStoppingConditionReached() {
        return evaluations >= maxEvaluations;
    }

    /**
     * Selection operator - returns the population as is.
     * In WOA, we don't use traditional selection; all whales move.
     * Standard jMetal method.
     *
     * @param population Current population
     * @return Same population
     */
    @Override
    protected List<SolutionType> selection(List<SolutionType> population) {
        return population;
    }


    /**
     * Reproduction operator - generates offspring using WOA movement equations.
     * Standard jMetal method.
     *
     * This method implements the core WOA behavior:
     * 1. Calculate linearly decreasing parameter 'a' (from 2 to 0)
     * 2. For each search agent (whale):
     *    - If |A| >= 1: Exploration phase (search for prey using random agent)
     *    - If |A| < 1: Exploitation phase (attack prey using best agent)
     *      - If p < 0.5: Use shrinking encircling mechanism
     *      - If p >= 0.5: Use spiral updating position
     * 3. Apply boundary constraints
     *
     * Works with any solution type (Integer, Double, Binary) by operating on
     * the generic variable interface.
     *
     * @param population Parent population
     * @return Offspring population
     */
    @Override
    protected List<SolutionType> reproduction(List<SolutionType> population) {
        List<SolutionType> offspringPopulation = new ArrayList<>(populationSize);
        List<SolutionType> nonDominatedSolutions = SolutionListUtils.getNonDominatedSolutions(population);

        // Ensure there's at least one non-dominated solution
        if (nonDominatedSolutions.isEmpty()) {
            nonDominatedSolutions.add(population.get(0));
        }

        // Calculate current generation from evaluations
        int currentGeneration = evaluations / populationSize;
        int maxGenerations = maxEvaluations / populationSize;

        // Parameter 'a' DECREASES LINEARLY FROM 2 TO 0
        // This controls the balance between exploration and exploitation
        double a = 2.0 - currentGeneration * (2.0 / maxGenerations);

        for (int i = 0; i < populationSize; i++) {
            SolutionType currentAgent = population.get(i);
            @SuppressWarnings("unchecked")
            SolutionType newAgent = (SolutionType) currentAgent.copy();

            int numVariables = problem.numberOfVariables();

            // Get current position as double array (works for Integer and Double)
            double[] currentPosition = new double[numVariables];
            for (int j = 0; j < numVariables; j++) {
                currentPosition[j] = getVariableValue(currentAgent, j);
            }

            double[] newPosition = new double[numVariables];

            // Calculate coefficient A to determine exploration vs exploitation
            // A = 2*a*r - a, where r is random in [0,1]
            // When |A| >= 1: Exploration (search globally)
            // When |A| < 1: Exploitation (search locally)
            double r = random.nextDouble();
            double A_magnitude = 2 * a * r - a;

            if (Math.abs(A_magnitude) >= 1) {
                // EXPLORATION PHASE: Search for prey using random search agent
                // This promotes global exploration to avoid local optima
                double[] A = new double[numVariables];
                double[] C = new double[numVariables];

                for (int j = 0; j < numVariables; j++) {
                    A[j] = 2 * a * random.nextDouble() - a;  // A coefficient
                    C[j] = 2 * random.nextDouble();           // C coefficient
                }

                // Select a random agent different from current
                int randomAgentIndex = random.nextInt(populationSize);
                while (randomAgentIndex == i && populationSize > 1) {
                    randomAgentIndex = random.nextInt(populationSize);
                }
                SolutionType randomAgent = population.get(randomAgentIndex);

                // Get random position
                double[] randomPosition = new double[numVariables];
                for (int j = 0; j < numVariables; j++) {
                    randomPosition[j] = getVariableValue(randomAgent, j);
                }

                // Update position using equation: X(t+1) = X_rand - A*D
                // where D = |C*X_rand - X|
                for (int j = 0; j < numVariables; j++) {
                    double D = Math.abs(C[j] * randomPosition[j] - currentPosition[j]);
                    newPosition[j] = randomPosition[j] - A[j] * D;
                }

            } else {
                // EXPLOITATION PHASE: Bubble-net attacking method using best agent
                // Select best agent from non-dominated solutions
                SolutionType bestAgent = nonDominatedSolutions.get(random.nextInt(nonDominatedSolutions.size()));

                // Get best position
                double[] bestPosition = new double[numVariables];
                for (int j = 0; j < numVariables; j++) {
                    bestPosition[j] = getVariableValue(bestAgent, j);
                }

                double p = random.nextDouble();

                if (p < 0.5) {
                    // SHRINKING ENCIRCLING MECHANISM
                    // Whales encircle the prey (best solution) and shrink the circle
                    double[] A = new double[numVariables];
                    double[] C = new double[numVariables];

                    for (int j = 0; j < numVariables; j++) {
                        A[j] = 2 * a * random.nextDouble() - a;
                        C[j] = 2 * random.nextDouble();
                    }

                    // Update position: X(t+1) = X_best - A*D
                    // where D = |C*X_best - X|
                    for (int j = 0; j < numVariables; j++) {
                        double D = Math.abs(C[j] * bestPosition[j] - currentPosition[j]);
                        newPosition[j] = bestPosition[j] - A[j] * D;
                    }

                } else {
                    // SPIRAL UPDATING POSITION
                    // Whales move in a spiral path around the prey
                    double b = 1; // Logarithmic spiral shape constant
                    double l = -1.0 + random.nextDouble() * 2.0; // Random number in [-1, 1]

                    // Update position: X(t+1) = D'*exp(b*l)*cos(2*pi*l) + X_best
                    // where D' = |X_best - X|
                    for (int j = 0; j < numVariables; j++) {
                        double D = Math.abs(bestPosition[j] - currentPosition[j]);
                        newPosition[j] = D * Math.exp(b * l) * Math.cos(2.0 * Math.PI * l) + bestPosition[j];
                    }
                }
            }

            // Apply new position to the agent (works for Integer and Double solutions)
            for (int j = 0; j < numVariables; j++) {
                setVariableValue(newAgent, j, newPosition[j]);
            }

            // Ensure the solution stays within bounds
            repairSolution(newAgent);
            offspringPopulation.add(newAgent);
        }

        return offspringPopulation;
    }

    /**
     * Replacement operator - selects next generation using non-dominated sorting.
     * Standard jMetal method.
     *
     * This method implements the "Non-dominated Sorting" part of NSWOA:
     * 1. Combine current population with offspring (total: 2*popSize)
     * 2. Apply non-dominated sorting and crowding distance selection
     * 3. Select the best popSize solutions for the next generation
     *
     * This ensures diversity and convergence towards the Pareto front.
     *
     * @param population Current population
     * @param offspringPopulation Offspring population
     * @return Next generation population
     */
    @Override
    protected List<SolutionType> replacement(List<SolutionType> population, List<SolutionType> offspringPopulation) {
        // Combine current population with offspring
        List<SolutionType> combinedPopulation = new ArrayList<>(population.size() + offspringPopulation.size());
        combinedPopulation.addAll(population);
        combinedPopulation.addAll(offspringPopulation);

        // Use Ranking and Crowding Distance selection to maintain diversity
        // This implements the "Non-dominated Sorting" part of NSWOA
        RankingAndCrowdingSelection<SolutionType> selection =
            new RankingAndCrowdingSelection<>(populationSize, dominanceComparator);

        return selection.execute(combinedPopulation);
    }

    /**
     * Repair solutions that exceed variable bounds.
     *
     * Works generically with any solution type (Integer, Double, Binary).
     * If a variable value is below the lower bound, it's set to the lower bound.
     * If a variable value is above the upper bound, it's set to the upper bound.
     *
     * @param solution Solution to check and repair
     */
    protected void repairSolution(SolutionType solution) {
        for (int i = 0; i < problem.numberOfVariables(); i++) {
            double value = getVariableValue(solution, i);
            double lowerBound;
            double upperBound;

            // Get bounds based on problem type
            if (problem instanceof IntegerProblem) {
                Bounds<Integer> bounds = ((IntegerProblem) problem).variableBounds().get(i);
                lowerBound = bounds.getLowerBound();
                upperBound = bounds.getUpperBound();
            } else if (problem instanceof DoubleProblem) {
                Bounds<Double> bounds = ((DoubleProblem) problem).variableBounds().get(i);
                lowerBound = bounds.getLowerBound();
                upperBound = bounds.getUpperBound();
            } else {
                // Skip repair for unknown problem types
                continue;
            }

            // Apply bounds
            if (value < lowerBound) {
                setVariableValue(solution, i, lowerBound);
            } else if (value > upperBound) {
                setVariableValue(solution, i, upperBound);
            }
        }
    }

    /**
     * Get variable value as double (works for Integer and Double solutions).
     *
     * @param solution The solution
     * @param index Variable index
     * @return Variable value as double
     */
    private double getVariableValue(SolutionType solution, int index) {
        Object value = solution.variables().get(index);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        throw new IllegalArgumentException("Variable is not a number: " + value.getClass());
    }

    /**
     * Set variable value (works for Integer and Double solutions).
     * For IntegerSolution, rounds the value to nearest integer.
     *
     * @param solution The solution
     * @param index Variable index
     * @param value New value
     */
    @SuppressWarnings("unchecked")
    private void setVariableValue(SolutionType solution, int index, double value) {
        Object currentValue = solution.variables().get(index);

        if (currentValue instanceof Integer) {
            // For IntegerSolution, round to nearest integer
            ((List<Integer>) solution.variables()).set(index, (int) Math.round(value));
        } else if (currentValue instanceof Double) {
            // For DoubleSolution, keep as double
            ((List<Double>) solution.variables()).set(index, value);
        } else {
            throw new IllegalArgumentException("Unsupported variable type: " + currentValue.getClass());
        }
    }

    /**
     * Get the name of the algorithm.
     *
     * @return Algorithm name
     */
    @Override
    public String name() {
        return "NSWOA";
    }

    /**
     * Get the description of the algorithm.
     *
     * @return Algorithm description
     */
    @Override
    public String description() {
        return "Non-dominated Sorting Whale Optimization Algorithm";
    }

    /**
     * Get the result of the optimization (non-dominated solutions).
     *
     * @return List of non-dominated solutions (Pareto front approximation)
     */
    @Override
    public List<SolutionType> result() {
        return SolutionListUtils.getNonDominatedSolutions(getPopulation());
    }

}