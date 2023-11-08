package jmetal.auto.tests.parameter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jmetal.auto.parameter.IntegerParameter;
import org.junit.jupiter.api.Test;
import jmetal.core.util.errorchecking.JMetalException;
import jmetal.core.util.errorchecking.exception.InvalidConditionException;

class IntegerParameterTest {

  @Test
  void constructorMustInitializeTheFieldsCorrectly() {
    int lowerBound = 1;
    int upperBound = 10;
    var realParameter = new IntegerParameter("integerParameter", lowerBound,
        upperBound);

    assertThat(realParameter.name()).isEqualTo("integerParameter");
    assertThat(realParameter.validValues()).containsExactly(lowerBound, upperBound);
  }

  @Test
  void constructorMustRaiseAnExceptionInTheLowerBoundIsNotLowerThanTheUpperBound() {
    int lowerBound = 10;
    int upperBound = 10;

    assertThatThrownBy(() -> new IntegerParameter("integerParameter", lowerBound,
        upperBound)).isInstanceOf(InvalidConditionException.class);
  }

  @Test
  void parseRaisesAnExceptionIfTheValueIsNotAnInteger() {
    int lowerBound = 10;
    int upperBound = 20;

    var integerParameter = new IntegerParameter("integerParameter", lowerBound,
        upperBound);
    String[] parameterString = new String[]{"--integerParameter", "hellow"};
    assertThatThrownBy(() -> integerParameter.parse(parameterString)).isInstanceOf(NumberFormatException.class);
  }

  @Test
  void parseGetsTheRightValue() {
    int lowerBound = 10;
    int upperBound = 20;

     var integerParameter = new IntegerParameter("integerParameter",  lowerBound, upperBound);
    assertThat(integerParameter.parse(new String[]{"--integerParameter", "3"}).value()).isEqualTo(3) ;
  }

  @Test
  void checkRaisesAnExceptionIfTheValueIsLowerThanTheLowerBound() {
    int lowerBound = 10;
    int upperBound = 20;

    var integerParameter = new IntegerParameter("integerParameter", lowerBound,
        upperBound);

    String[] parameterString = new String[]{"--integerParameter", "5"};
    integerParameter.parse(parameterString);
    assertThatThrownBy(integerParameter::check).isInstanceOf(JMetalException.class);
  }
}