package jmetal.util;

import jmetal.base.Solution;
import jmetal.base.SolutionSet;
import jmetal.base.operator.comparator.ObjectiveComparator;

public class AFZGASupportVectors {
    private ApparentFrontRanking ranking;
    private SolutionSet population;
    private SolutionSet supportVectors;

    public AFZGASupportVectors(SolutionSet population, ApparentFrontRanking ranking, int supportVectorsNumber) {
        this.population = population;
        this.ranking = ranking;
        this.supportVectors = new SolutionSet(supportVectorsNumber);
    }

    public SolutionSet getSupportVectors() {
        return supportVectors;
    }

    public void addSupportVectorsFromFronts(int nrOfZones, int supportVectorsPerFront) {
        for (int i = 0; i < nrOfZones; i++) {
            SolutionSet subFront = ranking.getSubfront(i);
            subFront = subFront.deepClone();
            subFront.sort(new jmetal.base.operator.comparator.CrowdingComparator());
            int maxSolutionsFromFront = Math.min(subFront.size(), supportVectorsPerFront);
            for (int j = 0; j < maxSolutionsFromFront; j++) {
                supportVectors.add(subFront.get(j));
            }
        }
    }

    public void addSupportVectorsCloseToAxis(int nrOfZones, int supportVectorsPerAxis, int xAxisAngle, int yAxisAngle) {
        double xAxisRadians = Math.toRadians(xAxisAngle);
        double yAxisRadians = Math.toRadians(yAxisAngle);

        int[] counts = new int[2];
        for (int i = 0; i < nrOfZones; i++) {
            SolutionSet front = ranking.getSubfront(i);

            GapObjectivesNormalizer normalizer = new GapObjectivesNormalizer(front);
            normalizer.scaleObjectives();

            int j = 0;
            while (j < front.size() &&
                    (counts[0] < supportVectorsPerAxis || counts[1] < supportVectorsPerAxis)) {
                double alfa1 = Math.atan(front.get(j).getObjective(0) / front.get(j).getObjective(1));
                double alfa2 = Math.atan(front.get(j).getObjective(1) / front.get(j).getObjective(0));

                if (alfa1 < yAxisRadians && counts[0] < supportVectorsPerAxis) {
                    supportVectors.add(front.get(j));
                    counts[0]++;
                } else if (alfa2 < xAxisRadians && counts[1] < supportVectorsPerAxis) {
                    supportVectors.add(front.get(j));
                    counts[1]++;
                }

                j++;
            }

            normalizer.restoreObjectives();
        }

        if (counts[0] < supportVectorsPerAxis) {
            addVirtualSupportVectorsCloseToY(supportVectorsPerAxis - counts[0]);
        }

        if (counts[1] < supportVectorsPerAxis) {
            addVirtualSupportVectorsCloseToX(supportVectorsPerAxis - counts[1]);
        }
    }

    private void addVirtualSupportVectorsCloseToX(int number) {
        SolutionSet sortedPopulation = population.deepClone();
        sortedPopulation.sort(new ObjectiveComparator(0));

        double startHC = 4500;
        for (int i = 0; i < number; i++) {
            Solution solMinIPC = new Solution(sortedPopulation.get(5));
            solMinIPC.setObjective(0, sortedPopulation.get(5).getObjective(0));
            solMinIPC.setObjective(1, startHC + i * 50);

            supportVectors.add(solMinIPC);
        }
    }

    private void addVirtualSupportVectorsCloseToY(int number) {
        SolutionSet sortedPopulation = population.deepClone();
        sortedPopulation.sort(new ObjectiveComparator(1));

        double startIPC = 2.5;
        for (int i = 0; i < number; i++) {
            Solution solMinHC = new Solution(sortedPopulation.get(5));
            solMinHC.setObjective(0, startIPC + 0.5 * i);
            solMinHC.setObjective(1, sortedPopulation.get(5).getObjective(1));

            supportVectors.add(solMinHC);
        }
    }
}
