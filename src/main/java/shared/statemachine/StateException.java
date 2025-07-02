package shared.statemachine;

public class StateException extends RuntimeException {
  private static final long serialVersionUID = -2096435001066446942L;

  public StateException(String message) {
    super(message);
  }
}
