package jmetal.util;

import jmetal.base.Solution;

public class AfDistance {
    public static double ComputeDistance(ApparentFront af, Solution solution){
        double distance = 0;

        double[] objectives = new double[solution.numberOfObjectives()];
        for (int i = 0; i < solution.numberOfObjectives(); i++) {
            objectives[i] = solution.getObjective(i);
        }

        for (int i = 0; i < objectives.length; i++) {
            distance += af.getCoefficients()[i] * Math.pow(objectives[i], af.getPower());
        }

        distance -= 1;
        return distance;
    }
}
