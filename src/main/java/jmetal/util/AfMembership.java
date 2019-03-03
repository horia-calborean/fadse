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
        double distance = 0;

        double[] objectives = new double[solution.numberOfObjectives()];
        for (int i = 0; i < solution.numberOfObjectives(); i++) {
            objectives[i] = solution.getObjective(i);
        }

        for (int i = 0; i < objectives.length; i++) {
            distance += af.getCoefficients()[i] * Math.pow(objectives[i], af.getPower());
        }

        distance -= 1;

        double alfa = 2;
        double membership = 1 - Math.pow(alfa, -(distance + 1));

        return membership;
    }
}
