package jmetal.util;

import jmetal.base.Solution;
import jmetal.base.SolutionSet;

import java.util.ArrayList;
import java.util.List;

public class GapObjectivesNormalizer {
    SolutionSet solutionSet_;

    List<double[]> objectivesBackup_;

    public GapObjectivesNormalizer(SolutionSet solutionSet) {
        solutionSet_ = solutionSet;
    }

    public void scaleObjectives() {
        objectivesBackup_ = new ArrayList<double[]>();

        for (int i = 0; i < solutionSet_.size(); i++) {
            Solution solution = solutionSet_.get(i);

            objectivesBackup_.add(new double[]{solution.getObjective(0), solution.getObjective(1)});

            solution.setObjective(0, scaleIPC(solution.getObjective(0)));
            solution.setObjective(1, scaleHC(solution.getObjective(1)));
        }
    }

    public void restoreObjectives() {
        for (int i = 0; i < solutionSet_.size(); i++) {
            Solution solution = solutionSet_.get(i);
            double[] objectives = objectivesBackup_.get(i);

            solution.setObjective(0, objectives[0]);
            solution.setObjective(1, objectives[1]);
        }
    }

    private double scaleIPC(double CPI) {
        return 1.0 / CPI / 4.0;
    }

    private double scaleHC(double HC) {
        return (10000.0 - HC) / 10000.0;
    }
}
