package ro.ulbsibiu.fadse.extended.problems.simulators;

import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.extended.problems.simulators.msim2.Msim2OutputParser;
import ro.ulbsibiu.fadse.extended.problems.simulators.msim2.Msim2Runner;

public class Msim2Simulator extends SimulatorBase {

    public Msim2Simulator(Environment environment) throws ClassNotFoundException{
        super(environment);
        // TODO: Where should this constant be kept?
        this.simulatorOutputFile = environment.getInputDocument().getSimulatorParameter("simulator_output_file");
        this.simulatorOutputParser = new Msim2OutputParser(this);
        this.simulatorRunner = new Msim2Runner(this);
    }
	
}
