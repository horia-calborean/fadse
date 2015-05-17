/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ulbsibiu.fadse.extended.problems.simulators.sniper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

import ro.ulbsibiu.fadse.extended.problems.simulators.SimulatorBase;
import ro.ulbsibiu.fadse.extended.problems.simulators.SimulatorRunner;

/**
 *
 * @author Andrei DAIAN
 * @since 03.05.2013
 * @version 1.0
 */
public class SniperRunner extends SimulatorRunner {

    protected LinkedHashMap<String, String> receivedParameters;
    public String currentSimulationDirectory = "";

    public SniperRunner(SimulatorBase simulator) {
        super(simulator);
        this.receivedParameters = this.simpleParameters;
    }

    @Override
    protected void prepareParameters() {
        super.prepareParameters(); //To change body of generated methods, choose Tools | Templates.
    }

    private void appendSimpleParams(StringBuilder paramString) {
        Map<String, String> simpleParams = SniperConstants.getSimpleParameters();

        // See which simple parameters have been received, by looking for every supported simple parameter
        // in receivedParameters
        for (Map.Entry<String, String> param : simpleParams.entrySet()) {
            // The parameter's name
            String pkey = param.getKey();       

            // The parameter's command-line option
            String pvalue = param.getValue();

            // Has the parameter been received?
            if (this.receivedParameters.containsKey(pkey)) {
                paramString.append(" ").append(pvalue);
                // Have we also received a value for the parameter? 
                if (!this.receivedParameters.get(pkey).isEmpty()) {
                    paramString.append("").append(this.receivedParameters.get(pkey));
                }
            }
        }
    }
    
    int TIME_BETWEEN_PROCESS_CHECKS = 5000;
    
     @Override
    public void run() {
        String line;
        String executeCommand = "";
        String CommandLineArgs[] = this.getCommandLine();
        for (String s : CommandLineArgs) {
            executeCommand += " " + s;
        };
        System.out.println("- Starting simulator: [" + simulator.getInputDocument().getSimulatorName()
                + "] with the following command: \n" + executeCommand);

        try {
            Thread.sleep(100);
            // Execute simulator
            p = Runtime.getRuntime().exec(CommandLineArgs);
            
            p.waitFor();
//            boolean is_terminated = false;
//            boolean finished = false;
//            do{
//               try {
//                    System.out.println(
//                            "Simulation: Let's wait for " + TIME_BETWEEN_PROCESS_CHECKS + " ms...");
//                    synchronized (p) {
//                        p.wait(TIME_BETWEEN_PROCESS_CHECKS);
//                    }
//                } catch (Exception iex) {
//                    System.out.println("Trouble while waiting: " + iex.getMessage());
//                }
//
//                // Check if we want to stop waiting for the simulation...
//                is_terminated = isExecutionTerminated(p);  
//                if (is_terminated) {                    
//                    finished = true;
//                } 
//                else{
//                    finished = false;
//                }
//                
//            }while(!finished);

            // Wait for the simulation to end
           // p.waitFor();
            System.out.println("- Simulation ended");
            
            String simOutputDir = this.simulator.getInputDocument().getSimulatorParameter("simulator_opt");
            String powerFilePath = simOutputDir + "power.py";
            File powerFile = new File(powerFilePath);
            if(!powerFile.exists())            {
                Thread.sleep(10000);
                p = Runtime.getRuntime().exec(CommandLineArgs);
            
                p.waitFor();
                System.out.println("- Simulation ended second time!!!!");
            }
            
            // Check if SniperSimulator is used
            String checkSniper = this.simulator.getInputDocument().getSimulatorParameter("simulator_executable");
            boolean exists = false;

            if (checkSniper.contains("run-sniper")) {
                exists = true;

                SniperGroupResults sgr = new SniperGroupResults(simulator);
                sgr.groupPartialResults();
            }

            p = null;
        } catch (IOException e) {
               System.err.print(e.getMessage() + "\n" + e.getStackTrace());
        } catch (Exception e) {
            
            System.err.print(e.getMessage() + "\n" + e.getStackTrace());
            
        }

    }
    
       /** Checks if the simulation is still running */
    private boolean isExecutionTerminated(Process p) {
        if (p == null) {
            System.out.println("Process is null - terminate!");
            return true;
        }

        // If running => we will get an exception!
        try {
            int exit_value = p.exitValue();
            System.out.println("Exit value is: " + exit_value);
            return true;
        } catch (IllegalThreadStateException ex) {
            // ex.printStackTrace();
            return false;
        }
    }

    /**
     *
     * @return
     */
    @Override
    protected String[] getCommandLine() {
        StringBuilder Output = new StringBuilder();

        /*
         * R U N N I N G  S P L A S H - 2  B E N C H S  
         */        
        String outputSimDir = this.simulator.getInputDocument().getSimulatorParameter("simulator_opt");//"simulator_output_dir");

        Output.append("mkdir -p " + outputSimDir + "; ");

        //Software optimization
        if (this.receivedParameters.containsKey("software_optimization")) {
            int softwareOptimization = Integer.parseInt(this.receivedParameters.get("software_optimization"));
            String simulatorFolder = simulator.getInputDocument().getSimulatorParameter("simulator_folder").toString();
            String benchkarksFolder = simulatorFolder + "benchmarksO" + softwareOptimization;
            String command = "export BENCHMARKS_ROOT=" + benchkarksFolder + "/;"
                    + "cd $BENCHMARKS_ROOT;./run-sniper -c gainestown ";
            Output.append(command);
        } else {
            // Start with appending the executable
            Output.append(this.simulator.getInputDocument().getSimulatorParameter("simulator_executable"));
        }

         // Command for benchs
        //--benchmarks=splash2-barnes-test-2
        String cBenchs = " --benchmarks=%s-%s-%s";
        // Command for type of instructions
        String instsType = this.simulator.getInputDocument().getSimulatorParameter("instructions_type");        
        String benchmark = individual.getBenchmark();          
        String nrThreads = receivedParameters.get("num_cores");
        if((receivedParameters.get("nr_threads") != null) && !receivedParameters.get("nr_threads").isEmpty()){
            nrThreads = receivedParameters.get("nr_threads");
        }                
        String formattedString = String.format(cBenchs, benchmark, instsType, nrThreads);
        Output.append(formattedString);
              
        // Append simple parameters
        appendSimpleParams(Output);

       
        
  

      

        if (Integer.parseInt(this.simulator.getInputDocument().getSimulatorParameter("mcpat")) == 1) {
            Output.append(" --power");
        }

        Output.append(" -d ").append(outputSimDir);

        Output.append(" 1>" + outputSimDir + this.simulator.getInputDocument().getSimulatorParameter("console_output") + " 2>&1");

        System.out.println("Sniper Command Line: " + Output.toString());

        File output = new File(outputSimDir);
        if(!output.exists()){
            output.mkdir();
        }
        
        String runScript = outputSimDir + "runSniper" + System.currentTimeMillis() + ".sh";
        
        try {
            
        BufferedWriter out = new BufferedWriter(new FileWriter(runScript));            
            out.write(Output.toString());            
            out.close();
            
            Runtime.getRuntime().exec("chmod u+x " + runScript);
        } catch (IOException e) {
            
        }
        
        return new String[]{runScript};
    }
}
