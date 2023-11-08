package jmetal.algorithm.multiobjective.omopso;

import jmetal.core.algorithm.AlgorithmBuilder;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.operator.mutation.impl.NonUniformMutation;
import jmetal.core.operator.mutation.impl.UniformMutation;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.evaluator.SolutionListEvaluator;

/** Class implementing the OMOPSO algorithm */
public class OMOPSOBuilder implements AlgorithmBuilder<OMOPSO> {
  protected DoubleProblem problem;
  protected SolutionListEvaluator<DoubleSolution> evaluator;

  private int swarmSize = 100 ;
  private int archiveSize = 100 ;
  private int maxIterations = 25000 ;

  private double eta = 0.0075 ;

  private UniformMutation uniformMutation ;
  private NonUniformMutation nonUniformMutation ;

  public OMOPSOBuilder(DoubleProblem problem, SolutionListEvaluator<DoubleSolution> evaluator) {
    this.evaluator = evaluator ;
    this.problem = problem ;
  }

  public OMOPSOBuilder setSwarmSize(int swarmSize) {
    this.swarmSize = swarmSize ;

    return this ;
  }

  public OMOPSOBuilder setArchiveSize(int archiveSize) {
    this.archiveSize = archiveSize ;

    return this ;
  }

  public OMOPSOBuilder setMaxIterations(int maxIterations) {
    this.maxIterations = maxIterations ;

    return this ;
  }

  public OMOPSOBuilder setUniformMutation(MutationOperator<DoubleSolution> uniformMutation) {
    this.uniformMutation = (UniformMutation)uniformMutation ;

    return this ;
  }

  public OMOPSOBuilder setNonUniformMutation(MutationOperator<DoubleSolution> nonUniformMutation) {
    this.nonUniformMutation = (NonUniformMutation)nonUniformMutation ;

    return this ;
  }

  public OMOPSOBuilder setEta(double eta) {
    this.eta = eta ;

    return this ;
  }

  /* Getters */
  public int getArchiveSize() {
    return archiveSize;
  }

  public int getSwarmSize() {
    return swarmSize;
  }

  public int getMaxIterations() {
    return maxIterations;
  }

  public UniformMutation getUniformMutation() {
    return uniformMutation;
  }

  public NonUniformMutation getNonUniformMutation() {
    return nonUniformMutation;
  }

  public OMOPSO build() {
    return new OMOPSO(problem, evaluator, swarmSize, maxIterations, archiveSize, eta, uniformMutation,
        nonUniformMutation) ;
  }
}
