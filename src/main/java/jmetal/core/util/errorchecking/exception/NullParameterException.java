package jmetal.core.util.errorchecking.exception;

@SuppressWarnings("serial")
public class NullParameterException extends RuntimeException {
  public NullParameterException() {
    super("The parameter is null") ;
  }
}
