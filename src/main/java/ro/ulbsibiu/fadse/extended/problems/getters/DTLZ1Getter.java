package ro.ulbsibiu.fadse.extended.problems.getters;

import ro.ulbsibiu.fadse.environment.Environment;

public class DTLZ1Getter implements ProblemParametersGetter {
    @Override
    public Object[] get(Environment environment) {
        int numberOfVariables = environment.getInputDocument().getParameters().length;
        int numberOfObjectives = environment.getInputDocument().getObjectives().size();

        return new Object[]
                {numberOfVariables, numberOfObjectives};
    }
}