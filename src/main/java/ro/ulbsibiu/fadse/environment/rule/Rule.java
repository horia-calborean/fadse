/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ro.ulbsibiu.fadse.environment.rule;

import java.io.Serializable;

import ro.ulbsibiu.fadse.environment.parameters.SimulatorParameter;

/**
 *
 * @author Horia
 */
public interface Rule extends Serializable{
    public boolean validate(SimulatorParameter[] parameters);
    
}
