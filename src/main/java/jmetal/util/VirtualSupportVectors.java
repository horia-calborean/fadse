package jmetal.util;

import jmetal.base.Solution;
import jmetal.base.SolutionSet;
import jmetal.base.operator.comparator.ObjectiveComparator;

public class VirtualSupportVectors {
    public static SolutionSet getVirtualSupportVectorsCloseToX(SolutionSet population, int number) {
        SolutionSet supportVectors = new SolutionSet(number);
        SolutionSet sortedPopulation = population.deepClone();
        sortedPopulation.sort(new ObjectiveComparator(0));

        double startHC = 4500;
        for (int i = 0; i < number; i++) {
            Solution solMinIPC = new Solution(sortedPopulation.get(5));
            solMinIPC.setObjective(0, sortedPopulation.get(5).getObjective(0));
            solMinIPC.setObjective(1, startHC + i * 50);

            supportVectors.add(solMinIPC);
        }

        return supportVectors;
    }

    public static SolutionSet getVirtualSupportVectorsCloseToY(SolutionSet population, int number) {
        SolutionSet supportVectors = new SolutionSet(number);
        SolutionSet sortedPopulation = population.deepClone();
        sortedPopulation.sort(new ObjectiveComparator(1));

        double startIPC = 2.5;
        for (int i = 0; i < number; i++) {
            Solution solMinHC = new Solution(sortedPopulation.get(5));
            solMinHC.setObjective(0, startIPC + 0.5 * i);
            solMinHC.setObjective(1, sortedPopulation.get(5).getObjective(1));

            supportVectors.add(solMinHC);
        }

        return supportVectors;
    }
}
