package ro.ulbsibiu.fadse.extended.problems.simulators.msim2;

import java.util.LinkedHashMap;
import java.util.Map;

import ro.ulbsibiu.fadse.extended.problems.simulators.CompositeParameter;
import ro.ulbsibiu.fadse.extended.problems.simulators.SimulatorBase;
import ro.ulbsibiu.fadse.extended.problems.simulators.SimulatorRunner;

public class Msim2Runner extends SimulatorRunner {
protected LinkedHashMap<String, String> receivedParameters;
	
	private CompositeParameter CacheDL1, CacheDL2, CacheIL1, CacheIL2;
	private CompositeParameter ITLB, DTLB, BPred_2lev, BPred_BTB, CPred_2lev, CPred_BTB;
	
    public Msim2Runner(SimulatorBase simulator){
        super(simulator);
        this.receivedParameters = this.simpleParameters; 
    }
    
    public void prepareCompositeParameters() {
    	CacheDL1 = Msim2Constants.getCacheDL1(); 
    	CacheDL2 = Msim2Constants.getCacheDL2();
    	CacheIL1 = Msim2Constants.getCacheIL1();  
    	CacheIL2 = Msim2Constants.getCacheIL2(); 
    	
    	ITLB = Msim2Constants.getITLB();
    	DTLB = Msim2Constants.getDTLB();
    	BPred_2lev = Msim2Constants.getBPred_2lev();
    	BPred_BTB = Msim2Constants.getBPred_BTB();
    	CPred_2lev = Msim2Constants.getCPred_2lev();
    	CPred_BTB = Msim2Constants.getBPred_BTB();
    	
    }

    @Override
    public void prepareParameters(){
        super.prepareParameters();
        this.addSimpleParameter("power_print_stats", "true");
        prepareCompositeParameters();
    }
    
    private void appendSimpleParams(StringBuilder paramstring) {
    	// The M-SIM 3 connector supports a number of simple (non-composite) parameters
    	Map<String, String> simpleParams = Msim2Constants.getSimpleParameters();
    	
    	// See which simple parameters have been received, by looking for every supported simple parameter
    	// in receivedParameters
    	for (Map.Entry<String, String> param:  simpleParams.entrySet()) {
    		// The parameter's name
    		String pkey = param.getKey();
    		
    		// The parameter's command-line option
    		String pvalue = param.getValue();
    		
    		// Has the parameter been received?
            if (this.receivedParameters.containsKey(pkey)) {
                paramstring.append(" " + pvalue);
                // Have we also received a value for the parameter? 
                if (!this.receivedParameters.get(pkey).isEmpty())
                    paramstring.append(" " + this.receivedParameters.get(pkey));
            }
    	}
    }
    
    @Override
    protected String[] getCommandLine() {
    	StringBuilder Output = new StringBuilder();
    	
    	// start with appending the executable
        Output.append(this.simulator.getInputDocument().getSimulatorParameter("simulator_executable"));
        
        Output.append(" -redir:sim " + this.simulator.getInputDocument().getSimulatorParameter("simulator_output_file"));
        
        // append simple and composite parameters
        appendSimpleParams(Output);
        
        CacheDL1.assembleParameterValues(receivedParameters, Output);
    	CacheDL2.assembleParameterValues(receivedParameters, Output);
    	CacheIL1.assembleParameterValues(receivedParameters, Output);  
    	CacheIL2.assembleParameterValues(receivedParameters, Output); 

    	ITLB.assembleParameterValues(receivedParameters, Output);
    	DTLB.assembleParameterValues(receivedParameters, Output);
    	BPred_2lev.assembleParameterValues(receivedParameters, Output);
    	BPred_BTB.assembleParameterValues(receivedParameters, Output);
    	CPred_2lev.assembleParameterValues(receivedParameters, Output);
    	CPred_BTB.assembleParameterValues(receivedParameters, Output);
    	
        
        // append benchmark
        Output.append(" " + individual.getBenchmark());
        System.out.println(new String("MSIM-2 Command Line: ") + Output.toString());
        return Output.toString().split(" ");
    }
}
