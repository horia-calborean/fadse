package core.qualityindicator;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.ranking.Ranking;
import org.uma.jmetal.util.ranking.impl.FastNonDominatedSortRanking;

import java.util.List;

/**
 * Epsilon indicator for measuring convergence quality of solutions.
 * Calculates epsilon based on the current front without requiring a reference front.
 *
 * Since we don't have a reference front, we use alternative metrics:
 * - For tracking progress: Average distance to ideal point (nadir)
 * - Lower values indicate better convergence (closer to ideal)
 */
public class EpsilonIndicator<S extends Solution<?>> extends QualityIndicator<S> {

    private final int numberOfObjectives;
    private double[] idealPoint = null;  // Best values seen so far (for minimization)
    private double[] nadirPoint = null;  // Worst values seen so far

    public EpsilonIndicator(int nrObjectives) {
        this.referenceFront = generateNormalizedReferenceFront(nrObjectives);
        this.numberOfObjectives = nrObjectives;
    }

    /**
     * Sets the ideal point (best known values for each objective).
     * For minimization problems, these should be the minimum values.
     * Call this with the initial population to establish a baseline.
     *
     * @param population Population to extract ideal point from
     */
    public void setIdealPointFromPopulation(List<S> population) {
        double[][] front = convertToMatrix(population);
        if (front == null || front.length == 0) {
            return;
        }

        idealPoint = new double[numberOfObjectives];
        nadirPoint = new double[numberOfObjectives];

        for (int obj = 0; obj < numberOfObjectives; obj++) {
            idealPoint[obj] = Double.POSITIVE_INFINITY;
            nadirPoint[obj] = Double.NEGATIVE_INFINITY;

            for (double[] point : front) {
                idealPoint[obj] = Math.min(idealPoint[obj], point[obj]);
                nadirPoint[obj] = Math.max(nadirPoint[obj], point[obj]);
            }
        }

        System.out.println("Ideal point set: " + formatPoint(idealPoint));
        System.out.println("Nadir point set: " + formatPoint(nadirPoint));
    }

    /**
     * Updates the ideal and nadir points with new population data.
     *
     * @param population Population to update points from
     */
    public void updateIdealPoint(List<S> population) {
        double[][] front = convertToMatrix(population);
        if (front == null || front.length == 0 || idealPoint == null) {
            return;
        }

        for (double[] point : front) {
            for (int obj = 0; obj < numberOfObjectives; obj++) {
                idealPoint[obj] = Math.min(idealPoint[obj], point[obj]);
                nadirPoint[obj] = Math.max(nadirPoint[obj], point[obj]);
            }
        }
    }

    /**
     * Calculates an epsilon-like indicator based on distance to ideal point.
     *
     * Without a reference front, we measure:
     * 1. Average normalized distance from solutions to ideal point
     * 2. Maximum normalized distance (worst case)
     *
     * Returns the average distance (lower is better).
     *
     * @param population The population to evaluate
     * @return Epsilon indicator value (lower is better, 0 = all solutions at ideal point)
     */
    public double calculateEpsilon(List<? extends S> population) {
        if (population == null || population.isEmpty()) {
            return Double.MAX_VALUE;
        }

        @SuppressWarnings("unchecked")
        List<S> pop = (List<S>) population;

        // Get first non-dominated front
        Ranking<S> ranking = new FastNonDominatedSortRanking<S>().compute(pop);
        List<S> paretoFront = ranking.getSubFront(0);

        if (paretoFront.isEmpty()) {
            return Double.MAX_VALUE;
        }

        double[][] front = convertToMatrix(paretoFront);

        // Update ideal point if set, otherwise calculate from current front
        if (idealPoint == null) {
            calculateIdealAndNadirFromFront(front);
        } else {
            updateIdealPointFromFront(front);
        }

        // Calculate average normalized distance to ideal point
        double sumDistance = 0.0;
        int count = 0;

        for (double[] point : front) {
            double distance = normalizedDistanceToIdeal(point);
            sumDistance += distance;
            count++;
        }

        if (count == 0) {
            return Double.MAX_VALUE;
        }

        return sumDistance / count;
    }

    /**
     * Calculate normalized distance from a point to the ideal point.
     * Uses nadir point for normalization.
     */
    private double normalizedDistanceToIdeal(double[] point) {
        double sumSquared = 0.0;

        for (int obj = 0; obj < numberOfObjectives; obj++) {
            double range = nadirPoint[obj] - idealPoint[obj];
            double normalizedDiff;

            if (range > 1e-10) {
                normalizedDiff = (point[obj] - idealPoint[obj]) / range;
            } else {
                normalizedDiff = 0.0;
            }

            sumSquared += normalizedDiff * normalizedDiff;
        }

        return Math.sqrt(sumSquared);
    }

    /**
     * Calculate ideal and nadir points from the current front only.
     */
    private void calculateIdealAndNadirFromFront(double[][] front) {
        idealPoint = new double[numberOfObjectives];
        nadirPoint = new double[numberOfObjectives];

        for (int obj = 0; obj < numberOfObjectives; obj++) {
            idealPoint[obj] = Double.POSITIVE_INFINITY;
            nadirPoint[obj] = Double.NEGATIVE_INFINITY;

            for (double[] point : front) {
                idealPoint[obj] = Math.min(idealPoint[obj], point[obj]);
                nadirPoint[obj] = Math.max(nadirPoint[obj], point[obj]);
            }
        }
    }

    /**
     * Update ideal and nadir points from new front data.
     */
    private void updateIdealPointFromFront(double[][] front) {
        for (double[] point : front) {
            for (int obj = 0; obj < numberOfObjectives; obj++) {
                idealPoint[obj] = Math.min(idealPoint[obj], point[obj]);
                nadirPoint[obj] = Math.max(nadirPoint[obj], point[obj]);
            }
        }
    }

    /**
     * Format a point for display.
     */
    private String formatPoint(double[] point) {
        if (point == null) return "null";

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < point.length; i++) {
            sb.append(String.format("%.2f", point[i]));
            if (i < point.length - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }
}
