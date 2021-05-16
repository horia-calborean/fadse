package jmetal.util;

import jmetal.base.Solution;
import jmetal.base.SolutionSet;

import java.util.ArrayList;
import java.util.List;

public class SyntheticObjectivesNormalizer {
    SolutionSet solutionSet_;

    List<double[]> objectivesBackup_;

    public SyntheticObjectivesNormalizer(SolutionSet solutionSet) {
        solutionSet_ = solutionSet;
    }

    public void scaleObjectives() {
        objectivesBackup_ = new ArrayList<double[]>();

        for (int i = 0; i < solutionSet_.size(); i++) {
            Solution solution = solutionSet_.get(i);
            int nrObjectives = solution.numberOfObjectives();
            double[] objectives = new double[nrObjectives];
            for (int j = 0; j< solution.numberOfObjectives() ; j++){
                       objectives[j] = solution.getObjective(j);
                       solution.setObjective(j, scaleObjective(solution.getObjective(j)));
            }
            
            objectivesBackup_.add(objectives);
        }
    }

    public void restoreObjectives() {
        for (int i = 0; i < solutionSet_.size(); i++) {
            Solution solution = solutionSet_.get(i);
            double[] objectives = objectivesBackup_.get(i);

             for (int j = 0; j< solution.numberOfObjectives() ; j++){
                  solution.setObjective(j, objectives[j]);
             }
        }
    }

    private double scaleObjective(double CPI) {
        return 500 - CPI;
    }
}
