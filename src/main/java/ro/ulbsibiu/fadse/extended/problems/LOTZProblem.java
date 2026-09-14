package ro.ulbsibiu.fadse.extended.problems;

import org.uma.jmetal.problem.doubleproblem.impl.AbstractDoubleProblem;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;

import java.util.ArrayList;
import java.util.List;

public class LOTZProblem extends AbstractDoubleProblem {

    public LOTZProblem(Integer numberOfVariables) {
        numberOfObjectives = 2;
        numberOfConstraints = 0;
        name = "LOTZ";

        List<Double> lowerLimit = new ArrayList<>(numberOfVariables);
        List<Double> upperLimit = new ArrayList<>(numberOfVariables);
        for (int var = 0; var < numberOfVariables; var++) {
            lowerLimit.set(var, 0.0);
            upperLimit.set(var, 1.0);
        }
        variableBounds(lowerLimit, upperLimit);
    }

    @Override
    public DoubleSolution evaluate(DoubleSolution solution) {
        List<Double> gen = solution.variables();
        int[] x = new int[solution.variables().size()];
        double[] f = new double[numberOfObjectives];
        int k = solution.variables().size() - numberOfObjectives + 1;
        for (int i = 0; i < solution.variables().size(); i++) {
            x[i] = gen.get(i).intValue();
        }
        boolean onesBest = true;
        for (int objective = 0; objective < numberOfObjectives; objective++) {
            int sum = 0;
            for (int i = 0; i < solution.variables().size(); i++) {
                int prod = 1;
                if (onesBest) {
                    for (int j = 0; j <= i; j++) {
                        prod *=  x[j];
                    }
                    sum = sum + prod;
                } else {
                    for (int j = i; j < solution.variables().size(); j++) {
                        prod *= (1 - x[j]);
                    }
                    sum = sum + prod;
                }
            }
            onesBest = !onesBest;// switch the function between
            solution.objectives()[objective] = sum;
            // problem
        }

        return  solution;
    }
}
