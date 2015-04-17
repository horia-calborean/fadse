/*
 * StateMachine.java
 *
 * Created on 12. August 2006, 01:54
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ro.ulbsibiu.fadse.shared.statemachine;

import java.awt.event.ActionEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.ulbsibiu.fadse.shared.*;
import shared.ActionEmitter;

/**
 * StateMashine, die auch als ActionEmitter arbeitet
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

        /** Ermittelt, ob die Machine einen bestimmten Status hat */
	public boolean hasState(String shallState) {
		if (state == null)
			return false;
		else
			return state.equals(shallState);
	}
        
        /** Ermittelt, ob die Maschine einen von mehreren Status hat (ODER-Verknüpfung) */
        public boolean hasState(String state_1, String state_2) {
            return hasState(state_1) || hasState(state_2);
        }
}
