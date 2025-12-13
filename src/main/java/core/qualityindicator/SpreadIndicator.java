package core.qualityindicator;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.ranking.Ranking;
import org.uma.jmetal.util.ranking.impl.FastNonDominatedSortRanking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Spread indicator for measuring distribution and extent of solutions.
 * Calculates spread based on the current front without requiring a reference front.
 *
 * Lower values indicate better spread (more uniform distribution).
 * Value of 0 means perfect uniform distribution.
 */
public class SpreadIndicator<S extends Solution<?>> extends QualityIndicator<S> {

    private final int numberOfObjectives;

    public SpreadIndicator(int nrObjectives) {
        this.referenceFront = generateNormalizedReferenceFront(nrObjectives);
        this.numberOfObjectives = nrObjectives;
    }

    /**
     * Calculates spread indicator based on the current front.
     * Measures uniformity of distribution and extent coverage.
     *
     * Formula (from Deb et al., 2002):
     * Δ = (df + dl + Σ|di - d̄|) / (df + dl + (N-1)·d̄)
     *
     * Where:
     * - df, dl: distances to extreme points
     * - di: distance between consecutive solutions
     * - d̄: mean distance
     * - N: number of solutions
     *
     * @param population The population to evaluate
     * @return Spread value (0 = perfect spread, higher = worse spread)
     */
    public double calculateSpread(List<? extends S> population) {
        if (population == null || population.isEmpty()) {
            return Double.MAX_VALUE; // Worst spread for empty population
        }

        if (population.size() == 1) {
            return 0.0; // Single solution has perfect spread
        }

        // Get first non-dominated front
        @SuppressWarnings("unchecked")
        List<S> pop = (List<S>) population;
        Ranking<S> ranking = new FastNonDominatedSortRanking<S>().compute(pop);
        List<S> paretoFront = ranking.getSubFront(0);

        if (paretoFront.size() <= 1) {
            return 0.0;
        }

        double[][] front = convertToMatrix(paretoFront);
        int nObjectives = front[0].length;

        // Normalize front to [0,1] for fair distance calculation
        double[][] normalizedFront = normalizeToUnitHypercube(front);

        if (nObjectives == 2) {
            return calculateSpread2D(normalizedFront);
        } else {
            return calculateSpreadND(normalizedFront);
        }
    }

    /**
     * Calculate spread for 2-objective problems using the standard Deb formula.
     */
    private double calculateSpread2D(double[][] front) {
        int n = front.length;

        // Sort by first objective
        Arrays.sort(front, (a, b) -> Double.compare(a[0], b[0]));

        // Find extreme solutions (best in each objective)
        double[] extreme1 = front[0]; // Best in obj 1
        double[] extreme2 = front[n - 1]; // Best in obj 2 (typically)

        // Actually find real extremes
        for (double[] point : front) {
            if (point[1] < extreme2[1]) {
                extreme2 = point;
            }
        }

        // Calculate distances between consecutive solutions
        List<Double> distances = new ArrayList<>();
        for (int i = 0; i < n - 1; i++) {
            double dist = euclideanDistance(front[i], front[i + 1]);
            distances.add(dist);
        }

        if (distances.isEmpty()) {
            return 0.0;
        }

        // Mean distance
        double meanDist = distances.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        if (meanDist == 0.0) {
            return 0.0; // All points identical
        }

        // Distance to extremes (use small epsilon if extremes are in the front)
        double df = euclideanDistance(extreme1, front[0]);
        double dl = euclideanDistance(extreme2, front[n - 1]);

        // If extremes are in the front, df and dl should be 0
        // Use a small boundary distance instead
        if (df < 1e-10) df = 0.0;
        if (dl < 1e-10) dl = 0.0;

        // Calculate spread
        double sumDeviation = distances.stream()
                .mapToDouble(d -> Math.abs(d - meanDist))
                .sum();

        double numerator = df + dl + sumDeviation;
        double denominator = df + dl + (n - 1) * meanDist;

        if (denominator < 1e-10) {
            return 0.0;
        }

        return numerator / denominator;
    }

    /**
     * Calculate spread for many-objective problems.
     * Uses average distance to nearest neighbor as diversity metric.
     */
    private double calculateSpreadND(double[][] front) {
        int n = front.length;

        // Calculate distance to nearest neighbor for each solution
        List<Double> nearestDistances = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            double minDist = Double.MAX_VALUE;
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    double dist = euclideanDistance(front[i], front[j]);
                    minDist = Math.min(minDist, dist);
                }
            }
            nearestDistances.add(minDist);
        }

        // Calculate mean and standard deviation
        double mean = nearestDistances.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        if (mean < 1e-10) {
            return 0.0;
        }

        double variance = nearestDistances.stream()
                .mapToDouble(d -> Math.pow(d - mean, 2))
                .average()
                .orElse(0.0);

        double stdDev = Math.sqrt(variance);

        // Coefficient of variation as spread measure (normalized)
        return stdDev / mean;
    }

    /**
     * Normalize front to unit hypercube [0,1]^n for fair distance calculations.
     */
    private double[][] normalizeToUnitHypercube(double[][] front) {
        int n = front.length;
        int nObjectives = front[0].length;

        double[] minValues = new double[nObjectives];
        double[] maxValues = new double[nObjectives];

        // Initialize
        System.arraycopy(front[0], 0, minValues, 0, nObjectives);
        System.arraycopy(front[0], 0, maxValues, 0, nObjectives);

        // Find min and max
        for (double[] point : front) {
            for (int j = 0; j < nObjectives; j++) {
                minValues[j] = Math.min(minValues[j], point[j]);
                maxValues[j] = Math.max(maxValues[j], point[j]);
            }
        }

        // Normalize
        double[][] normalized = new double[n][nObjectives];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < nObjectives; j++) {
                double range = maxValues[j] - minValues[j];
                if (range > 1e-10) {
                    normalized[i][j] = (front[i][j] - minValues[j]) / range;
                } else {
                    normalized[i][j] = 0.0;
                }
            }
        }

        return normalized;
    }

    /**
     * Calculate Euclidean distance between two points.
     */
    private double euclideanDistance(double[] p1, double[] p2) {
        double sum = 0.0;
        for (int i = 0; i < p1.length; i++) {
            sum += Math.pow(p1[i] - p2[i], 2);
        }
        return Math.sqrt(sum);
    }
}
