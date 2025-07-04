package core.qualityindicator;
import org.uma.jmetal.qualityindicator.impl.Spread;
import org.uma.jmetal.qualityindicator.impl.hypervolume.impl.WFGHypervolume;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.util.ranking.Ranking;
import org.uma.jmetal.util.ranking.impl.FastNonDominatedSortRanking;

import java.util.List;

//TODO - the integration of the quality indicators was done in a rush. Improvements are needed
//TODO - currently only focused on normalized values
public abstract class QualityIndicator<S extends Solution<?>> {
    protected double[][] referenceFront;

    protected double[][] generateNormalizedReferenceFront(int numberOfObjectives) {
        referenceFront = new double[numberOfObjectives + 1][numberOfObjectives];

        for (int i = 0; i < numberOfObjectives; i++) {
            for (int j = 0; j < numberOfObjectives; j++) {
                referenceFront[i][j] = 1.0;
            }
            referenceFront[i][i] = 0.0; // best value in objective i
        }

        // Add center point with 0.5 in all objectives
        for (int j = 0; j < numberOfObjectives; j++) {
            referenceFront[numberOfObjectives][j] = 0.5;
        }

        return referenceFront;
    }

    protected double[][] normalizeFront(List<S> population){
        Ranking<S> fronts = new FastNonDominatedSortRanking<S>().compute(population);
        List<S> firstFront = fronts.getSubFront(0);

        double[][] objectiveValues = convertToMatrix(firstFront);
        int numObjectives = objectiveValues[0].length;
        double[] minValues = new double[numObjectives];
        double[] maxValues = new double[numObjectives];

        // Initialize with first point
        for (int i = 0; i < numObjectives; i++) {
            minValues[i] = objectiveValues[0][i];
            maxValues[i] = objectiveValues[0][i];
        }

        // Find min and max
        for (int i = 1; i < objectiveValues.length; i++) {
            for (int j = 0; j < numObjectives; j++) {
                if (objectiveValues[i][j] < minValues[j]) minValues[j] = objectiveValues[i][j];
                if (objectiveValues[i][j] > maxValues[j]) maxValues[j] = objectiveValues[i][j];
            }
        }
        double[][] normalized = new double[objectiveValues.length][numObjectives];

        for (int i = 0; i < objectiveValues.length; i++) {
            for (int j = 0; j < numObjectives; j++) {
                if (maxValues[j] - minValues[j] == 0) {
                    normalized[i][j] = 0.0; // Avoid division by zero
                } else {
                    normalized[i][j] = (objectiveValues[i][j] - minValues[j]) / (maxValues[j] - minValues[j]);
                }
            }
        }

        return normalized;
    }

    protected double[][] convertToMatrix(List<S> population) {
        int numberOfObjectives = population.get(0).objectives().length;
        double[][] matrix = new double[population.size()][numberOfObjectives];

        for (int i = 0; i < population.size(); i++) {
            Solution<?> sol = population.get(i);
            for (int j = 0; j < numberOfObjectives; j++) {
                matrix[i][j] = sol.objectives()[j];
            }
        }
//        Ranking<S> fronts = new FastNonDominatedSortRanking<S>().compute(population);
//        List<S> firstFront = fronts.getSubFront(0);
//        double[][] matrix = new double[convertToMatrix(firstFront).length][2];
//        for (int i = 0; i < convertToMatrix(firstFront).length; i++) {
//            double x = i / (double)(convertToMatrix(firstFront).length - 1);
//            referenceFrontNorm[i][0] = x;
//            referenceFrontNorm[i][1] = 1.0 - x;
//        }
        return matrix;
    }

}
