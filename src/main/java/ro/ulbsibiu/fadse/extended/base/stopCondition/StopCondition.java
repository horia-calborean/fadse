/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ro.ulbsibiu.fadse.extended.base.stopCondition;

import java.io.File;
import java.util.List;

import ro.ulbsibiu.fadse.simulationIO.SimulationIO;

/**
 *
 * @author Horia
 */
public abstract class StopCondition {
protected SimulationIO environment;
    public StopCondition(SimulationIO environment) {
        this.environment = environment;
    }
    
    abstract public boolean stopConditionFulfilled(List<File> listOfPopulationFiles);

}
