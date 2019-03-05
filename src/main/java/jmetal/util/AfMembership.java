package jmetal.util;

import jmetal.base.Solution;

/**
 * This class computes the AFR Membership for a given Apparent Front and a Solution
 */
public class AfMembership {
    /**
     * Computes the fuzzy membership of a {@link Solution} to an {@link ApparentFront}
     *
     * @param af the {@link ApparentFront}
     * @param solution the {@link Solution} for which the membership is computed
     * @return the fuzzy membership of the {@link Solution} to the {@link ApparentFront}
     */
    public double compute(ApparentFront af, Solution solution) {
        double distance = AfDistance.ComputeDistance(af, solution);

        double alfa = 2;
        double membership = 1 - Math.pow(alfa, -(distance + 1));

        return membership;
    }
}
