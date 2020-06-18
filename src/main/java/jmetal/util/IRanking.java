package jmetal.util;

import jmetal.base.SolutionSet;

public interface IRanking {
    SolutionSet getSubfront(int rank);

    int getNumberOfSubfronts();
}
