package core.qualityindicator;


import org.uma.jmetal.qualityindicator.impl.hypervolume.impl.WFGHypervolume;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.ranking.Ranking;
import org.uma.jmetal.util.ranking.impl.FastNonDominatedSortRanking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HypervolumeIndicator<S extends Solution<?>> extends QualityIndicator<S> {

    private double[] fixedReferencePoint = null;
    private final int numberOfObjectives;

    public HypervolumeIndicator(int nrObjectives){
        this.referenceFront = generateNormalizedReferenceFront(nrObjectives);
        this.numberOfObjectives = nrObjectives;
    }

    /**
     * Sets a fixed reference point to use for all hypervolume calculations.
     * This ensures hypervolume values are comparable across different generations.
     *
     * @param referencePoint The fixed reference point (should be worse than all expected solutions)
     */
    public void setFixedReferencePoint(double[] referencePoint) {
        if (referencePoint.length != numberOfObjectives) {
            throw new IllegalArgumentException(
                "Reference point dimension (" + referencePoint.length +
                ") does not match number of objectives (" + numberOfObjectives + ")");
        }
        this.fixedReferencePoint = referencePoint.clone();
        System.out.println("Fixed Reference Point set: " + formatReferencePoint(referencePoint));
    }

    /**
     * Automatically determines and sets a fixed reference point from the initial population.
     * Call this once with the initial population before starting evolution.
     *
     * @param initialPopulation The initial population
     * @param marginFactor Factor to extend beyond max values (e.g., 1.2 = 20% margin)
     */
    public void setFixedReferencePointFromPopulation(List<S> initialPopulation, double marginFactor) {
        double[][] front = convertToMatrix(initialPopulation);
        if (front == null || front.length == 0) {
            throw new IllegalArgumentException("Cannot set reference point from empty population");
        }

        double[] refPoint = new double[numberOfObjectives];
        for (int obj = 0; obj < numberOfObjectives; obj++) {
            double max = Double.NEGATIVE_INFINITY;
            for (double[] point : front) {
                max = Math.max(max, point[obj]);
            }
            refPoint[obj] = max * marginFactor;
        }

        setFixedReferencePoint(refPoint);
    }

    private String formatReferencePoint(double[] point) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < point.length; i++) {
            sb.append(String.format("%.2f", point[i]));
            if (i < point.length - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }

    public double calculateHypervolume(List<? extends S> population) {
        List<S> casted = (List<S>) population; // safe due to generic contract

        double[][] front = normalizeFront(casted);
        WFGHypervolume hypervolume = new WFGHypervolume(this.referenceFront);
        return hypervolume.compute(front);
    }

    /**
     * Calculates normalized hypervolume for a population.
     * If a fixed reference point is set, it will be used (recommended for comparing across generations).
     * Otherwise, a dynamic reference point is calculated from the current population.
     *
     * @param pop The population to calculate hypervolume for
     * @return Normalized hypervolume value between 0 and 1
     */
    public double calculateNormalizedHypervolume(List<S> pop) {
        double[][] originalFront = convertToMatrix(pop);
        if (originalFront == null || originalFront.length == 0) {
            return 0.0;
        }

        int nObjectives = originalFront[0].length;
        double[] referencePoint;

        // Use fixed reference point if available, otherwise calculate dynamically
        if (fixedReferencePoint != null) {
            referencePoint = fixedReferencePoint;
            // Validate that no point exceeds the fixed reference
            for (double[] point : originalFront) {
                for (int obj = 0; obj < nObjectives; obj++) {
                    if (point[obj] > referencePoint[obj]) {
                        System.err.println("WARNING: Solution objective value " + point[obj] +
                                         " exceeds fixed reference point " + referencePoint[obj] +
                                         " for objective " + obj);
                    }
                }
            }
        } else {
            // Dynamic reference point (original behavior - NOT recommended for cross-generation comparison)
            double[] maxValues = new double[nObjectives];
            for (int obj = 0; obj < nObjectives; obj++) {
                maxValues[obj] = Double.NEGATIVE_INFINITY;
                for (double[] point : originalFront) {
                    if (point[obj] > maxValues[obj]) {
                        maxValues[obj] = point[obj];
                    }
                }
            }

            // Create reference point (10% beyond max, rounded up to nearest 0.1)
            referencePoint = new double[nObjectives];
            for (int obj = 0; obj < nObjectives; obj++) {
                referencePoint[obj] = Math.ceil(maxValues[obj] * 10.0) / 10.0;
            }

            System.out.println("Dynamic Reference Point: " + formatReferencePoint(referencePoint) +
                             " (WARNING: Not comparable across generations!)");
        }

        // Transform to maximization problem
        List<double[]> maximizationFront = new ArrayList<>();
        for (double[] point : originalFront) {
            double[] transformedPoint = new double[nObjectives];
            for (int obj = 0; obj < nObjectives; obj++) {
                transformedPoint[obj] = referencePoint[obj] - point[obj];
                // Ensure non-negative values (point should be better than reference)
                if (transformedPoint[obj] < 0) {
                    transformedPoint[obj] = 0.0;
                }
            }
            maximizationFront.add(transformedPoint);
        }

        // Calculate raw hypervolume using PISA algorithm
        double rawHypervolume = pisaCalculate(maximizationFront, maximizationFront.size(), nObjectives);

        // Calculate normalization area (product of all reference point coordinates)
        double normalizationArea = 1.0;
        for (double ref : referencePoint) {
            normalizationArea *= ref;
        }

        return normalizationArea <= 0 ? 0.0 : rawHypervolume / normalizationArea;
    }

    private static double pisaCalculate(List<double[]> front, int nPoints, int nObjectives) {
        double volume = 0.0;
        double distance = 0.0;

        while (nPoints > 0) {
            int nonDominatedPoints = filterNondominatedSet(front, nPoints, nObjectives - 1);
            double tempVolume = 0.0;
            if (nObjectives < 3) {
                if (nonDominatedPoints > 0) {
                    tempVolume = front.get(0)[0];
                }
            } else {
                List<double[]> subFront = new ArrayList<>(front.subList(0, nonDominatedPoints));
                tempVolume = pisaCalculate(subFront, nonDominatedPoints, nObjectives - 1);
            }

            double tempDistance = surfaceUnchangedTo(front, nPoints, nObjectives - 1);
            volume += tempVolume * (tempDistance - distance);
            distance = tempDistance;
            nPoints = reduceNondominatedSet(front, nPoints, nObjectives - 1, distance);
        }

        return volume;
    }

    private static boolean dominates(double[] p1, double[] p2, int nObjectives) {
        boolean betterInAny = false;
        for (int i = 0; i < nObjectives; i++) {
            if (p1[i] < p2[i]) return false;
            if (p1[i] > p2[i]) betterInAny = true;
        }
        return betterInAny;
    }

    private static int filterNondominatedSet(List<double[]> front, int nPoints, int nObjectives) {
        int n = nPoints;
        int i = 0;
        while (i < n) {
            int j = i + 1;
            while (j < n) {
                if (dominates(front.get(i), front.get(j), nObjectives)) {
                    n--;
                    Collections.swap(front, j, n);
                } else if (dominates(front.get(j), front.get(i), nObjectives)) {
                    n--;
                    Collections.swap(front, i, n);
                    i--;
                    break;
                } else {
                    j++;
                }
            }
            i++;
        }
        return n;
    }

    private static double surfaceUnchangedTo(List<double[]> front, int nPoints, int objective) {
        if (nPoints == 0) return 0.0;
        double minVal = front.get(0)[objective];
        for (int i = 1; i < nPoints; i++) {
            minVal = Math.min(minVal, front.get(i)[objective]);
        }
        return minVal;
    }

    private static int reduceNondominatedSet(List<double[]> front, int nPoints, int objective, double threshold) {
        int n = nPoints;
        int i = 0;
        while (i < n) {
            if (front.get(i)[objective] <= threshold) {
                n--;
                Collections.swap(front, i, n);
            } else {
                i++;
            }
        }
        return n;
    }
}
