/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ro.ulbsibiu.fadse.extended.problems.simulators;
import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.extended.problems.simulators.msim3.Msim3OutputParser;
import ro.ulbsibiu.fadse.extended.problems.simulators.msim3.Msim3Runner;

/**
 *
 * @author Andrei
 */
public class MsimSimulator extends SimulatorBase {

    /**
     * class constructor
     * @param inputDocument
     */
    public MsimSimulator(Environment environment) throws ClassNotFoundException{
        super(environment);
        // TODO: Where should this constant be kept?
        this.simulatorOutputFile = environment.getInputDocument().getSimulatorParameter("simulator_output_file");
        this.simulatorOutputParser = new Msim3OutputParser(this);
        this.simulatorRunner = new Msim3Runner(this);
    }

   
  
}
