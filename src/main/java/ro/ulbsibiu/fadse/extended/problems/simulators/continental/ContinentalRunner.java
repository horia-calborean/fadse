
package ro.ulbsibiu.fadse.extended.problems.simulators.continental;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import ro.ulbsibiu.fadse.environment.Objective;

import ro.ulbsibiu.fadse.environment.parameters.IntegerParameter;
import ro.ulbsibiu.fadse.environment.parameters.Parameter;
import ro.ulbsibiu.fadse.extended.problems.simulators.SimulatorBase;
import ro.ulbsibiu.fadse.extended.problems.simulators.SimulatorRunner;

/**
 *
 * @author Radu
 */
public class ContinentalRunner extends SimulatorRunner {

    private File benchmarkDirectory;
    private static final long TIME_BETWEEN_PROCESS_CHECKS = 10000;
    private long MAX_SIMULATION_TIME_SECONDS = 500; //MAX simulation time - if not finished, restart

    public ContinentalRunner(SimulatorBase simulator) {
        super(simulator);
    }

    @Override
    public void prepareParameters() {
        super.prepareParameters();
    }

    @Override
    protected String[] getCommandLine() {
        String outputFileString = this.simpleParameters.get(ContinentalConstants.CURRENT_SIMULATION_DIRECTORY);
        this.simulator.setSimulatorOutputFile(outputFileString);
        String[] result = new String[1];
        return result;
    }

    /**
     * Execute :)
     */
    @Override
    public void run() {
        // Target Directory for Benchmark
        this.benchmarkDirectory = null;

        int maxMinutes = Integer.parseInt(simulator.getInputDocument().getSimulatorParameter("maximumTimeOfASimulation"));
        
        MAX_SIMULATION_TIME_SECONDS = maxMinutes * 60;
        
        String basename = individual.getBenchmark();

        String[] benchmarks = basename.split(";");

        benchmarkDirectory = new File(simulator.getSimulatorOutputFile() + "_" + basename);
        benchmarkDirectory.mkdirs();

        this.simpleParameters.put(ContinentalConstants.CURRENT_SIMULATION_DIRECTORY, benchmarkDirectory.getAbsolutePath());
        this.simulator.setSimulatorOutputFile(benchmarkDirectory.getAbsolutePath());
        String comsolDirectory = "";
        String targetDirectory = "";
        try {
            // Prepare command to execute
            File sourceDirectory = new File(simulator.getInputDocument().getSimulatorParameter("benchmark_model_directory"));
            targetDirectory = benchmarkDirectory.getAbsolutePath();
            File[] files = sourceDirectory.listFiles();
            for (File file : files) {
                Path source = Paths.get(file.getAbsolutePath());
                Path target = Paths.get(targetDirectory + "\\" + file.getName());
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            }
            //create input files
            for (Parameter param : individual.getParameters()) {
                String name = param.getName();
                double paramvalue = (Integer) param.getValue();
                if (param instanceof IntegerParameter) {
                    paramvalue = paramvalue / ((IntegerParameter) param).getDivideBy();
                }
                TextWrite(name, paramvalue);
            }

            // E:\Intalled Matlab and Comsol\COMSOL42\bin\win64 matlab "myscript4, exit" -nodesktop
            String comsolConfigsPath = simulator.getInputDocument().getSimulatorParameter("comsol_directory");

            BufferedReader br = new BufferedReader(new FileReader(comsolConfigsPath));
            comsolDirectory = br.readLine();
            br.close();

        } catch (Exception ex) {
            Logger.getLogger(ContinentalRunner.class.getName()).log(Level.SEVERE, null, ex);
        }

        long start = System.currentTimeMillis();

        //create master m file http://www.mathworks.com/matlabcentral/answers/51926
        File master = new File(targetDirectory + "\\masterfile.m");
        try {
            master.createNewFile();
            BufferedWriter out = new BufferedWriter(new FileWriter(master));

            for (String benchmark : benchmarks) {
                out.write(benchmark.replace(".m", "") + ";\n");
            }
            out.flush();
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(ContinentalRunner.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {

            String executeCommand = "\"" + comsolDirectory + "\" "
                    + " matlab \"cd \'" + targetDirectory + "\';" + "masterfile" + ";exit\" -nodesktop -nosplash -minimize";

            System.out.println(
                    "- Starting simulator: ["
                    + simulator.getInputDocument().getSimulatorName()
                    + "] with the following command: \n" + executeCommand);
            //p = Runtime.getRuntime().exec(executeCommand, null, benchmarkDirectory);

                //Random r = new Random();
            //Thread.sleep(r.nextInt(5000) + 1000);
            //TextWrite("modelName.txt", "Model" + System.currentTimeMillis());
            p = Runtime.getRuntime().exec(executeCommand, null, benchmarkDirectory);
            Boolean isTerminated = false;
            Boolean doTerminate = false;
            Boolean wasRestarted = false;
            String reason;
            // Execute the benchmark
            do {
                try {
                    System.out.println(
                            "Simulation took " + (System.currentTimeMillis() - start) + " ms...");
                    synchronized (p) {
                        p.wait(TIME_BETWEEN_PROCESS_CHECKS);
                    }
                } catch (Exception iex) {
                    System.out.println("Trouble while waiting: " + iex.getMessage());
                }

                // Check if we want to stop waiting for the simulation...
                isTerminated = isExecutionTerminated(p);

                if (isTerminated) {
                    reason = "The process has terminated.";
                    
                    if(EveryThingOk(targetDirectory)){
                        doTerminate = true;
                    }
                    else{
                        if(!wasRestarted){
                            wasRestarted = true;
                            p = Runtime.getRuntime().exec(executeCommand, null, benchmarkDirectory);
                            start = System.currentTimeMillis();                            
                            System.out.println("Simulation restarted because outputs were missing");
                        }
                        else{
                            doTerminate = true;
                        }
                    }
                                                                                                                        
                }
                else{
                    long currentEllapsed = System.currentTimeMillis();
                    long ellapsedTime = (currentEllapsed - start) / 1000; 
                    if(ellapsedTime > MAX_SIMULATION_TIME_SECONDS){
                         if (p != null && !isExecutionTerminated(p)) {
                             p.destroy();                                
                         }
                         p = Runtime.getRuntime().exec(executeCommand, null, benchmarkDirectory);
                         start = System.currentTimeMillis();
                         System.out.println("Simulation restarted because max_simulation_time");
                    }
                }
            } while (!doTerminate);

        } catch (IOException ex) {
            Logger.getLogger(ContinentalRunner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            e.printStackTrace();
            this.getIndividual().markAsInfeasibleAndSetBadValuesForObjectives(
                    "Exception while running, " + e.getMessage());
        } finally {
            System.out.println("Now let's terminate the process if it is still existing.");
            // Kill process if necessary (it is still there)
            if (p != null && !isExecutionTerminated(p)) {
                System.out.println("There is a process, it is not terminated - kill it.");
                try {
                    p.destroy();
                    System.out.println("  Mission accomplished.");
                } catch (Exception e) {
                    System.out.println("  Exception during destroying process: " + e.getMessage());
                }
            }

            // Delete Benchmark directory if wanted... todo.
        }

        p = null;
        long end = System.currentTimeMillis();
        long ellapsedSeconds = (end - start) / 1000;
        System.out.println("- Simulation completed (with or without errors) in " + ellapsedSeconds + " seconds.");

    }

    /**
     * Checks if the simulation is still running
     */
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

    private boolean BinaryWrite(String path, Object value) {
        FileOutputStream fos = null;
        try {
            String str = String.valueOf(value);
            byte[] data = str.getBytes();
            fos = new FileOutputStream(new File(benchmarkDirectory + "\\" + path));
            fos.write(data, 0, data.length);
            fos.flush();
            fos.close();
            return true;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ContinentalRunner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ContinentalRunner.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(ContinentalRunner.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

    private boolean TextWrite(String path, Object value) {
        BufferedWriter out = null;
        try {
            String str = String.valueOf(value);

            out = new BufferedWriter(new FileWriter(new File(benchmarkDirectory + "\\" + path)));
            out.write(str);
            out.flush();
            out.close();
            return true;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ContinentalRunner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ContinentalRunner.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(ContinentalRunner.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

    private boolean EveryThingOk(String targetDirectory) {
           for (Objective obj : individual.getObjectives()) {
                String name = obj.getName();
                File outputFile = new File(targetDirectory, "\\" + name);
                if(!outputFile.exists()){
                    return false;
                }
           }
           return true;
    }
}
