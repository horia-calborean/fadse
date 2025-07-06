package core.qualityindicator;


import org.uma.jmetal.qualityindicator.impl.hypervolume.impl.WFGHypervolume;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.ranking.Ranking;
import org.uma.jmetal.util.ranking.impl.FastNonDominatedSortRanking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HypervolumeIndicator<S extends Solution<?>> extends QualityIndicator<S> {

    public HypervolumeIndicator(int nrObjectives){
        this.referenceFront = generateNormalizedReferenceFront(nrObjectives);
    }

    public double calculateHypervolume(List<? extends S> population) {
        List<S> casted = (List<S>) population; // safe due to generic contract

        double[][] front = normalizeFront(casted);
        WFGHypervolume hypervolume = new WFGHypervolume(this.referenceFront);
        return hypervolume.compute(front);
    }

    public double calculateNormalizedHypervolume(List<S> pop) {
        double[][] originalFront = convertToMatrix(pop);
        if (originalFront == null || originalFront.length == 0) {
            return 0.0;
        }

        double maxX = 0.0;
        double maxY = 0.0;
        for (double[] point : originalFront) {
            if (point[0] > maxX) maxX = point[0];
            if (point[1] > maxY) maxY = point[1];
        }

        double refX = Math.ceil(maxX * 10.0) / 10.0;
        double refY = Math.ceil(maxY * 10.0) / 10.0;
        double[] referencePoint = {refX, refY};
        System.out.println("Reference Point: [" + refX + ", " + refY + "]");

        List<double[]> maximizationFront = new ArrayList<>();
        for (double[] point : originalFront) {
            maximizationFront.add(new double[]{
                    referencePoint[0] - point[0],
                    referencePoint[1] - point[1]
            });
        }

        double rawHypervolume = pisaCalculate(maximizationFront, maximizationFront.size(), 2);
        double normalizationArea = referencePoint[0] * referencePoint[1];
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
