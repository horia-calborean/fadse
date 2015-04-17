/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ro.ulbsibiu.fadse.extended.problems.simulators;
import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.extended.problems.simulators.*;
import ro.ulbsibiu.fadse.extended.problems.simulators.gap.GAPOutputParser;
import ro.ulbsibiu.fadse.extended.problems.simulators.gap.GAPRunner;

/**
 *
 * @author Andrei
 */
public class GAPSimulator extends SimulatorBase {

    /**
     * class constructor
     * @param inputDocument
     */
    public GAPSimulator(Environment environment) throws ClassNotFoundException{
        super(environment);
        this.simulatorOutputFile = environment.getInputDocument().getSimulatorParameter("simulator_output_file");
        this.simulatorOutputParser = new GAPOutputParser(this);
        this.simulatorRunner = new GAPRunner(this);
    }
}
