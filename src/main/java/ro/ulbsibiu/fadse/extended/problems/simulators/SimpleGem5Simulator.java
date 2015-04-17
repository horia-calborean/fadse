/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ro.ulbsibiu.fadse.extended.problems.simulators;

import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.extended.problems.simulators.simplegem5.SimpleGem5OutputParser;
import ro.ulbsibiu.fadse.extended.problems.simulators.simplegem5.SimpleGem5Runner;

/**
 *
 * @author Andrei
 */
public class SimpleGem5Simulator extends SimulatorBase {

    /**
     * class constructor
     * @param inputDocument
     */
    public SimpleGem5Simulator(Environment environment) throws ClassNotFoundException{
        super(environment);
        // TODO: Where should this constant be kept?
        this.simulatorOutputFile = environment.getInputDocument().getSimulatorParameter("simulator_output_file");
        this.simulatorOutputParser = new SimpleGem5OutputParser(this);
        this.simulatorRunner = new SimpleGem5Runner(this);
    }
}
