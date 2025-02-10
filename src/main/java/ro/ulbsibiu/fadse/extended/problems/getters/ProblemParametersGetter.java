package ro.ulbsibiu.fadse.extended.problems.getters;

import ro.ulbsibiu.fadse.environment.Environment;

public interface ProblemParametersGetter {
    Object[] get(Environment environment);
}