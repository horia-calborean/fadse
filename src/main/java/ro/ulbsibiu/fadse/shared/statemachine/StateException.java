package ro.ulbsibiu.fadse.shared.statemachine;

/** Exception für fehlerhaften Status im RoutingPrologController */
public class StateException extends Exception {
	private static final long serialVersionUID = -2096435001066446942L;

	public StateException(String message) {
		super(message);
	}
}