package jmetal.util;

import jmetal.base.Solution;
import jmetal.base.SolutionSet;

public class FPDRanking {
    public void rankSolutionSet(SolutionSet solutionSet) {
        for (int i = 0; i < solutionSet.size(); i++) {
            double rank = 0;
            for (int j = 0; j < solutionSet.size(); j++) {
                if (i != j) {
                    double dominanceDegree = computeDominanceDegree(solutionSet.get(j), solutionSet.get(i));
                    if (dominanceDegree > rank) {
                        rank = dominanceDegree;
                    }
                }
            }

            solutionSet.get(i).setFitness(rank);
        }
    }

    /**
     * Computes the degree by which the solution a dominates the solution b
     */
    private double computeDominanceDegree(Solution a, Solution b) {
        double numerator = 1;
        double denominator = 1;
        for (int i = 0; i < a.numberOfObjectives(); i++) {
            double ai = a.getObjective(i);
            double bi = b.getObjective(i);
            if (ai < bi && ai != 0) {
                numerator *= ai;
            } else if (bi != 0) {
                numerator *= bi;
            }

            if (ai != 0) {
                denominator *= ai;
            }
        }

        return numerator / denominator;
    }
}
