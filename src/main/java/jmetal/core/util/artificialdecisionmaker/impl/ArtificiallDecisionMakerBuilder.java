package jmetal.core.util.artificialdecisionmaker.impl;

import java.util.List;
import jmetal.core.algorithm.AlgorithmBuilder;
import jmetal.core.problem.Problem;
import jmetal.core.solution.Solution;
import jmetal.core.util.artificialdecisionmaker.InteractiveAlgorithm;
import jmetal.core.util.errorchecking.JMetalException;

/**
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class ArtificiallDecisionMakerBuilder<S extends Solution<?>> implements AlgorithmBuilder<ArtificialDecisionMakerDecisionTree<S>> {

  /**
   * Artificial Decsion Maaker
   */
  private final Problem<S> problem;
  private int maxEvaluations;
  private InteractiveAlgorithm<S,List<S>> algorithm;
  private double considerationProbability;
  private double tolerance;
  private List<Double> rankingCoeficient;
  private List<Double> asp;

  /**
   * ArtificiallDecisionMakerBuilder constructor
   */
  public ArtificiallDecisionMakerBuilder(Problem<S> problem, InteractiveAlgorithm<S,List<S>> algorithm) {
    this.problem = problem;
    this.maxEvaluations = 25000;
    this.algorithm = algorithm;
  }

  public ArtificiallDecisionMakerBuilder<S> setMaxEvaluations(int maxEvaluations) {
    if (maxEvaluations < 0) {
      throw new JMetalException("maxEvaluations is negative: " + maxEvaluations);
    }
    this.maxEvaluations = maxEvaluations;

    return this;
  }

  public ArtificiallDecisionMakerBuilder<S> setAsp(List<Double> asp) {
    this.asp = asp;
    return this;
  }

  public ArtificiallDecisionMakerBuilder<S> setAlgorithm(InteractiveAlgorithm<S,List<S>> algorithm) {
    if (algorithm==null) {
      throw new JMetalException("algorithm is null");
    }
    this.algorithm = algorithm;
    return this;
  }

  public ArtificiallDecisionMakerBuilder<S> setConsiderationProbability(double considerationProbability) {
    if (considerationProbability < 0.0) {
      throw new JMetalException("considerationProbability is negative: " + considerationProbability);
    }
    this.considerationProbability = considerationProbability;
    return this;
  }

  public ArtificiallDecisionMakerBuilder<S> setTolerance(double tolerance) {
    if (tolerance < 0.0) {
      throw new JMetalException("tolerance is negative: " + tolerance);
    }
    this.tolerance = tolerance;
    return this;
  }

  public ArtificiallDecisionMakerBuilder<S> setRankingCoeficient(List<Double> rankingCoeficient) {
    this.rankingCoeficient = rankingCoeficient;
    return this;
  }

  public ArtificiallDecisionMakerBuilder<S> setNumberReferencePoints(int numberReferencePoints) {
    return this;
  }



  public ArtificialDecisionMakerDecisionTree<S> build() {
    ArtificialDecisionMakerDecisionTree<S> algorithmRun = null ;
    algorithmRun = new ArtificialDecisionMakerDecisionTree<S>(problem,algorithm,considerationProbability,tolerance, maxEvaluations,
          rankingCoeficient,asp);

    return algorithmRun ;
  }


  /* Getters */
  public Problem<S> getProblem() {
    return problem;
  }

  public int getMaxIterations() {
    return maxEvaluations;
  }

}
