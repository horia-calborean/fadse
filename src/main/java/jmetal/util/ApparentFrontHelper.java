package jmetal.util;

import jmetal.base.SolutionSet;

public class ApparentFrontHelper {

    public static void FitTheFront(ApparentFront af, SolutionSet supportVectors, ObjectivesNormalizer normalizer) {
        //turn to maximization problem
        normalizer.scaleObjectives(supportVectors);

        af.fit(supportVectors);

        //restore to minimization problem
        normalizer.restoreObjectives(supportVectors);
    }

    public static SolutionSet ComputeSupportVectors(Ranking ranking, int index, SolutionSet front, int minVectors) {
        SolutionSet supportVectors;//if we have at least N+1 individuals on the first front
        if (front.size() >= minVectors) {
            supportVectors = front;
        } else {
            supportVectors = new SolutionSet(minVectors);

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
}
