package jmetal.util;

import jmetal.base.SolutionSet;

public abstract class ObjectivesNormalizer {
    public abstract void scaleObjectives( SolutionSet solutionSet_);

    public abstract void restoreObjectives( SolutionSet solutionSet_);
}
