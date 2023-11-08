package jmetal.core.util.errorchecking.exception;

@SuppressWarnings("serial")
public class EmptyCollectionException extends RuntimeException {
  public EmptyCollectionException() {
    super("The collection is empty") ;
  }
}
