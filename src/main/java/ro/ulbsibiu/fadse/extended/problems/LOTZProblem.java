package ro.ulbsibiu.fadse.extended.problems;

import jmetal.base.Problem;
import jmetal.base.Solution;
import jmetal.base.Variable;
import jmetal.util.JMException;

public class LOTZProblem extends Problem {

    public LOTZProblem(String solutionType, Integer numberOfVariables) throws ClassNotFoundException {
        numberOfVariables_ = numberOfVariables.intValue();
        numberOfObjectives_ = 2;
        numberOfConstraints_ = 0;
        problemName_ = "LOTZ";

        lowerLimit_ = new double[numberOfVariables_];
        upperLimit_ = new double[numberOfVariables_];
        for (int var = 0; var < numberOfVariables; var++) {
            lowerLimit_[var] = 0.0;
            upperLimit_[var] = 1.0;
        } //for

//        if (solutionType.compareTo("BinaryReal") == 0) {
//            solutionType_ = new BinaryRealSolutionType(this);
//        } else if (solutionType.compareTo("Real") == 0) {
//            solutionType_ = new RealSolutionType(this);
//        } else {
//            System.out.println("Error: solution type " + solutionType + " invalid");
//            System.exit(-1);
//        }
    }

    public void evaluate(Solution solution) throws JMException {
        Variable[] gen = solution.getDecisionVariables();
        int[] x = new int[numberOfVariables_];
        double[] f = new double[numberOfObjectives_];
        int k = numberOfVariables_ - numberOfObjectives_ + 1;
        for (int i = 0; i < numberOfVariables_; i++) {
            x[i] = (int) gen[i].getValue();
        }
        boolean onesBest = true;
        for (int objective = 0; objective < numberOfObjectives_; objective++) {
            int sum = 0;
            for (int i = 0; i < numberOfVariables_; i++) {
                int prod = 1;
                if (onesBest) {
                    for (int j = 0; j <= i; j++) {
                        prod *=  x[j];
                    }
                    sum = sum + prod;
                } else {
                    for (int j = i; j < numberOfVariables_; j++) {
                        prod *= (1 - x[j]);
                    }
                    sum = sum + prod;
                }
            }
            onesBest = !onesBest;// switch the function between
            solution.setObjective(objective,sum);
            // problem
        }

    }   
}
