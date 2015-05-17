/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ulbsibiu.fadse.extended.problems.simulators;

import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.extended.problems.simulators.sniper.SniperOutputParser;
import ro.ulbsibiu.fadse.extended.problems.simulators.sniper.SniperRunner;



/**
 *
 * @author Andrei DAIAN
 * @since 14.05.2013
 * @version 1.0
 */
public class SniperSimulator extends SimulatorBase {

    public SniperSimulator(Environment environment) throws ClassNotFoundException {
        super(environment);
        // TODO: Where should this constant be kept?
        this.simulatorOutputFile = environment.getInputDocument().getSimulatorParameter("simulator_final_results");
        this.simulatorOutputParser = new SniperOutputParser(this);
        this.simulatorRunner = new SniperRunner(this);
    }
}