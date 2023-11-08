package jmetal.core.util.errorchecking.exception;

@SuppressWarnings("serial")
public class NegativeValueException extends RuntimeException {
  public NegativeValueException(double value) {
    super("The parameter " + value + " is negative") ;
  }
}
