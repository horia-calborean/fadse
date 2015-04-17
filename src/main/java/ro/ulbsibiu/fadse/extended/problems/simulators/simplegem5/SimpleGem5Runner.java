/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ulbsibiu.fadse.extended.problems.simulators.simplegem5;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import ro.ulbsibiu.fadse.extended.problems.simulators.SimulatorBase;
import ro.ulbsibiu.fadse.extended.problems.simulators.SimulatorRunner;
import ro.ulbsibiu.fadse.extended.problems.simulators.gap.GAPDirectoryDustman;

/**
 *
 * @author Andrei
 */
public class SimpleGem5Runner extends SimulatorRunner {

    public SimpleGem5Runner(SimulatorBase simulator) {
        super(simulator);
    }

    /**
     * creates the simpleParameter list and adds the fixed msim parameters
     * needed for proper execution
     *
     * ./build/ALPHA_SE/m5.opt configs/example/se3.py -d -t -n$Sn --caches
     * --l2cache --cachehierachy=$Sch --l1data_cache_size=$Sd1cs
     * --l1d_assoc=$Sd1a --l1d_mshrs=$Sd1m --l1d_tgts_per_mshr=$Sd1tm
     * --l1instr_cache_size=$Si1cs --l1i_assoc=$Si1a --l1i_mshrs=$Si1m
     * --l1i_tgts_per_mshr=$Si1tm --l2cache_size=$Sl2s --l2_assoc=$Sl2a
     * --l2_mshrs=$Sl2m --l2_tgts_per_mshr=$Sl2tm
     * --cmd=/dist/splash2/codes/kernels/fft/FFT --options="-p$Sn -m18"
     *
     */
    @Override
    public void prepareParameters() {
        super.prepareParameters();

        /*
         * this.addSimpleParameter("d", ""); this.addSimpleParameter("t", "");
         * this.addSimpleParameter("caches", "");
         * this.addSimpleParameter("l2cache", "");
         * this.addSimpleParameter("options", "-p1 -m18");
         */
    }

    @Override
    protected String[] getCommandLine() {
        //sbParamList.add();

        // LinkedList<String> sbParamList = new LinkedList<String>();
        // sbParamList.add(this.simulator.getInputDocument().getSimulatorParameter("simulator_executable"));



        // Search the basic params and add the existing ones
        /*
         * Map<String, String> basicParams =
         * SimpleGem5Constants.getSimpleParameters(); LinkedList<String>
         * customParameters = SimpleGem5Constants.getCustomParameters(); for
         * (Map.Entry<String, String> param: basicParams.entrySet()){ if
         * (this.simpleParameters.containsKey(param.getKey())){
         *
         * String p = param.getValue();
         *
         * if (param.getKey().equals(SimpleGem5Constants.P_SCRIPT)) p =
         * this.simpleParameters.get(param.getKey()); else if
         * (!this.simpleParameters.get(param.getKey()).isEmpty()){ p += ("=" +
         * this.simpleParameters.get(param.getKey())); if
         * (customParameters.contains(param.getKey())) p += ("kB"); }
         * sbParamList.add(p); } }
         *
         * // search for benchmark if
         * (this.simpleParameters.containsKey(Msim3Constants.P_BENCHMARK)){
         * sbParamList.add("--cmd=" +
         * this.simpleParameters.get(Msim3Constants.P_BENCHMARK)); }
         *
         *
         *
         * String[] result = new String[sbParamList.size()];
         * sbParamList.toArray(result);
         *
         * return result;
         */

        ArrayList<String> commands = new ArrayList<String>();

        String benchmark = individual.getBenchmark();
        String target_directory = this.simpleParameters.get(SimpleGem5Constants.P_TARGET_DIRECTORY);
        String repository_directory = simulator.getInputDocument().getSimulatorParameter("benchmark_repository_path");

        commands.add("cp " + repository_directory + benchmark + "/* " + target_directory + " --copy-contents -v -R");
        commands.add("cd " + target_directory);

        System.out.println("Commands so far: " + commands);

        String compiler_flags = "";
        System.out.println("Parameters: " + this.simpleParameters.toString());
        for (String parameter : this.simpleParameters.keySet()) {
            if (parameter.startsWith("f") && this.simpleParameters.get(parameter).equals("1")) {
                compiler_flags += "-" + parameter + " ";
            }
        }
        System.out.println("Compiler flags: " + compiler_flags);

        commands.add("make GCC_PARAMS=\"" + compiler_flags + "\" > make.log 2>&1");

        String[] result_array = new String[commands.size()];
        for (int i = 0; i < commands.size(); i++) {
            result_array[i] = commands.get(i);
        }

        return result_array;
    }

    @Override
    public void run() {
        String basename = individual.getBenchmark();
        basename = basename.replace('/', '_');

        // Create target directory
        File benchmarkDirectory = new File(simulator.getInputDocument().getSimulatorParameter("benchmark_target_directory"));
        benchmarkDirectory = new File(benchmarkDirectory.getAbsolutePath() + "/gem5_dump_" + System.currentTimeMillis() + "_" + basename);
        benchmarkDirectory.mkdirs();

        GAPDirectoryDustman.getInstance().register(benchmarkDirectory);

        // Update Target directory
        this.simpleParameters.put(SimpleGem5Constants.P_TARGET_DIRECTORY, benchmarkDirectory.getAbsolutePath());
        System.out.println("- Target directory: " + benchmarkDirectory);

        // Update result file
        String outputFileString = benchmarkDirectory.getAbsolutePath() + "/objectives.txt";
        this.simulator.setSimulatorOutputFile(outputFileString);

        // Create Bash-Script
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(benchmarkDirectory.getAbsolutePath() + "/runme.sh"));
            bw.append("#!/bin/bash\n");
            for (String a : this.getCommandLine()) {
                bw.append(a + "\n");
            }
            bw.close();

            /*
             * String line; String executeCommand = ""; String CommandLineArgs[]
             * = this.getCommandLine(); for (String s : CommandLineArgs){
             * executeCommand += " "+ s; }; System.out.println("- Starting
             * simulator: [" + simulator.getInputDocument().getSimulatorName() +
             * "] with the following command: \n" + executeCommand);
             */

            // Execute simulator, set working directory to benchmark directory
            String my_command = "/bin/chmod a+x runme.sh > chmod.log";
            Process p = Runtime.getRuntime().exec(my_command, null, benchmarkDirectory);
            p.waitFor();

            my_command = "/bin/bash runme.sh > /tmp/runme.log";
            p = Runtime.getRuntime().exec(my_command, null, benchmarkDirectory);

            // Wait for the simulation to end
            boolean is_terminated = false;
            String reason = "";

            // Execute the benchmark
            boolean do_terminate = false;
            do {
                try {
                    System.out.println("Simulation: Let's wait for " + 5000 + " ms...");
                    synchronized (p) {
                        p.wait(5000);
                    }
                } catch (Exception iex) {
                    System.out.println("Trouble while waiting: " + iex.getMessage());
                }

                // Check if we want to stop waiting for the simulation...
                is_terminated = isExecutionTerminated(p);

                if (is_terminated) {
                    reason = "The process has terminated.";
                    do_terminate = true;
                } else {
                    do_terminate = false;
                }
            } while (!do_terminate);

            System.out.println("The simulation has been terminated because: " + reason);

        } catch (Exception e) {
            System.out.println("ERROR " + e.getMessage());
            e.printStackTrace();
        }

        /*
         * try { // Execute simulator p =
         * Runtime.getRuntime().exec(CommandLineArgs);
         *
         * // Retrieve output of simulator (for debugging only) BufferedReader
         * output = new BufferedReader(new
         * InputStreamReader(p.getInputStream())); while ((line =
         * output.readLine()) != null) { // System.out.println(line); }
         * output.close();
         *
         * // Wait for the simulation to end p.waitFor(); System.out.println("-
         * Simulation ended"); p = null; } catch (IOException e) {
         * e.printStackTrace(); } catch (Exception e) { e.printStackTrace(); }
         */

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
}
