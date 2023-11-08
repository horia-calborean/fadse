package jmetal.parallel.synchronous;

import java.util.List;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import jmetal.core.problem.Problem;
import jmetal.core.util.evaluator.SolutionListEvaluator;

/**
 * Class implementing an {@link SolutionListEvaluator} based on Apache Spark.
 * Reference: C. Barba-González, J. García-Nieto, Antonio J. Nebro, J.F.Aldana-Montes: Multi-objective Big
 * Data Optimization with jMetal and Spark. EMO 2017". DOI: http://dx.doi.org/10.1007/978-3-319-54157-0_2
 * @param <S>
 *
 * @author Antonio J. Nebro */
@SuppressWarnings("serial")
public class SparkSolutionListEvaluator<S> implements SolutionListEvaluator<S> {

  private JavaSparkContext sparkContext ;

  public SparkSolutionListEvaluator(JavaSparkContext sparkContext) {
    this.sparkContext = sparkContext;
  }

  @Override
  public List<S> evaluate(List<S> solutionList, Problem<S> problem) {
    JavaRDD<S> solutionsToEvaluate = sparkContext.parallelize(solutionList);
    JavaRDD<S> evaluatedSolutions = solutionsToEvaluate.map(problem::evaluate);

    return evaluatedSolutions.collect() ;
  }

  @Override
  public void shutdown() {
    sparkContext.stop();
  }
}
