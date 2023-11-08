package jmetal.core.util.artificialdecisionmaker;

import java.util.List;
import jmetal.core.algorithm.Algorithm;

public interface InteractiveAlgorithm<S,R> extends Algorithm<R> {
  void updatePointOfInterest(List<Double> newReferencePoints);
}
