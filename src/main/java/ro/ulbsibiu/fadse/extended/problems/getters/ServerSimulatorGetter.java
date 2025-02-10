package ro.ulbsibiu.fadse.extended.problems.getters;

import ro.ulbsibiu.fadse.environment.Environment;

public class ServerSimulatorGetter implements ProblemParametersGetter {
    @Override
    public Object[] get(Environment environment) {
        return new Environment[]{environment};
    }
}