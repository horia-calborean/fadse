package jmetal.util;

import jmetal.base.Solution;
import jmetal.base.SolutionSet;

import java.util.LinkedList;

public class ApparentFrontRanking {

    private SolutionSet[] ranking_  ;

    public ApparentFrontRanking(ApparentFront af, SolutionSet solutionSet, int nrZones) {
        af.fit(solutionSet);

        LinkedList<Solution>[] solutionsInZones = new LinkedList[nrZones];
        for(int i=0;i<nrZones;i++){
            solutionsInZones[i] = new LinkedList<Solution>();
        }

        for (int i=0;i<solutionSet.size();i++) {
            Solution currentSolution = solutionSet.get(i);
            double distance = AfDistance.ComputeDistance(af, currentSolution);
            if(distance < -0.05){
                solutionsInZones[2].add(currentSolution);
            }
            else if(distance > 0.05){
                solutionsInZones[0].add(currentSolution);
            }
            else{
                solutionsInZones[1].add(currentSolution);
            }
        }


        ranking_ = new SolutionSet[nrZones];
        for(int i = 0;i<nrZones;i++){
            LinkedList<Solution> currentZoneSolutions = solutionsInZones[i];
            ranking_[i] = new SolutionSet(currentZoneSolutions.size());
            for (Solution sol : currentZoneSolutions) {
                ranking_[i].add(sol);
            }
        }
    }

    /**
     * Returns a <code>SolutionSet</code> containing the solutions of a given rank.
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
