package jmetal.lab.experiment.util;

import jmetal.core.problem.Problem;
import jmetal.core.solution.Solution;

/**
 * Class used to add a tag field to a problem.
 *
 * @author Antonio J. Nebro <ajnebro@uma.es>
 */
public class ExperimentProblem<S extends Solution<?>> {
  private Problem<S> problem ;
  private String tag ;
  private String referenceFront;

  public ExperimentProblem(Problem<S> problem, String tag) {
    this.problem = problem;
    this.tag = tag;
    this.referenceFront = this.problem.name() + ".csv";
  }

  public ExperimentProblem(Problem<S> problem) {
    this(problem,problem.name());
  }

  public ExperimentProblem<S> setReferenceFront(String referenceFront) {
    this.referenceFront = referenceFront;
    return this;
  }

  public Problem<S> getProblem() {
    return problem;
  }

  public String getTag() {
    return tag ;
  }

  public String getReferenceFront() {return referenceFront;}
}
