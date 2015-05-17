/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ulbsibiu.fadse.extended.problems.simulators;

import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.extended.problems.simulators.continental.ContinentalOutputParser;
import ro.ulbsibiu.fadse.extended.problems.simulators.continental.ContinentalRunner;


/**
 *
 * @author Radu
 */
public class ContinentalSimulator extends SimulatorBase {

    public ContinentalSimulator(Environment environment) throws ClassNotFoundException {
        super(environment);
        this.simulatorOutputFile = environment.getInputDocument().getSimulatorParameter("simulator_output_file");
        this.simulatorOutputParser = new ContinentalOutputParser(this);
        this.simulatorRunner = new ContinentalRunner(this);
    }
}
