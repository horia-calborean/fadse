/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ro.ulbsibiu.fadse.simulationIO.rule;

import java.io.Serializable;

import ro.ulbsibiu.fadse.simulationIO.parameters.simulator.SimulatorParameter;

/**
 *
 * @author Horia
 */
public interface Rule extends Serializable{
    public boolean validate(SimulatorParameter[] parameters);
    
}
