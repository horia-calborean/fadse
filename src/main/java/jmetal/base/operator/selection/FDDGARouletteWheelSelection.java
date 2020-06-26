package jmetal.base.operator.selection;

import jmetal.base.SolutionSet;
import jmetal.util.JMException;

import java.util.Arrays;
import java.util.Random;

public class FDDGARouletteWheelSelection extends Selection {
    Random rng;

    public FDDGARouletteWheelSelection() {
        rng = new Random();
    }

    @Override
    public Object execute(Object object) throws JMException {
        SolutionSet population = (SolutionSet) object;
        int poolSize = (int) this.getParameter("PoolSize");

        double[] cumulativeFitnesses = new double[population.size()];
        cumulativeFitnesses[0] = getAdjustedFitness(population.get(0).getFitness());

        for (int i = 1; i < population.size(); i++) {
            double fitness = getAdjustedFitness(population.get(i).getFitness());
            cumulativeFitnesses[i] = cumulativeFitnesses[i - 1] + fitness;
        }

        SolutionSet pool = new SolutionSet(2 * poolSize);
        for (int i = 0; i < poolSize; i++) {
            double p1 = rng.nextDouble() * cumulativeFitnesses[cumulativeFitnesses.length - 1];
            int index1 = Arrays.binarySearch(cumulativeFitnesses, p1);
            if (index1 < 0) {
                // Convert negative insertion point to array index.
                index1 = Math.abs(index1 + 1);
            }

            double p2 = rng.nextDouble() * cumulativeFitnesses[cumulativeFitnesses.length - 1];
            int index2 = Arrays.binarySearch(cumulativeFitnesses, p2);
            if (index2 < 0) {
                // Convert negative insertion point to array index.
                index2 = Math.abs(index2 + 1);
            }

            pool.add(population.get(index1));
            pool.add(population.get(index2));
        }

        return pool;
    }

    private double getAdjustedFitness(double fitness) {
        return -Math.log(fitness);
    }
}
