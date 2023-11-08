package jmetal.core.operator.localsearch;

import jmetal.core.operator.Operator;

/**
 * Interface representing a local search operator
 *
 * Created by cbarba on 5/3/15.
 */
public interface LocalSearchOperator <Source> extends Operator<Source, Source> {
  int numberOfImprovements() ;
  int numberOfEvaluations() ;
}
