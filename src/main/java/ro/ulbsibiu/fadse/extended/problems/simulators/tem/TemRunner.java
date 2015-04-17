/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ulbsibiu.fadse.extended.problems.simulators.tem;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import ro.ulbsibiu.fadse.extended.problems.simulators.SimulatorBase;
import ro.ulbsibiu.fadse.extended.problems.simulators.SimulatorRunner;

/**
 * 
 * @author Andrei
 */
public class TemRunner extends SimulatorRunner {

    public TemRunner(SimulatorBase simulator) {
        super(simulator);
    }

    /**
     * creates the simpleParameter list and adds the fixed msim parameters
     * needed for proper execution
     * 
     */
    @Override
    public void prepareParameters() {
        super.prepareParameters();
    }

    @Override
    protected String[] getCommandLine() {
        String os = System.getProperty("os.name");
        String cpDelim = ";";
        boolean isLinux = os.toLowerCase().trim().equals("linux");
        if (isLinux) {
            cpDelim = ":";
        }

        String cmd = "java -cp ." + cpDelim
                + "../libs/* de.uau.octrust.aggregation.eval.Evaluator > tem.log 2>&1";

        String target_directory = this.simpleParameters
                .get(TemConstants.P_TARGET_DIRECTORY);

        Properties p = new Properties();
        // p.put("evals", "50");
        // p.put("time_steps_part_1", "2000");
        // p.put("time_steps_part_2", "4000");
        // p.put("time_steps_part_3", "2000");
        p.put("steps", "8000");
        p.put("scenario_dir", "../../szenarios");
        p.put("eval_agents", "10");

        // p.put("t_rep_max", "0.1");
        // p.put("t_rep_neg", "0.2");
        // p.put("t_rep_pos", "0.1");
        // p.put("t_rep_start", "1");

        List<String> floatInReal10000 = new ArrayList<String>();
        floatInReal10000.add("w_a");
        floatInReal10000.add("w_n");
        floatInReal10000.add("w_v");

        List<String> floatInReal100 = new ArrayList<String>();
        floatInReal100.add("t_rep_max");
        floatInReal100.add("t_rep_pos");
        floatInReal100.add("t_rep_neg");
        floatInReal100.add("t_agg_high");
        floatInReal100.add("t_agg_low");

        List<String> floatInReal10 = new ArrayList<String>();
        floatInReal10.add("t_rep_start");

        p.put("output_dir", target_directory);

        System.out.println("Parameters: " + this.simpleParameters.toString());

        for (String parameter : this.simpleParameters.keySet()) {
            if (!parameter.equals(TemConstants.P_TARGET_DIRECTORY)) {
                if (floatInReal10000.contains(parameter)) {
                    p.put(parameter,
                          (Double.parseDouble(this.simpleParameters
                                  .get(parameter)) / (double) 10000) + "");
                } else if (floatInReal100.contains(parameter)) {
                    p.put(parameter,
                          (Double.parseDouble(this.simpleParameters
                                  .get(parameter)) / (double) 100) + "");
                } else if (floatInReal10.contains(parameter)) {
                    p.put(parameter,
                          (Double.parseDouble(this.simpleParameters
                                  .get(parameter)) / (double) 10) + "");
                } else {
                    p.put(parameter, this.simpleParameters.get(parameter));
                }

            }
        }

        String propertyFilePath = target_directory + "/eval.properties";
        System.out.println("Writing properties file: " + propertyFilePath);
        FileOutputStream fileout = null;
        try {
            fileout = new FileOutputStream(new File(propertyFilePath));
            p.store(fileout, "");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileout != null) {
                try {
                    fileout.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /*
         * String benchmark = individual.getBenchmark(); String target_directory
         * = this.simpleParameters.get(TemConstants.P_TARGET_DIRECTORY); String
         * repository_directory =
         * simulator.getInputDocument().getSimulatorParameter
         * ("benchmark_repository_path");
         * 
         * commands.add("cp " + repository_directory + benchmark + "/* " +
         * target_directory + " --copy-contents -v -R"); commands.add("cd " +
         * target_directory);
         * 
         * System.out.println("Commands so far: " + commands);
         * 
         * String compiler_flags = ""; System.out.println("Parameters: " +
         * this.simpleParameters.toString()); for (String parameter :
         * this.simpleParameters.keySet()) { if (parameter.startsWith("f") &&
         * this.simpleParameters.get(parameter).equals("1")) { compiler_flags +=
         * "-" + parameter + " "; } } System.out.println("Compiler flags: " +
         * compiler_flags);
         * 
         * commands.add("make GCC_PARAMS=\"" + compiler_flags +
         * "\" > make.log 2>&1");
         * 
         * String[] result_array = new String[commands.size()]; for (int i = 0;
         * i < commands.size(); i++) { result_array[i] = commands.get(i); }
         */

        if (isLinux) {
            String[] foo = { "#!/bin/bash", cmd };
            return foo;
        } else {
            String[] foo = { cmd };
            return foo;
        }

        // return result_array;
    }

    @Override
    public void run() {
        // String basename = individual.getBenchmark();
        // basename = basename.replace('/', '_');

        // Create target directory
        File benchmarkDirectory = new File(simulator.getInputDocument()
                .getSimulatorParameter("benchmark_target_directory"));

        benchmarkDirectory = new File(benchmarkDirectory.getAbsolutePath()
                + "/tem_" + System.currentTimeMillis()
                + Math.round(Math.random() * 10000)); // + "_" + basename);

        benchmarkDirectory.mkdirs();

        TemDirectoryDustman.getInstance().register(benchmarkDirectory);

        // Update Target directory
        this.simpleParameters.put(TemConstants.P_TARGET_DIRECTORY,
                                  benchmarkDirectory.getAbsolutePath());
        System.out.println("- Target directory: " + benchmarkDirectory);

        // Update result file
        String outputFileString = benchmarkDirectory.getAbsolutePath()
                + "/fadse_mean.txt";
        this.simulator.setSimulatorOutputFile(outputFileString);

        String os = System.getProperty("os.name");
        boolean isLinux = os.toLowerCase().trim().equals("linux");

        // Create Bash-Script
        try {
            String batFilePath = benchmarkDirectory.getAbsolutePath()
                    + (isLinux ? "/runme.sh" : "/runme.bat");
            System.out.println("Writing bat file: " + batFilePath);
            BufferedWriter bw = new BufferedWriter(new FileWriter(batFilePath));
            // bw.append("#!/bin/bash\n");
            for (String a : this.getCommandLine()) {
                bw.append(a + (isLinux ? "\n" : "\r\n"));
            }
            bw.flush();
            bw.close();

            // Execute simulator, set working directory to benchmark directory
            // String my_command = "/bin/chmod a+x runme.sh > chmod.log";
            System.out.println("Starting bat file in " + benchmarkDirectory);

            Process p;

            if (!isLinux) {
                String my_command = "cmd /c runme.bat 1> runme.log 2>&1";
                p = Runtime.getRuntime().exec(my_command, null,
                                              benchmarkDirectory);
                p.waitFor();
            } else {
                String my_command = "/bin/chmod a+x runme.sh > chmod.log";
                p = Runtime.getRuntime().exec(my_command, null,
                                              benchmarkDirectory);
                p.waitFor();

                my_command = "/bin/bash runme.sh > runme.log";
                p = Runtime.getRuntime().exec(my_command, null,
                                              benchmarkDirectory);

                p.waitFor();
            }

            // my_command = "/bin/bash runme.sh > /tmp/runme.log";
            // p = Runtime.getRuntime().exec(my_command, null,
            // benchmarkDirectory);

            // Wait for the simulation to end
            boolean is_terminated = false;
            String reason = "";

            // Execute the benchmark
            boolean do_terminate = false;
            do {
                try {
                    System.out.println("Simulation: Let's wait for " + 3000
                            + " ms...");
                    synchronized (p) {
                        p.wait(5000);
                    }
                } catch (Exception iex) {
                    System.out.println("Trouble while waiting: "
                            + iex.getMessage());
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

            System.out.println("The simulation has been terminated because: "
                    + reason);

        } catch (Exception e) {
            System.out.println("ERROR " + e.getMessage());
            e.printStackTrace();
        }
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
