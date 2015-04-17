package ro.ulbsibiu.fadse.extended.problems.simulators.msim3;

import java.util.LinkedHashMap;

import ro.ulbsibiu.fadse.extended.problems.simulators.SimulatorBase;
import ro.ulbsibiu.fadse.extended.problems.simulators.SimulatorRunner;

public class Msim3Runner extends SimulatorRunner {
		protected Msim3Parameters ParameterManager;
	
	
    public Msim3Runner(SimulatorBase simulator){
        super(simulator);
    }
    
    @Override
    public void prepareParameters(){
        super.prepareParameters();
        LinkedHashMap<String, String> SimulatorValues = new LinkedHashMap<String, String>(this.simulator.getInputDocument().getSimulatorParameters());
        LinkedHashMap<String, String> DSEValues = this.simpleParameters;
        
        ParameterManager = new Msim3Parameters();
    	ParameterManager.initSimulatorParameters();
    	ParameterManager.initStructuralParameters();
    	ParameterManager.retrieveSimulatorParameters(SimulatorValues);
    	ParameterManager.retrieveStructuralParameters(DSEValues);
    	
    	// The core parameters must be initialized after the retrieval of the structural parameters (due to num_cores)
    	ParameterManager.initCoreParameters();
    	ParameterManager.retrieveCoreParameters(DSEValues);
    }
    
    @Override
    protected String[] getCommandLine() {
    	StringBuilder Output = new StringBuilder();
    	String SimulatorParameters = ParameterManager.getSimulatorParameters().toString();
    	String StructuralParameters = ParameterManager.getStructuralParameters().toString();
    	String MulticoreParameters = ParameterManager.getMulticoreParameters().toString();
    	String Benchmarks = individual.getBenchmark();
        
    	Output.append(SimulatorParameters);
    	Output.append(" ");
    	Output.append(StructuralParameters);
        Output.append(" ");
        Output.append(MulticoreParameters);
        Output.append(" ");
        Output.append(Benchmarks);
        
        return Output.toString().trim().split(" ");
    }
}
