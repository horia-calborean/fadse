package jmetal.component.tests.catalogue.common.termination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jmetal.component.catalogue.common.termination.impl.TerminationByQualityIndicator;
import jmetal.core.qualityindicator.QualityIndicator;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.errorchecking.exception.EmptyCollectionException;
import jmetal.core.util.errorchecking.exception.InvalidConditionException;
import jmetal.core.util.errorchecking.exception.NegativeValueException;
import jmetal.core.util.errorchecking.exception.NullParameterException;

class TerminationByQualityIndicatorTest {

  private QualityIndicator qualityIndicator;
  private double[][] referenceFront;
  private double percentage;
  private int evaluationsLimit;
  TerminationByQualityIndicator termination;


  @BeforeEach
  void setUp() {
    qualityIndicator = mock(QualityIndicator.class);
    referenceFront = new double[][]{{1.0, 0.0}, {0.0, 1.0}};
    percentage = 80;
    evaluationsLimit = 1000;
  }

  @Test
  void TheConstructorInitializesCorrectlyTheFields() {
    termination = new TerminationByQualityIndicator(qualityIndicator, referenceFront, percentage,
        evaluationsLimit);
    assertThat(termination.getQualityIndicator()).isSameAs(qualityIndicator);
    assertThat(termination.getEvaluationsLimit()).isEqualTo(evaluationsLimit);
  }

  @Test
  void TheConstructorRaisesAnExceptionIfTheQualityIndicatorIsNull() {
    assertThatThrownBy(() -> new TerminationByQualityIndicator(null, referenceFront, percentage,
        evaluationsLimit)).isInstanceOf(NullParameterException.class) ;
  }

  @Test
  void TheConstructorRaisesAnExceptionIfTheReferenceFrontIsNull() {
    assertThatThrownBy(() -> new TerminationByQualityIndicator(qualityIndicator, null, percentage,
        evaluationsLimit)).isInstanceOf(NullParameterException.class) ;
  }

  @Test
  void TheConstructorRaisesAnExceptionIfTheReferenceFrontIsEmpty() {
    double [][] referenceFront = new double[][]{} ;
    assertThatThrownBy(() -> new TerminationByQualityIndicator(qualityIndicator, referenceFront, percentage,
        evaluationsLimit)).isInstanceOf(InvalidConditionException.class) ;
  }

  @Test
  void TheConstructorRaisesAnExceptionIfTheReferenceFrontHasOnePoint() {
    double [][] referenceFront = new double[][]{{1.0, 0.0}} ;
    assertThatThrownBy(() -> new TerminationByQualityIndicator(qualityIndicator, referenceFront, percentage,
        evaluationsLimit)).isInstanceOf(InvalidConditionException.class) ;
  }

  @Test
  void TheConstructorRaisesAnExceptionIfThePercentageIsANegativeValue() {
    int percentage = -1 ;
    assertThatThrownBy(() -> new TerminationByQualityIndicator(qualityIndicator, referenceFront, percentage,
        evaluationsLimit)).isInstanceOf(NegativeValueException.class) ;
  }

  @Test
  void TheConstructorRaisesAnExceptionIfTheEvaluationsLimitIsANegativeValue() {
    int evaluationsLimit = -1 ;
    assertThatThrownBy(() -> new TerminationByQualityIndicator(qualityIndicator, referenceFront, percentage,
        evaluationsLimit)).isInstanceOf(NegativeValueException.class) ;
  }

  @Test
  void isMetRaisesAnExceptionIfTheEVALUATIONSFieldIsNotPresent() {
    termination = new TerminationByQualityIndicator(qualityIndicator, referenceFront, percentage,
        evaluationsLimit);

    Map<String, Object> algorithmStatusData = new HashMap<>();
    algorithmStatusData.put("POPULATION", new ArrayList<DoubleSolution>().add(mock(DoubleSolution.class))) ;
    assertThatThrownBy(() -> termination.isMet(algorithmStatusData)).isInstanceOf(
        NullParameterException.class);
  }

  @Test
  void isMetRaisesAnExceptionIfThePOPULATIONFieldIsNotPresent() {
    termination = new TerminationByQualityIndicator(qualityIndicator, referenceFront, percentage,
        evaluationsLimit);

    Map<String, Object> algorithmStatusData = new HashMap<>();
    algorithmStatusData.put("EVALUATIONS", 100) ;
    assertThatThrownBy(() -> termination.isMet(algorithmStatusData)).isInstanceOf(
        NullParameterException.class);
  }

  @Test
  void isMetRaisesAnExceptionIfThePOPULATIONFieldIsAnEmptyCollection() {
    termination = new TerminationByQualityIndicator(qualityIndicator, referenceFront, percentage,
        evaluationsLimit);

    Map<String, Object> algorithmStatusData = new HashMap<>();
    algorithmStatusData.put("EVALUATIONS", 100) ;
    algorithmStatusData.put("POPULATION", new ArrayList<DoubleSolution>()) ;
    assertThatThrownBy(() -> termination.isMet(algorithmStatusData)).isInstanceOf(
        EmptyCollectionException.class);
  }
  @Test
  void isMetRaisesReturnsTrueIfTheNumberOfEvaluationsReachesTheLimit() {
    termination = new TerminationByQualityIndicator(qualityIndicator, referenceFront, percentage,
        evaluationsLimit);

    Map<String, Object> algorithmStatusData = new HashMap<>();
    algorithmStatusData.put("EVALUATIONS", evaluationsLimit) ;
    List<DoubleSolution> population = new ArrayList<>() ;
    population.add(mock(DoubleSolution.class)) ;
    algorithmStatusData.put("POPULATION", population) ;

    assertThat(termination.isMet(algorithmStatusData)).isTrue() ;
    assertThat(termination.evaluationsLimitReached()).isTrue() ;
  }
}