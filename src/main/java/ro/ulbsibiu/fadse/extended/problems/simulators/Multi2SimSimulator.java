/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ro.ulbsibiu.fadse.extended.problems.simulators;
import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.extended.problems.simulators.Multi2Sim.Multi2SimOutputParser;
import ro.ulbsibiu.fadse.extended.problems.simulators.Multi2Sim.Multi2SimRunner;

/**
 *
 * @author Andrei
 */
public class Multi2SimSimulator extends SimulatorBase {
    
    /**
     * class constructor
     * @param inputDocument 
     */
    public Multi2SimSimulator(Environment environment) throws ClassNotFoundException{
        super(environment);
        this.simulatorOutputFile = environment.getInputDocument().getSimulatorParameter("simulator_output_file");
        this.simulatorOutputParser = new Multi2SimOutputParser(this);
        this.simulatorRunner = new Multi2SimRunner(this);
    }



}
