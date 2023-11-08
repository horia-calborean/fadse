package jmetal.core.util.errorchecking;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import jmetal.core.util.errorchecking.exception.InvalidConditionException;
import jmetal.core.util.errorchecking.exception.InvalidProbabilityValueException;
import jmetal.core.util.errorchecking.exception.NegativeValueException;
import jmetal.core.util.errorchecking.exception.NullParameterException;
import jmetal.core.util.errorchecking.exception.ValueOutOfRangeException;

class CheckerTest {

  @Test
  void shouldIsNotNullRaiseAnExceptionIfTheObjectIsNull() {
    assertThatThrownBy(() -> Check.notNull(null)).isInstanceOf(NullParameterException.class);
  }

  @Test
  void shouldIsValidProbabilityRaiseAnExceptionIfTheValueIsNegative() {
    assertThatThrownBy(() -> Check.probabilityIsValid(-1.0)).isInstanceOf(InvalidProbabilityValueException.class);
  }

  @Test
  void shouldIsValidProbabilityRaiseAnExceptionIfTheValueIsHigherThanOne() {
    assertThatThrownBy(() -> Check.probabilityIsValid(1.1)).isInstanceOf(InvalidProbabilityValueException.class);
  }

  @Test
  void shouldIsValueInRangeRaiseAnExceptionIfTheValueIsLowerThanTheLowerBound() {
    assertThatThrownBy(() -> Check.valueIsInRange(2, 3, 5)).isInstanceOf(ValueOutOfRangeException.class);
  }

  @Test
  void shouldIsValueInRangeRaiseAnExceptionIfTheValueIsHigherThanTheUpperBound() {
    assertThatThrownBy(() -> Check.valueIsInRange(6.2, 3.1, 5.5)).isInstanceOf(ValueOutOfRangeException.class);

  }

  @Test
  void shouldThatRaiseAnExceptionIfTheExpressionIsFalse() {
    assertThatThrownBy(() -> Check.that(false, "")).isInstanceOf(InvalidConditionException.class);
  }

  @Test
  void shouldThatRaiseAnExceptionIfTheValueIsNegative() {
    assertThatThrownBy(() -> Check.valueIsNotNegative(-1)).isInstanceOf(NegativeValueException.class);
  }
}