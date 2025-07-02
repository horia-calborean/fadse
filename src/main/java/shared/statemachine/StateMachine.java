package shared.statemachine;

import java.awt.event.ActionEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shared.ActionEmitter;

/**
 * StateMachine, which also functions as an ActionEmitter
 * @author ralf
 */
public abstract class StateMachine extends ActionEmitter {
    /** Logger für das Logging */
    private static Logger log = LoggerFactory.getLogger(StateMachine.class);

    private String state = null;

    public void setState(String newState) {
        state = newState;

        if (log.isDebugEnabled())
            log.debug("State has been changed in class " + this.getClass() + " => " + newState);

        ActionEvent ae = new ActionEvent(this, 0, "State has been changed: " + this.getClass().getSimpleName() + " => " + newState);
        notifyListeners(ae);
    }

    public String getState() {
        return state;
    }

    public void assertState(String shallState) throws StateException {
        if (!hasState(shallState)) {
            throw new StateException("Invalid state. Current state is '" + state
                    + "' and wanted is '" + shallState + "'");
        }
    }

    public void assertState(String shallState1, String shallState2)
            throws StateException {
        if (!hasState(shallState1) && !hasState(shallState2)) {
            throw new StateException("Invalid state. Current state is '" + state
                    + "' and wanted is '" + shallState1 + "' or '"
                    + shallState2 + "''");
        }
    }

    public void assertState(String shallState1, String shallState2,
                            String shallState3) throws StateException {
        if (!hasState(shallState1) && !hasState(shallState2)
                && !hasState(shallState3)) {
            throw new StateException("Invalid state. Current state is '" + state
                    + "' and wanted is '" + shallState1 + "' or '"
                    + shallState2 + "' or '" + shallState3 + "'");
        }
    }

    /** Checks if the machine has a certain state */
    public boolean hasState(String shallState) {
        if (state == null)
            return false;
        else
            return state.equals(shallState);
    }
}
