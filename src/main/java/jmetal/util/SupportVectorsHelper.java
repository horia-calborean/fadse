package jmetal.util;

import jmetal.base.Solution;
import jmetal.base.SolutionSet;

public class SupportVectorsHelper {
    public SolutionSet getAFZGASupportVectors(ApparentFrontRanking ranking, int nrOfZones, int supportVectorsPerFront) {
        SolutionSet supportVectors = new SolutionSet(nrOfZones * supportVectorsPerFront);
        for (int i = 0; i < nrOfZones; i++) {
            SolutionSet subFront = ranking.getSubfront(i);
            subFront = subFront.deepClone();
            subFront.sort(new jmetal.base.operator.comparator.CrowdingComparator());
            int maxSolutionsFromFront = Math.min(subFront.size(), supportVectorsPerFront);
            for (int j = 0; j < maxSolutionsFromFront; j++) {
                supportVectors.add(subFront.get(j));
            }
        }

        return supportVectors;
    }

    public SolutionSet getNSGAAFRSupportVectors(IRanking ranking, int index, SolutionSet front, int minVectors) {
        SolutionSet supportVectors;//if we have at least N+1 individuals on the first front
        if (front.size() >= minVectors) {
            supportVectors = front;
        } else {
            supportVectors = new SolutionSet();

            for (int i = 0; i < front.size(); i++) {
                supportVectors.add(front.get(i));
            }

            int l = index + 1;

            while (supportVectors.size() < minVectors) {
                SolutionSet nextFront = ranking.getSubfront(l);
                int necessary = minVectors - supportVectors.size();
                int size = nextFront.size() <= necessary ? nextFront.size() : necessary;
                for (int i = 0; i < size; i++) {
                    supportVectors.add(nextFront.get(i));
                }
                l++;
            }

        }

        return supportVectors;
    }

    public SolutionSet getSupportVectorsCloseToAxis(SolutionSet population, IRanking ranking, int nrOfFronts, int supportVectorsPerAxis, int xAxisAngle, int yAxisAngle) {
        SolutionSet supportVectors = new SolutionSet(2 * supportVectorsPerAxis);

        double xAxisRadians = Math.toRadians(xAxisAngle);
        double yAxisRadians = Math.toRadians(yAxisAngle);

        int[] counts = new int[2];
        nrOfFronts = nrOfFronts < ranking.getNumberOfSubfronts() ? nrOfFronts : ranking.getNumberOfSubfronts();
        for (int i = 0; i < nrOfFronts; i++) {
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
            SolutionSet virtualSupportVectors = VirtualSupportVectors.getVirtualSupportVectorsCloseToX(population, supportVectorsPerAxis - counts[0]);
            for (int i = 0; i < virtualSupportVectors.size(); i++) {
                supportVectors.add(virtualSupportVectors.get(i));
            }
        }

        if (counts[1] < supportVectorsPerAxis) {
            SolutionSet virtualSupportVectors = VirtualSupportVectors.getVirtualSupportVectorsCloseToY(population, supportVectorsPerAxis - counts[1]);
            for (int i = 0; i < virtualSupportVectors.size(); i++) {
                supportVectors.add(virtualSupportVectors.get(i));
            }
        }

        return supportVectors;
    }

    public SolutionSet combine(SolutionSet set1, SolutionSet set2) {
        SolutionSet combined = new SolutionSet(set1.size() + set2.size());

        for (int i = 0; i < set1.size(); i++) {
            if (!supportVectorsSetContains(combined, set1.get(i))) {
                combined.add(set1.get(i));
            }
        }

        for (int i = 0; i < set2.size(); i++) {
            if (!supportVectorsSetContains(combined, set2.get(i))) {
                combined.add(set2.get(i));
            }
        }

        return combined;
    }

    private boolean supportVectorsSetContains(SolutionSet supportVectors, Solution solution) {
        for (int i = 0; i < supportVectors.size(); i++) {
            Solution current = supportVectors.get(i);

            boolean equalObjectives = true;
            for (int j = 0; j < current.numberOfObjectives(); j++) {
                if (current.getObjective(j) != solution.getObjective(j)) {
                    equalObjectives = false;
                    break;
                }
            }

            if (equalObjectives) {
                return true;
            }
        }

        return false;
    }
}
