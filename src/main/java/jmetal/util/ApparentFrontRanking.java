package jmetal.util;

import jmetal.base.Solution;
import jmetal.base.SolutionSet;

import java.util.LinkedList;

public class ApparentFrontRanking implements IRanking {

    private SolutionSet[] ranking_;

    public ApparentFrontRanking(ApparentFront apparentFront, SolutionSet solutionSet, int nrZones) {

        LinkedList<Solution>[] solutionsInZones = new LinkedList[nrZones];
        for (int i = 0; i < nrZones; i++) {
            solutionsInZones[i] = new LinkedList<Solution>();
        }


        GapObjectivesNormalizer normalizer = new GapObjectivesNormalizer(solutionSet);
        normalizer.scaleObjectives();

        for (int i = 0; i < solutionSet.size(); i++) {
            Solution currentSolution = solutionSet.get(i);

            double distance =
                    AfDistance.ComputeDistance(apparentFront, currentSolution);
            double membership = AfMembership.compute(apparentFront, currentSolution);
            currentSolution.setAfrMembership(membership);
            if (distance < -0.05) {
                solutionsInZones[3].add(currentSolution);
            } else if (distance > 0.05) {
                solutionsInZones[0].add(currentSolution);
            } else if (distance < 0) {
                solutionsInZones[2].add(currentSolution);
            } else {
                solutionsInZones[1].add(currentSolution);
            }
        }

        normalizer.restoreObjectives();

        ranking_ = new SolutionSet[nrZones];
        for (int i = 0; i < nrZones; i++) {
            LinkedList<Solution> currentZoneSolutions = solutionsInZones[i];
            ranking_[i] = new SolutionSet(currentZoneSolutions.size());
            for (Solution sol : currentZoneSolutions) {
                ranking_[i].add(sol);
            }
        }
    }

    /**
     * Returns a <code>SolutionSet</code> containing the solutions of a given rank.
     *
     * @param rank The rank
     * @return Object representing the <code>SolutionSet</code>.
     */
    public SolutionSet getSubfront(int rank) {
        return ranking_[rank];
    } // getSubFront

    /**
     * Returns the total number of subFronts founds.
     */
    public int getNumberOfSubfronts() {
        return ranking_.length;
    } // getNumberOfSubfronts
}
