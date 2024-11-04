/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ro.ulbsibiu.fadse.extended.problems.simulators;
import ro.ulbsibiu.fadse.simulationIO.SimulationIO;
import ro.ulbsibiu.fadse.extended.problems.simulators.tem.TemOutputParser;
import ro.ulbsibiu.fadse.extended.problems.simulators.tem.TemRunner;

/**
 *
 * @author Rolf
 */
public class TemSimulator extends SimulatorBase {

    /**
     * class constructor
     * @param inputDocument
     */
    public TemSimulator(SimulationIO environment) throws ClassNotFoundException{
        super(environment);
        this.simulatorOutputFile = environment.getDesignSpaceDocument().getSimulatorParameter("simulator_output_file");
        this.simulatorOutputParser = new TemOutputParser(this);
        this.simulatorRunner = new TemRunner(this);
    }
}
