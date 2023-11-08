/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ro.ulbsibiu.fadse.extended.problems.simulators.network.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.extended.problems.SimulatorWrapper;
import jmetal.problems.ProblemFactory;
import jMetal.util.JMException;

/**
 *
 * @author Horia Calborean
 */
public class SimulatorFactory {
    private static SimulatorWrapper problem = null;
    public static SimulatorWrapper getSimulator(String name, Environment env){
        try {
            Object[] problemParams = {env};
            problem = (SimulatorWrapper) (new ProblemFactory()).getProblem(name, problemParams);
        } catch (JMException ex) {
            Logger.getLogger(SimulatorFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return problem;
    }
}
