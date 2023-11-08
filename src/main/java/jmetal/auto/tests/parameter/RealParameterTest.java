package jmetal.auto.tests.parameter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jmetal.auto.parameter.RealParameter;
import org.junit.jupiter.api.Test;
import jmetal.core.util.errorchecking.JMetalException;
import jmetal.core.util.errorchecking.exception.InvalidConditionException;

class RealParameterTest {

  @Test
  void constructorMustInitializeTheFieldsCorrectly() {
    double lowerBound = 1.0;
    double upperBound = 10.0;
    RealParameter realParameter = new RealParameter("realParameter", lowerBound,
        upperBound);

    assertThat(realParameter.name()).isEqualTo("realParameter");
    assertThat(realParameter.validValues()).containsExactly(lowerBound, upperBound);
  }

  @Test
  void constructorMustRaiseAnExceptionInTheLowerBoundIsNotLowerThanTheUpperBound() {
    double lowerBound = 10.0;
    double upperBound = 10.0;

    assertThatThrownBy(() -> new RealParameter("realParameter", lowerBound,
        upperBound)).isInstanceOf(InvalidConditionException.class);
  }

  @Test
  void parseRaisesAnExceptionIfTheValueIsNotADouble() {
    double lowerBound = 10.0;
    double upperBound = 20.0;

    RealParameter realParameter = new RealParameter("realParameter", lowerBound,
        upperBound);
    String[] parameterString = new String[]{"--realParameter", "hellow"};
    assertThatThrownBy(() -> realParameter.parse(parameterString)).isInstanceOf(NumberFormatException.class);
  }

  @Test
  void parseGetsTheRightValue() {
    double lowerBound = 10.0;
    double upperBound = 20.0;

    RealParameter realParameter = new RealParameter("realParameter",  lowerBound, upperBound);
    assertThat(realParameter.parse(new String[]{"--realParameter", "2.4"}).value()).isEqualTo(2.4) ;
  }

  @Test
  void checkRaisesAnExceptionIfTheValueIsLowerThanTheLowerBound() {
    double lowerBound = 10.0;
    double upperBound = 20.0;

    RealParameter realParameter = new RealParameter("realParameter", lowerBound,
        upperBound);

    String[] parameterString = new String[]{"--realParameter", "5"};
    realParameter.parse(parameterString);
    assertThatThrownBy(realParameter::check).isInstanceOf(JMetalException.class);
  }
}