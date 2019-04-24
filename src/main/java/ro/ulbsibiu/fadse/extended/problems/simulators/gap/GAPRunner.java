package ro.ulbsibiu.fadse.extended.problems.simulators.gap;

import configuration_analysis.opt_delta.Prepare;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
// import java.security.AccessController;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import ro.ulbsibiu.fadse.extended.problems.simulators.SimulatorBase;
import ro.ulbsibiu.fadse.extended.problems.simulators.SimulatorRunner;
import shared.FileCopy;
import stepstepgui.batchhelpers.GaptimizeRunner;
import stepstepgui.benchmarks.Benchmark;
import stepstepgui.benchmarks.BenchmarkRepository;
import stepstepgui.datastructures.YamlParameters;
// import sun.security.action.GetPropertyAction;

/**
 * GAPRunner extends SimulatorRunner to properly create the command line string
 * for simulations with the GAP. Well, it also does other stuff like running
 * GAPtimize...
 * @author Andrei, Ralf
 */
public class GAPRunner extends SimulatorRunner {

    private File benchmarkDirectory;
    private static final long TIME_BETWEEN_PROCESS_CHECKS = 2000;
    private static final long TIMEOUT_TO_KILL_SIMULATION = 30000;

    /** Constructor */
    public GAPRunner(SimulatorBase simulator) {
        super(simulator);

        // Configure benchmark repository
        BenchmarkRepository.setFilenames(
                simulator.getInputDocument().getSimulatorParameter("benchmark_yaml_file"),
                null,
                simulator.getInputDocument().getSimulatorParameter("benchmark_repository_path"));

        // Configure the GAPtimize-runner
        GaptimizeRunner.setExecutable(
                simulator.getInputDocument().getSimulatorParameter("gaptimize_executable_file"));

        // Add a shutdown hook to kill GAP simulator if necessary
        Runtime.getRuntime().addShutdownHook(
                new Thread(
                new Runnable() {

                    public void run() {
                        System.out.println("## Shutdown hook is running for the GAP simulator...");
                        if (p != null) {
                            System.out.println("There is a process running.");
                            try {
                                p.destroy();
                                System.out.println("...and it should have been killed.");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        System.out.println("## The shutdown-thread terminated.");
                    }
                }));
    }

    /**
     * This method creates the simpleParameter list.
     * To have well-defined parameteres also for unused parameteres we set default values.
     */
    @Override
    public void prepareParameters() {
        this.addSimpleParameter("//lb", "//lb");

        this.addSimpleParameter(GAPConstants.DO_LOOP_ACCELERATION, "1");
        this.addSimpleParameter(GAPConstants.DO_BRANCH_PREDICTION, "1");
        this.addSimpleParameter(GAPConstants.DO_PEX, "0");
        this.addSimpleParameter(GAPConstants.DO_FUNCTION_INLINING, "0");
        this.addSimpleParameter(GAPConstants.DO_STATIC_SPECULATION, "0");
        this.addSimpleParameter(GAPConstants.DO_QDLRU, "0");

        super.prepareParameters();
    }
    private static int simulator_id = 0;

    private String getMySimulator() throws IOException {
        if (simulator_id == 0) {
            simulator_id = (int) ((Math.random() * 900) + 100);
        }

        String sim = this.simulator.getInputDocument().getSimulatorParameter("simulator_executable");
        String my_sim = sim.replace(".exe", simulator_id + ".exe");

        if (!(new File(my_sim)).canRead()) {
            FileCopy.copy(new File(sim), new File(my_sim));
        }

        return my_sim;
    }

    /**
     * @todo this must be corrected: not all parameteres are necessary for the
     * command line! find the right values and use only these.
     * @return
     */
    @Override
    protected String[] getCommandLine() {
        // The parameters as linked list of string
        LinkedList<String> sbParamList = new LinkedList<String>();

        try {
            // Add executable of simulator
            sbParamList.add(getMySimulator());
        } catch (IOException ex) {
            Logger.getLogger(GAPRunner.class.getName()).log(Level.SEVERE, null, ex);
            sbParamList.add(
                    this.simulator.getInputDocument().getSimulatorParameter("simulator_executable"));
        }

        // Output directory to build
        String outputFileString = "";

        // search for benchmark
        if (this.simpleParameters.containsKey(GAPConstants.P_TARGET_DIRECTORY)) {
            sbParamList.add(this.simpleParameters.get(GAPConstants.P_TARGET_DIRECTORY));
            outputFileString += this.simpleParameters.get(GAPConstants.P_TARGET_DIRECTORY);
        } else {
            /// AAAAH forget it.
            System.out.println("Invalid configuration.");
        }

        // set the output file path from the parameters
        outputFileString += File.separator + "results";

        String switches = "/";
        boolean do_qdlru = false;

        // For all parameter choose either default or specified value
        for (String parameter_key : GAPConstants.commandLineParameters) {
            String parameter_value = "";

            // Set Parameter to either specified value or default value
            if (this.simpleParameters.containsKey(parameter_key)) {
                // There is a value available.
                parameter_value = this.simpleParameters.get(parameter_key);
            } else if (parameter_key.equals(GAPConstants.N_COLUMNS)) {
                parameter_value = this.simpleParameters.get(GAPConstants.N_LINES);
            } else {
                // Use default value.
                parameter_value = GAPConstants.getDefaultParameterMap().get(parameter_key);
            }

            System.out.println("Checking " + parameter_key + " with value " + parameter_value);

            if (parameter_key.equals(GAPConstants.DO_LOOP_ACCELERATION)) {
                if (parameter_value.equals("1")) {
                    switches += "l";
                    outputFileString += "_loop";
                }
            } else if (parameter_key.equals(GAPConstants.DO_PEX)) {
                if (parameter_value.equals("1")) {
                    switches += "p";
                    outputFileString += "_pred";
                }
            } else if (parameter_key.equals(GAPConstants.DO_BRANCH_PREDICTION)) {
                if (parameter_value.equals("1")) {
                    switches += "b";
                    outputFileString += "_bpred";
                }
            } else if (parameter_key.equals(GAPConstants.DO_QDLRU)) {
                File confs_file = new File(
                        this.simpleParameters.get(GAPConstants.P_TARGET_DIRECTORY) + "\\" + "qdconf_"
                        + this.simpleParameters.get(GAPConstants.N_LINES) + "_"
                        + this.simpleParameters.get(GAPConstants.N_COLUMNS) + "_"
                        + this.simpleParameters.get(GAPConstants.N_LAYERS) + "_basic.txt");

                if (parameter_value.equals("1") && confs_file.canRead()) {
                    sbParamList.add("2");
                    outputFileString += "_QDLRUs";
                }
                // Do Nothing...
            } else {
                sbParamList.add(parameter_value);
                // see if string is numeric and add to output directory
                if (parameter_value.matches("[0-9]+")) {
                    outputFileString += "_" + parameter_value;
                }
            }
        }

        if (switches.length() > 1) {
            sbParamList.add(switches);
        }

        outputFileString = outputFileString.replace("_bpred_pred", "_pred_bpred");

        this.simulator.setSimulatorOutputFile(outputFileString);
        String[] result = new String[sbParamList.size()];
        sbParamList.toArray(result);

        System.out.println("We found as command line: " + sbParamList);
        System.out.println("We found as result directory: " + outputFileString);

        return result;
    }

    /** Execute :) */
    @Override
    public void run() {
        // Target Directory for Benchmark
        this.benchmarkDirectory = null;

        // Check if Benchmark exists (can be found in yaml file which has been set in gapsimin)
        String basename = individual.getBenchmark();
        Benchmark bench = BenchmarkRepository.getDump(basename);
        if (bench == null) {
            System.out.println("ERROR: Benchmark " + basename + " was not found!");
            return;
        }

        // Let's start here a large try-catch-statement. 
        // Cleanup must be done in the finally-section.
        try {
            // Create copy of benchmark

            // #################################################################
            // # INLINING
            // #################################################################
            if (simpleParameters.get(GAPConstants.DO_FUNCTION_INLINING).equals("1") && simpleParameters.get(GAPConstants.DO_FUNCTION_INLINING_FIRST).equals("1")) {
                // Create target directory
                benchmarkDirectory = new File(
                        simulator.getInputDocument().getSimulatorParameter("benchmark_target_directory"));
                benchmarkDirectory = new File(
                        benchmarkDirectory.getAbsolutePath() + "\\gap_dump_" + System.currentTimeMillis() + "_" + basename);
                benchmarkDirectory.mkdirs();
                GAPDirectoryDustman.getInstance().register(benchmarkDirectory);
                System.out.println("- Target directory: " + benchmarkDirectory);

                // Initialize parameters
                YamlParameters para = new YamlParameters(
                        bench.getDumpdir(),
                        bench.getStreamfile_compressed(),
                        benchmarkDirectory.getAbsolutePath());

                // Extract parameteres for GAPtimize

                // a) inlining
                para.put(
                        YamlParameters.DO_FUNCTION_INLINING,
                        (simpleParameters.get(GAPConstants.DO_FUNCTION_INLINING)).equals("1"));

                para.put(
                        YamlParameters.FINLINE_KPI_INSNS_PER_CALLER,
                        Integer.parseInt(simpleParameters.get(GAPConstants.FINLINE_KPI_INSNS_PER_CALLER)));
                para.put(
                        YamlParameters.FINLINE_LENGTH_OF_FUNCTION,
                        Integer.parseInt(simpleParameters.get(GAPConstants.FINLINE_LENGTH_OF_FUNCTION)));
                para.put(
                        YamlParameters.FINLINE_MAX_CALLER_COUNT,
                        Integer.parseInt(simpleParameters.get(GAPConstants.FINLINE_MAX_CALLER_COUNT)));
                para.put(
                        YamlParameters.FINLINE_WEIGHT_OF_CALLER,
                        Integer.parseInt(simpleParameters.get(GAPConstants.FINLINE_WEIGHT_OF_CALLER)));


                // b) predicated execution
                para.put(
                        YamlParameters.DO_PEX,
                        (simpleParameters.get(GAPConstants.DO_PEX)).equals("1"));

                para.put(
                        YamlParameters.DO_BRANCH_PREDICTION,
                        (simpleParameters.get(GAPConstants.DO_BRANCH_PREDICTION)).equals("1"));

                if ((simpleParameters.get(GAPConstants.DO_PEX)).equals("1")) {
                    para.put(
                            YamlParameters.PEX_MAX_TOTAL_DYNAMIC_LENGTH,
                            Integer.parseInt(simpleParameters.get(GAPConstants.PEX_MAX_TOTAL_DYNAMIC_LENGTH)));
                    para.put(
                            YamlParameters.PEX_MAX_TOTAL_DYNAMIC_LENGTH_IN_LOOPS,
                            Integer.parseInt(simpleParameters.get(GAPConstants.PEX_MAX_TOTAL_DYNAMIC_LENGTH_IN_LOOPS)));
                }

                // ##### Jetzt das ganze restliche Zeug...

                // Run GAPtimize
                System.out.println("Let's run GAPtimize...");
                GaptimizeRunner.runGaptimize(para, true);

                // Create copy of benchmark's input
                bench.copyInput(benchmarkDirectory);

                // Update Target directory
                this.simpleParameters.put(GAPConstants.P_TARGET_DIRECTORY, benchmarkDirectory.getAbsolutePath());

                // Run the simulation - it checks the results and throws an exception if something is wrong
                executeAndMonitor(bench, benchmarkDirectory);

                // Now update benchmark-object:
                // - STRC-File
                System.out.println("Current STRC-File: " + bench.getStreamfile_compressed());
                System.out.println("New STRC-File:     " + this.simulator.getSimulatorOutputFile() + "\\executed_instructions_compressed.txt");
                bench.setStreamfile_compressed(this.simulator.getSimulatorOutputFile() + "\\executed_instructions_compressed.txt");
                System.out.println("Updated STRC-File: " + bench.getStreamfile_compressed());

                // - Dump-directory
                System.out.println("Current Dump-Dir:  " + bench.getDumpdir());
                System.out.println("New Dump-Dir:      " + new File(this.simulator.getSimulatorOutputFile()).getParentFile().getAbsolutePath());
                bench.setDumpdir(new File(this.simulator.getSimulatorOutputFile()).getParentFile().getAbsolutePath());
                System.out.println("Updated Dump-Dir:  " + bench.getDumpdir());

                System.out.println("STOP NOW!");
            }

            // #################################################################
            // # STATIC SPECULATION
            // #################################################################
            if (simpleParameters.get(GAPConstants.DO_STATIC_SPECULATION).equals("1")) {
                // Create target directory
                benchmarkDirectory = new File(
                        simulator.getInputDocument().getSimulatorParameter("benchmark_target_directory"));
                benchmarkDirectory = new File(
                        benchmarkDirectory.getAbsolutePath() + "\\gap_dump_" + System.currentTimeMillis() + "_" + basename);
                benchmarkDirectory.mkdirs();
                GAPDirectoryDustman.getInstance().register(benchmarkDirectory);
                System.out.println("- Target directory: " + benchmarkDirectory);

                // Initialize parameters
                YamlParameters para = new YamlParameters(
                        bench.getDumpdir(),
                        bench.getStreamfile_compressed(),
                        benchmarkDirectory.getAbsolutePath());

                // Extract parameteres for GAPtimize

                // a) static speculation
                para.put(
                        YamlParameters.DO_STATIC_SPECULATION,
                        simpleParameters.get(GAPConstants.DO_STATIC_SPECULATION).equals("1"));

                para.put(
                        YamlParameters.SSPEC_MIN_NODE_WEIGHT,
                        Integer.parseInt(simpleParameters.get(GAPConstants.SSPEC_MIN_NODE_WEIGHT)));
                para.put(
                        YamlParameters.SSPEC_MIN_PROBABILITY_PERCENT,
                        Integer.parseInt(simpleParameters.get(GAPConstants.SSPEC_MIN_PROBABILITY_PERCENT)));
                para.put(
                        YamlParameters.SSPEC_HOT_FUNCTION_RATIO_PERCENT,
                        Integer.parseInt(simpleParameters.get(GAPConstants.SSPEC_HOT_FUNCTION_RATIO_PERCENT)));
                para.put(
                        YamlParameters.SSPEC_MAX_NUMBER_OF_INSNS_TO_SHIFT,
                        Integer.parseInt(simpleParameters.get(GAPConstants.SSPEC_MAX_NUMBER_OF_INSNS_TO_SHIFT)));
                para.put(
                        YamlParameters.SSPEC_MAX_NUMBER_OF_BLOCKS_TO_SHIFT,
                        Integer.parseInt(simpleParameters.get(GAPConstants.SSPEC_MAX_NUMBER_OF_BLOCKS_TO_SHIFT)));
                para.put(
                        YamlParameters.SSPEC_MAX_ALLOWED_ADDITIONAL_HEIGHT,
                        Integer.parseInt(simpleParameters.get(GAPConstants.SSPEC_MAX_ALLOWED_ADDITIONAL_HEIGHT)));

                // b) predicated execution
                para.put(
                        YamlParameters.DO_PEX,
                        (simpleParameters.get(GAPConstants.DO_PEX)).equals("1"));

                para.put(
                        YamlParameters.DO_BRANCH_PREDICTION,
                        (simpleParameters.get(GAPConstants.DO_BRANCH_PREDICTION)).equals("1"));

                if ((simpleParameters.get(GAPConstants.DO_PEX)).equals("1")) {
                    para.put(
                            YamlParameters.PEX_MAX_TOTAL_DYNAMIC_LENGTH,
                            Integer.parseInt(simpleParameters.get(GAPConstants.PEX_MAX_TOTAL_DYNAMIC_LENGTH)));
                    para.put(
                            YamlParameters.PEX_MAX_TOTAL_DYNAMIC_LENGTH_IN_LOOPS,
                            Integer.parseInt(simpleParameters.get(GAPConstants.PEX_MAX_TOTAL_DYNAMIC_LENGTH_IN_LOOPS)));
                }

                // ##### Jetzt das ganze restliche Zeug...

                // Run GAPtimize
                System.out.println("Let's run GAPtimize...");
                GaptimizeRunner.runGaptimize(para, true);

                // Create copy of benchmark's input
                bench.copyInput(benchmarkDirectory);

                // Update Target directory
                this.simpleParameters.put(GAPConstants.P_TARGET_DIRECTORY, benchmarkDirectory.getAbsolutePath());

                // Run the simulation - it checks the results and throws an exception if something is wrong
                executeAndMonitor(bench, benchmarkDirectory);

                // - STRC-File
                System.out.println("Current STRC-File: " + bench.getStreamfile_compressed());
                System.out.println("New STRC-File:     " + this.simulator.getSimulatorOutputFile() + "\\executed_instructions_compressed.txt");
                bench.setStreamfile_compressed(this.simulator.getSimulatorOutputFile() + "\\executed_instructions_compressed.txt");
                System.out.println("Updated STRC-File: " + bench.getStreamfile_compressed());

                // - Dump-directory
                System.out.println("Current Dump-Dir:  " + bench.getDumpdir());
                System.out.println("New Dump-Dir:      " + new File(this.simulator.getSimulatorOutputFile()).getParentFile().getAbsolutePath());
                bench.setDumpdir(new File(this.simulator.getSimulatorOutputFile()).getParentFile().getAbsolutePath());
                System.out.println("Updated Dump-Dir:  " + bench.getDumpdir());

                System.out.println("STOP NOW!");
            }

            // #################################################################
            // # INLINING
            // #################################################################
            if (simpleParameters.get(GAPConstants.DO_FUNCTION_INLINING).equals("1") && simpleParameters.get(GAPConstants.DO_FUNCTION_INLINING_FIRST).equals("0")) {
                // Create target directory
                benchmarkDirectory = new File(
                        simulator.getInputDocument().getSimulatorParameter("benchmark_target_directory"));
                benchmarkDirectory = new File(
                        benchmarkDirectory.getAbsolutePath() + "\\gap_dump_" + System.currentTimeMillis() + "_" + basename);
                benchmarkDirectory.mkdirs();
                GAPDirectoryDustman.getInstance().register(benchmarkDirectory);
                System.out.println("- Target directory: " + benchmarkDirectory);

                // Initialize parameters
                YamlParameters para = new YamlParameters(
                        bench.getDumpdir(),
                        bench.getStreamfile_compressed(),
                        benchmarkDirectory.getAbsolutePath());

                // Extract parameteres for GAPtimize

                // a) inlining
                para.put(
                        YamlParameters.DO_FUNCTION_INLINING,
                        (simpleParameters.get(GAPConstants.DO_FUNCTION_INLINING)).equals("1"));

                para.put(
                        YamlParameters.FINLINE_KPI_INSNS_PER_CALLER,
                        Integer.parseInt(simpleParameters.get(GAPConstants.FINLINE_KPI_INSNS_PER_CALLER)));
                para.put(
                        YamlParameters.FINLINE_LENGTH_OF_FUNCTION,
                        Integer.parseInt(simpleParameters.get(GAPConstants.FINLINE_LENGTH_OF_FUNCTION)));
                para.put(
                        YamlParameters.FINLINE_MAX_CALLER_COUNT,
                        Integer.parseInt(simpleParameters.get(GAPConstants.FINLINE_MAX_CALLER_COUNT)));
                para.put(
                        YamlParameters.FINLINE_WEIGHT_OF_CALLER,
                        Integer.parseInt(simpleParameters.get(GAPConstants.FINLINE_WEIGHT_OF_CALLER)));


                // b) predicated execution
                para.put(
                        YamlParameters.DO_PEX,
                        (simpleParameters.get(GAPConstants.DO_PEX)).equals("1"));

                para.put(
                        YamlParameters.DO_BRANCH_PREDICTION,
                        (simpleParameters.get(GAPConstants.DO_BRANCH_PREDICTION)).equals("1"));

                if ((simpleParameters.get(GAPConstants.DO_PEX)).equals("1")) {
                    para.put(
                            YamlParameters.PEX_MAX_TOTAL_DYNAMIC_LENGTH,
                            Integer.parseInt(simpleParameters.get(GAPConstants.PEX_MAX_TOTAL_DYNAMIC_LENGTH)));
                    para.put(
                            YamlParameters.PEX_MAX_TOTAL_DYNAMIC_LENGTH_IN_LOOPS,
                            Integer.parseInt(simpleParameters.get(GAPConstants.PEX_MAX_TOTAL_DYNAMIC_LENGTH_IN_LOOPS)));
                }

                // ##### Jetzt das ganze restliche Zeug...

                // Run GAPtimize
                System.out.println("Let's run GAPtimize...");
                GaptimizeRunner.runGaptimize(para, true);

                // Create copy of benchmark's input
                bench.copyInput(benchmarkDirectory);

                // Update Target directory
                this.simpleParameters.put(GAPConstants.P_TARGET_DIRECTORY, benchmarkDirectory.getAbsolutePath());

                // Run the simulation - it checks the results and throws an exception if something is wrong
                executeAndMonitor(bench, benchmarkDirectory);

                // Now update benchmark-object:
                // - STRC-File
                System.out.println("Current STRC-File: " + bench.getStreamfile_compressed());
                System.out.println("New STRC-File:     " + this.simulator.getSimulatorOutputFile() + "\\executed_instructions_compressed.txt");
                bench.setStreamfile_compressed(this.simulator.getSimulatorOutputFile() + "\\executed_instructions_compressed.txt");
                System.out.println("Updated STRC-File: " + bench.getStreamfile_compressed());

                // - Dump-directory
                System.out.println("Current Dump-Dir:  " + bench.getDumpdir());
                System.out.println("New Dump-Dir:      " + new File(this.simulator.getSimulatorOutputFile()).getParentFile().getAbsolutePath());
                bench.setDumpdir(new File(this.simulator.getSimulatorOutputFile()).getParentFile().getAbsolutePath());
                System.out.println("Updated Dump-Dir:  " + bench.getDumpdir());

                System.out.println("STOP NOW!");
            }


            // #################################################################
            // # PREDICATED EXECUTION
            // #################################################################
            /* if (simpleParameters.get(GAPConstants.DO_PEX).equals("1")) {
            // Create target directory
            benchmarkDirectory = new File(
            simulator.getInputDocument().getSimulatorParameter("benchmark_target_directory"));
            benchmarkDirectory = new File(
            benchmarkDirectory.getAbsolutePath() + "\\gap_dump_" + System.currentTimeMillis() + "_" + basename);
            benchmarkDirectory.mkdirs();
            GAPDirectoryDustman.getInstance().register(benchmarkDirectory);
            System.out.println("- TGAPDirectoryDustmanarget directory: " + benchmarkDirectory);

            // Initialize parameters
            YamlParameters para = new YamlParameters(
            bench.getDumpdir(),
            bench.getStreamfile_compressed(),
            benchmarkDirectory.getAbsolutePath());

            // Extract parameteres for GAPtimize

            // b) predicated execution
            para.put(
            YamlParameters.DO_PEX,
            (simpleParameters.get(GAPConstants.DO_PEX)).equals("1"));

            para.put(
            YamlParameters.DO_BRANCH_PREDICTION,
            (simpleParameters.get(GAPConstants.DO_BRANCH_PREDICTION)).equals("1"));

            para.put(
            YamlParameters.PEX_MAX_TOTAL_DYNAMIC_LENGTH,
            Integer.parseInt(simpleParameters.get(GAPConstants.PEX_MAX_TOTAL_DYNAMIC_LENGTH)));
            para.put(
            YamlParameters.PEX_MAX_TOTAL_DYNAMIC_LENGTH_IN_LOOPS,
            Integer.parseInt(simpleParameters.get(GAPConstants.PEX_MAX_TOTAL_DYNAMIC_LENGTH_IN_LOOPS)));

            // ##### Jetzt das ganze restliche Zeug...

            // Run GAPtimize
            System.out.println("Let's run GAPtimize...");
            GaptimizeRunner.runGaptimize(para, true);

            // Create copy of benchmark's input
            bench.copyInput(benchmarkDirectory);

            // Update Target directory
            this.simpleParameters.put(GAPConstants.P_TARGET_DIRECTORY, benchmarkDirectory.getAbsolutePath());

            // Run the simulation - it checks the results and throws an exception if something is wrong
            executeAndMonitor(bench, benchmarkDirectory);

            // - STRC-File
            System.out.println("Current STRC-File: " + bench.getStreamfile_compressed());
            System.out.println("New STRC-File:     " + this.simulator.getSimulatorOutputFile() + "\\executed_instructions_compressed.txt");
            bench.setStreamfile_compressed(this.simulator.getSimulatorOutputFile() + "\\executed_instructions_compressed.txt");
            System.out.println("Updated STRC-File: " + bench.getStreamfile_compressed());

            // - Dump-directory
            System.out.println("Current Dump-Dir:  " + bench.getDumpdir());
            System.out.println("New Dump-Dir:      " + new File(this.simulator.getSimulatorOutputFile()).getParentFile().getAbsolutePath());
            bench.setDumpdir(new File(this.simulator.getSimulatorOutputFile()).getParentFile().getAbsolutePath());
            System.out.println("Updated Dump-Dir:  " + bench.getDumpdir());

            System.out.println("Finished with predication!");
            } */


            // #################################################################
            // # QDLRU
            // #################################################################
            if (simpleParameters.get(GAPConstants.DO_QDLRU).equals("1")) {

                System.out.println("Überprüft wird folgende Datei: " + new File(this.simulator.getSimulatorOutputFile() + "//layer_trace.txt").getAbsolutePath());
                if (!(new File(this.simulator.getSimulatorOutputFile() + "//layer_trace.txt")).canRead()) {
                    // Create target directory
                    benchmarkDirectory = new File(
                            simulator.getInputDocument().getSimulatorParameter("benchmark_target_directory"));
                    benchmarkDirectory = new File(
                            benchmarkDirectory.getAbsolutePath() + "\\gap_dump_" + System.currentTimeMillis() + "_" + basename);
                    benchmarkDirectory.mkdirs();
                    GAPDirectoryDustman.getInstance().register(benchmarkDirectory);
                    System.out.println("- Target directory: " + benchmarkDirectory);

                    if (simpleParameters.get(GAPConstants.DO_PEX).equals("1")) {
                        // Create Copy of Benchmark with Predicated Execution

                        // Initialize parameters
                        YamlParameters para = new YamlParameters(
                                bench.getDumpdir(),
                                bench.getStreamfile_compressed(),
                                benchmarkDirectory.getAbsolutePath());

                        // Extract parameteres for GAPtimize

                        // b) predicated execution
                        para.put(
                                YamlParameters.DO_PEX,
                                (simpleParameters.get(GAPConstants.DO_PEX)).equals("1"));

                        para.put(
                                YamlParameters.DO_BRANCH_PREDICTION,
                                (simpleParameters.get(GAPConstants.DO_BRANCH_PREDICTION)).equals("1"));

                        if ((simpleParameters.get(GAPConstants.DO_PEX)).equals("1")) {
                            para.put(
                                    YamlParameters.PEX_MAX_TOTAL_DYNAMIC_LENGTH,
                                    Integer.parseInt(simpleParameters.get(GAPConstants.PEX_MAX_TOTAL_DYNAMIC_LENGTH)));
                            para.put(
                                    YamlParameters.PEX_MAX_TOTAL_DYNAMIC_LENGTH_IN_LOOPS,
                                    Integer.parseInt(simpleParameters.get(GAPConstants.PEX_MAX_TOTAL_DYNAMIC_LENGTH_IN_LOOPS)));
                        }

                        // ##### Jetzt das ganze restliche Zeug...

                        // Run GAPtimize
                        System.out.println("Let's run GAPtimize...");
                        GaptimizeRunner.runGaptimize(para, true);
                    } else {
                        // Create Copy of Benchmark
                        bench.copyBenchmark(benchmarkDirectory);
                    }

                    // Create Copy of benchmark's input
                    bench.copyInput(benchmarkDirectory);

                    // Update Target directory
                    this.simpleParameters.put(GAPConstants.P_TARGET_DIRECTORY, benchmarkDirectory.getAbsolutePath());

                    // Run the simulation - it checks the results and throws an exception if something is wrong
                    executeAndMonitor(bench, benchmarkDirectory);

                    // Now update benchmark-object:
                    // - STRC-File
                    System.out.println("Current STRC-File: " + bench.getStreamfile_compressed());
                    System.out.println("New STRC-File:     " + this.simulator.getSimulatorOutputFile() + "\\executed_instructions_compressed.txt");
                    bench.setStreamfile_compressed(this.simulator.getSimulatorOutputFile() + "\\executed_instructions_compressed.txt");
                    System.out.println("Updated STRC-File: " + bench.getStreamfile_compressed());

                    // - Dump-directory
                    System.out.println("Current Dump-Dir:  " + bench.getDumpdir());
                    System.out.println("New Dump-Dir:      " + new File(this.simulator.getSimulatorOutputFile()).getParentFile().getAbsolutePath());
                    bench.setDumpdir(new File(this.simulator.getSimulatorOutputFile()).getParentFile().getAbsolutePath());
                    System.out.println("Updated Dump-Dir:  " + bench.getDumpdir());
                }

                StringBuffer configurations = Prepare.prepare(
                        new File(this.simulator.getSimulatorOutputFile() + "//layer_trace.txt"),
                        Integer.parseInt(this.simpleParameters.get(GAPConstants.N_LAYERS)));

                File confs_file = new File(
                        benchmarkDirectory.getAbsoluteFile() + "\\" + "qdconf_"
                        + this.simpleParameters.get(GAPConstants.N_LINES) + "_"
                        + this.simpleParameters.get(GAPConstants.N_COLUMNS) + "_"
                        + this.simpleParameters.get(GAPConstants.N_LAYERS) + "_basic.txt");

                // BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(confs_file));
                BufferedWriter bw = new BufferedWriter(new FileWriter(confs_file));
                bw.append(configurations);
                bw.flush();
                bw.close();

                // Now just run the same stuff again...
                // Run the simulation - it checks the results and throws an exception if something is wrong
                executeAndMonitor(bench, benchmarkDirectory);
            }

            if (!simpleParameters.get(GAPConstants.DO_FUNCTION_INLINING).equals("1")
                    && !simpleParameters.get(GAPConstants.DO_STATIC_SPECULATION).equals("1")
                    // && !simpleParameters.get(GAPConstants.DO_PEX).equals("1")
                    && !simpleParameters.get(GAPConstants.DO_QDLRU).equals("1")) {
                // Create target directory
                benchmarkDirectory = new File(
                        simulator.getInputDocument().getSimulatorParameter("benchmark_target_directory"));
                benchmarkDirectory = new File(
                        benchmarkDirectory.getAbsolutePath() + "\\gap_dump_" + System.currentTimeMillis() + "_" + basename);
                benchmarkDirectory.mkdirs();
                GAPDirectoryDustman.getInstance().register(benchmarkDirectory);
                System.out.println("- Target directory: " + benchmarkDirectory);

                // ##### Jetzt das ganze restliche Zeug...

                // Create Copy of Benchmark or Run GAPtimize
                if (simpleParameters.get(GAPConstants.DO_PEX).equals("1")) {
                    // Create Copy of Benchmark with Predicated Execution

                    // Initialize parameters
                    YamlParameters para = new YamlParameters(
                            bench.getDumpdir(),
                            bench.getStreamfile_compressed(),
                            benchmarkDirectory.getAbsolutePath());

                    // Extract parameteres for GAPtimize

                    // b) predicated execution
                    para.put(
                            YamlParameters.DO_PEX,
                            (simpleParameters.get(GAPConstants.DO_PEX)).equals("1"));

                    para.put(
                            YamlParameters.DO_BRANCH_PREDICTION,
                            (simpleParameters.get(GAPConstants.DO_BRANCH_PREDICTION)).equals("1"));

                    if ((simpleParameters.get(GAPConstants.DO_PEX)).equals("1")) {
                        para.put(
                                YamlParameters.PEX_MAX_TOTAL_DYNAMIC_LENGTH,
                                Integer.parseInt(simpleParameters.get(GAPConstants.PEX_MAX_TOTAL_DYNAMIC_LENGTH)));
                        para.put(
                                YamlParameters.PEX_MAX_TOTAL_DYNAMIC_LENGTH_IN_LOOPS,
                                Integer.parseInt(simpleParameters.get(GAPConstants.PEX_MAX_TOTAL_DYNAMIC_LENGTH_IN_LOOPS)));
                    }

                    // ##### Jetzt das ganze restliche Zeug...

                    // Run GAPtimize
                    System.out.println("Let's run GAPtimize...");
                    GaptimizeRunner.runGaptimize(para, true);
                } else {
                    // Create Copy of Benchmark
                    bench.copyBenchmark(benchmarkDirectory);
                }

                // Create copy of benchmark's input
                bench.copyInput(benchmarkDirectory);

                // Update Target directory
                this.simpleParameters.put(GAPConstants.P_TARGET_DIRECTORY, benchmarkDirectory.getAbsolutePath());

                // Run the simulation - it checks the results and throws an exception if something is wrong
                executeAndMonitor(bench, benchmarkDirectory);

                // - STRC-File
                System.out.println("Current STRC-File: " + bench.getStreamfile_compressed());
                System.out.println("New STRC-File:     " + this.simulator.getSimulatorOutputFile() + "\\executed_instructions_compressed.txt");
                bench.setStreamfile_compressed(this.simulator.getSimulatorOutputFile() + "\\executed_instructions_compressed.txt");
                System.out.println("Updated STRC-File: " + bench.getStreamfile_compressed());

                // - Dump-directory
                System.out.println("Current Dump-Dir:  " + bench.getDumpdir());
                System.out.println("New Dump-Dir:      " + new File(this.simulator.getSimulatorOutputFile()).getParentFile().getAbsolutePath());
                bench.setDumpdir(new File(this.simulator.getSimulatorOutputFile()).getParentFile().getAbsolutePath());
                System.out.println("Updated Dump-Dir:  " + bench.getDumpdir());

                System.out.println("STOP NOW!");
            }

            // c) predicated execution
            /* para.put(
            YamlParameters.DO_PEX,
            (simpleParameters.get(GAPConstants.DO_PEX)).equals("1"));

            if (simpleParameters.get(GAPConstants.DO_PEX).equals("1")) {
            para.put(
            YamlParameters.PEX_MAX_TOTAL_DYNAMIC_LENGTH,
            Integer.parseInt(simpleParameters.get(GAPConstants.PEX_MAX_TOTAL_DYNAMIC_LENGTH)));
            para.put(
            YamlParameters.PEX_MAX_TOTAL_DYNAMIC_LENGTH_IN_LOOPS,
            Integer.parseInt(simpleParameters.get(GAPConstants.PEX_MAX_TOTAL_DYNAMIC_LENGTH_IN_LOOPS)));

            if (simpleParameters.get(GAPConstants.PEX_DECIDEDNESS_MIN) != null) {
            para.put(
            YamlParameters.PEX_DECIDEDNESS_MIN,
            Integer.parseInt(simpleParameters.get(GAPConstants.PEX_DECIDEDNESS_MIN)));
            }
            if (simpleParameters.get(GAPConstants.PEX_DECIDEDNESS_MAX) != null) {
            para.put(
            YamlParameters.PEX_DECIDEDNESS_MAX,
            Integer.parseInt(simpleParameters.get(GAPConstants.PEX_DECIDEDNESS_MAX)));
            }
            if (simpleParameters.get(GAPConstants.PEX_RELEVANCE_MIN) != null) {
            para.put(
            YamlParameters.PEX_RELEVANCE_MIN,
            Integer.parseInt(simpleParameters.get(GAPConstants.PEX_RELEVANCE_MIN)));
            }
            if (simpleParameters.get(GAPConstants.PEX_RELEVANCE_MAX) != null) {
            para.put(
            YamlParameters.PEX_RELEVANCE_MAX,
            Integer.parseInt(simpleParameters.get(GAPConstants.PEX_RELEVANCE_MAX)));
            }
            } */

            // Check if parameteres are set in a manner to modify the benchmark and, eigher way, create copy of benchmark
            /* System.out.println("Parameters so far: " + para.toLongString());
            if (para.isModifyingBenchmark()) {
            // Run GAPtimize
            System.out.println("Let's run GAPtimize...");
            GaptimizeRunner.runGaptimize(para, true);
            } else {
            // Copy benchmarks
            System.out.println("GAPtimize not needed, let's copy the benchmark...");
            bench.copyBenchmark(benchmarkDirectory);
            }

            // Create copy of benchmark's input
            bench.copyInput(benchmarkDirectory);

            // Update Target directory
            this.simpleParameters.put(GAPConstants.P_TARGET_DIRECTORY, benchmarkDirectory.getAbsolutePath());

            // Run the simulation - it checks the results and throws an exception if something is wrong
            executeAndMonitor(bench, benchmarkDirectory); */
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
        System.out.println("- Simulation completed (with or without errors)");
    }
    long last_successful_progression_check = System.currentTimeMillis();
    int last_progression_check_executed_instructions = -1;

    private void initProgressionCheck() {
        last_successful_progression_check = System.currentTimeMillis();
        last_progression_check_executed_instructions = -1;
    }

    /** Checks if there is still some progress in the simulation, Not yet implemented. */
    private boolean isExecutionProgressing() {
        int executed_instructions = this.getExecutedInstructions();
        if (executed_instructions > last_progression_check_executed_instructions) {
            last_progression_check_executed_instructions = executed_instructions;
            last_successful_progression_check = System.currentTimeMillis();
            return true;
        } else if (System.currentTimeMillis() - last_successful_progression_check < TIMEOUT_TO_KILL_SIMULATION) {
            // It has a chance for some sec...
            return true;
        } else {
            return false;
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

    private int getExecutedInstructions() {
        int executed_instructions = -1;

        // Get last line of ipcDump.txt
        String last_line = null;
        String resultDirName = this.simulator.getSimulatorOutputFile();
        File ipcDump = new File(resultDirName + "\\" + "IpcDump.txt");
        // System.out.println("File to monitor: " + ipcDump);

        try {
            // Fetch last line
            last_line = getLastLineFromFile(ipcDump);

            // Check last line if it is OK
            if (last_line != null && last_line.length() > 0) {
                // Check the last line of the file
                Scanner sc = new Scanner(last_line);
                if (sc.hasNextInt()) {
                    executed_instructions = sc.nextInt();
                }
                sc.close();
            }
        } catch (IOException ex) {
            System.out.println("Exception while checking ipcDump.txt: " + ex.getMessage());
        }

        return executed_instructions;
    }

    /** Checks if the simulation has executed more than N percent instructinons than the ref simulation */
    private boolean isExecutionOutOfBounds(int ref_instructions, double threshold) {
        // Return true if no ref-value is given
        if (ref_instructions <= 0) {
            return false;
        }

        // Else: Check it

        double allowed_instructions = ref_instructions * (1 + threshold);
        double executed_instructions = this.getExecutedInstructions();
        double percentage = (double) executed_instructions / (double) allowed_instructions;

        if (executed_instructions <= allowed_instructions) {
            // in case of an error, executed_instructions is smaller than 0.
            System.out.println("OK " + executed_instructions + " <= " + allowed_instructions + " (" + percentage + ")");
            return false;
        } else {
            System.out.println("ERROR " + executed_instructions + " > " + allowed_instructions + " (" + percentage + ")");
            return true;
        }
    }

    /** Returns the last line of a file as String */
    private String getLastLineFromFile_slow(File file) throws IOException {
        IOException ioex = null;

        String last_line = null, prev_line = null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));

            prev_line = null;
            while ((last_line = br.readLine()) != null) {
                prev_line = last_line;
            }
            last_line = prev_line;
        } catch (IOException ex) {
            ioex = ex;
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (Exception e) {
                // who cares.
            }
        }

        if (ioex != null) {
            throw ioex;
        }

        System.out.println("Last line of " + file + " is: " + last_line);
        return last_line;
    }

    public String getLastLineFromFile(File file) throws IOException, FileNotFoundException {
        String lastLine = null;
        RandomAccessFile fileHandler = null;
        try {
            fileHandler = new RandomAccessFile(file, "r");
            long fileLength = file.length() - 1;
            StringBuilder sb = new StringBuilder();

            for (long filePointer = fileLength; filePointer != -1; filePointer--) {
                fileHandler.seek(filePointer);
                int readByte = fileHandler.readByte();

                if (readByte == 0xA) {
                    if (filePointer == fileLength) {
                        continue;
                    } else {
                        break;
                    }
                } else if (readByte == 0xD) {
                    if (filePointer == fileLength - 1) {
                        continue;
                    } else {
                        break;
                    }
                }

                sb.append((char) readByte);
            }

            lastLine = sb.reverse().toString();
        } catch (FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (fileHandler != null) {
                    fileHandler.close();
                }
            } catch (Exception e) {
                // who cares.
            }
        }
        return lastLine;
    }

    private void addOptimizationTimeToResultFile(long optimization_time) {
        // TODO: regroup results
        File file = new File(this.simulator.getSimulatorOutputFile());

        // Check file it is dir or not...
        if (file.isDirectory()) {
            // Somebody specified a directory... browse through it.
            for (File item : file.listFiles()) {
                if (item.getName().endsWith("results.txt")) {
                    file = item;
                    break;
                }
            }
        }
        System.out.println("Found result file: " + file);

        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(file, true));
            String line = GAPOutputParser.OBJECTIVE_OPTIMIZATION_TIME + " : " + optimization_time;
            bw.append("\r\n" + line + "\r\n");

        } catch (IOException ex) {
            Logger.getLogger(GAPRunner.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
            } catch (Exception e) {
                // Nothing to do
            }
        }

        // throw new UnsupportedOperationException("Not yet implemented");
    }

    private void generateBatchForGap(String absolutePath) {
        File file = new File(absolutePath + "\\run.bat");

        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(file, true));

            String executeCommand = "";
            for (String s : this.getCommandLine()) {
                executeCommand += " " + s;
            }
            bw.append("REM " + individual + "\r\n");
            bw.append(executeCommand + "\r\n");
            bw.append("\r\n");

        } catch (IOException ex) {
            Logger.getLogger(GAPRunner.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
            } catch (Exception e) {
                // Nothing to do
            }
        }
    }

    private void executeAndMonitor(Benchmark bench, File benchmarkDirectory) throws Exception {
        // Prepare command to execute
        String executeCommand = "";
        for (String s : this.getCommandLine()) {
            executeCommand += " " + s;
        }

        System.out.println(
                "- Starting simulator: ["
                + simulator.getInputDocument().getSimulatorName()
                + "] with the following command: \n" + executeCommand);

        // Init progression check
        initProgressionCheck();

        // Generate batch file for manual re-execution
        generateBatchForGap(benchmarkDirectory.getAbsolutePath());

        // Now check for all input data if there is already a zip file which can be used... If yes, extract its content to the benchmark directory. else perform simulation.
        boolean simulation_must_be_done = true;
        File zipfile = null;
        try {
            String[] cmd = this.getCommandLine();
            cmd[0] = "";
            cmd[1] = "";
            String my_cmd = "";
            for (String a : cmd) {
                my_cmd += " " + a;
            }
            my_cmd = my_cmd.trim();
            String checksum = getInputChecksum(bench, benchmarkDirectory, my_cmd, simulator.getInputDocument().getSimulatorName());

            String zipname = benchmarkDirectory.getAbsoluteFile().getParent() + "\\" + checksum + ".zip";
            System.out.println("##### Name of the Zipfile: " + zipname);
            zipfile = new File(zipname);

            if (zipfile.canRead()) {
                System.out.println("########################################### EXTRACTION Starting");
                GAPResultDustman.getInstance().register(zipfile);
                extractZipFile(zipfile, new File(this.simulator.getSimulatorOutputFile()));
                simulation_must_be_done = false;
                System.out.println("########################################### EXTRACTION Finished");
            }

        } catch (Exception e) {
            System.out.println("Exception during checksum calculation: " + e.getMessage());
            e.printStackTrace();
        }

        // start simulation if it really must be done...
        if (simulation_must_be_done) {
            // Execute simulator, set working directory to benchmark directory
            p = Runtime.getRuntime().exec(this.getCommandLine(), null, benchmarkDirectory);

            // Wait for the simulation to end
            boolean is_terminated = false;
            boolean is_progressing = true;
            boolean is_out_of_bound = false;
            String reason = "";
            boolean do_terminate = false;

            // Execute the benchmark
            do {
                try {
                    System.out.println(
                            "Simulation: Let's wait for " + TIME_BETWEEN_PROCESS_CHECKS + " ms...");
                    synchronized (p) {
                        p.wait(TIME_BETWEEN_PROCESS_CHECKS);
                    }
                } catch (Exception iex) {
                    System.out.println("Trouble while waiting: " + iex.getMessage());
                }

                // Check if we want to stop waiting for the simulation...
                is_terminated = isExecutionTerminated(p);
                is_progressing = isExecutionProgressing();
                is_out_of_bound = isExecutionOutOfBounds(
                        bench.getExecuted_instructions_ref(), 0.25);

                if (is_terminated) {
                    reason = "The process has terminated.";
                    do_terminate = true;
                } else if (!is_progressing) {
                    reason = "There is no progress in the simulation detectable.";
                    do_terminate = true;
                } else if (is_out_of_bound) {
                    reason = "The simulation has hit its predefined bounds.";
                    do_terminate = true;
                } else {
                    do_terminate = false;
                }
            } while (!do_terminate);
            System.out.println("The Simulation has been terminated because: " + reason);

            // Check correctness of generated data
            HashMap<String, String> files = new HashMap<String, String>();
            System.out.println("Interesting filenames: ");
            System.out.println("REF  " + bench.getOutput_files_reference());
            System.out.println("CALC " + bench.getOutput_files_generated());

            for (int i = 0; i < bench.getOutput_files_reference().size(); i++) {
                String ref = bench.getOutput_files_reference().get(i);
                String gen = bench.getOutput_files_generated().get(i);
                if (gen.contains("RESULT")) {
                    gen = gen.replace("RESULT", this.simulator.getSimulatorOutputFile());
                }
                System.out.println("files to compare: " + ref + " and " + gen);
                files.put(ref, gen);
            }

            if (GAPResultChecker.compareResults(files)) {
                System.out.println("Correct!");

                // Now save the output, use MD5 of all input data as filename
                try {
                    if (zipfile != null) {
                        createZipFile(this.simulator.getSimulatorOutputFile(), zipfile, benchmarkDirectory);
                        GAPResultDustman.getInstance().register(zipfile);
                    }
                } catch (Exception e) {
                    System.out.println("Exception during creation of zip: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                this.getIndividual().markAsInfeasibleAndSetBadValuesForObjectives("Output is not correct");
                throw new Exception("Calculated data does not match reference data");
            }
        } else {
            // Nothing to do :)
        }
    }

    private String getInputChecksum(Benchmark bench, File benchmarkDirectory, String a, String b) throws FileNotFoundException, IOException, NoSuchAlgorithmException {
        TreeMap<String, String> checksums = new TreeMap<String, String>();

        File[] listOfFiles = benchmarkDirectory.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile() && listOfFiles[i].getName().endsWith(".hex")) {
                String my_checksum = getChecksum(listOfFiles[i]);
                System.out.println("We found a file, it should be checksummed: " + listOfFiles[i].getName() + " => " + my_checksum);
                checksums.put(listOfFiles[i].getName(), my_checksum);
            }
        }

        String checksum = "";
        for (String value : checksums.values()) {
            checksum += value + " ";
        }

        System.out.println("Adding to Checksum: " + a + " => " + getChecksum(a));
        checksum += getChecksum(a) + " ";

        System.out.println("Adding to Checksum: " + b + " => " + getChecksum(b));
        checksum += getChecksum(b) + " ";

        String summary_checksum = getChecksum(checksum);
        System.out.println("All checksums together => " + checksum + " => " + summary_checksum);

        return bench.getBasename() + "_" + summary_checksum;
    }

    private String getChecksum(File input) throws FileNotFoundException, IOException, NoSuchAlgorithmException {
        /* FileInputStream file = new FileInputStream(input);
        CheckedInputStream check = new CheckedInputStream(file, new CRC32());
        BufferedInputStream in = new BufferedInputStream(check);

        while (in.read() != -1) {
        // Read file in completely
        }
        file.close();

        long result = check.getChecksum().getValue();

        return result; */

        MessageDigest messagedigest = MessageDigest.getInstance("SHA");
        byte[] md = new byte[1024];
        InputStream in = new FileInputStream(input);
        for (int n = 0; (n = in.read(md)) > -1;) {
            messagedigest.update(md, 0, n);
        }
        in.close();
        byte[] digest = messagedigest.digest();
        String resultString = StringUtils.getHexString(digest);
        return resultString;
    }

    private String getChecksum(String b) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        /* CRC32 c2 = new CRC32();
        c2.update(b.getBytes());
        long checksum = c2.getValue();
        return checksum; */
        MessageDigest messagedigest = MessageDigest.getInstance("SHA");
        messagedigest.update(b.getBytes());
        byte[] digest = messagedigest.digest();
        String resultString = StringUtils.getHexString(digest);
        return resultString;
    }

    private void extractZipFile(File file, File benchmarkDirectory) throws ZipException, IOException {
        // Extract zip file...
        int BUFFER = 2048;

        ZipFile zip = new ZipFile(file);
        benchmarkDirectory.mkdirs();

        String newPath = benchmarkDirectory.getAbsolutePath();

        Enumeration zipFileEntries = zip.entries();

        // Process each entry
        while (zipFileEntries.hasMoreElements()) {
            // grab a zip file entry
            ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();

            String currentEntry = entry.getName();

            File destFile = new File(newPath, currentEntry);
            destFile = new File(newPath, destFile.getName());
            File destinationParent = destFile.getParentFile();

            // create the parent directory structure if needed
            destinationParent.mkdirs();
            if (!entry.isDirectory()) {
                BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
                int currentByte;

                // establish buffer for writing file
                byte data[] = new byte[BUFFER];

                // write the current file to disk
                FileOutputStream fos = new FileOutputStream(destFile);
                BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);

                // read and write until last byte is encountered
                while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, currentByte);
                }
                dest.flush();
                dest.close();
                is.close();
            }
        }
    }

    private void createZipFile(String resultDirectory, File zipfile, File benchmarkDirectory) throws FileNotFoundException, IOException {
        ZipOutputStream zip = null;
        FileOutputStream fileWriter = null;
        zip = null;

        fileWriter = new FileOutputStream(zipfile);
        zip = new ZipOutputStream(fileWriter);

        addFolderToZip("", resultDirectory, zip);
        zip.flush();
        zip.close();
    }

    static private void addFileToZip(String path, String srcFile, ZipOutputStream zip) throws FileNotFoundException, IOException {
        File folder = new File(srcFile);
        if (folder.isDirectory()) {
            addFolderToZip(path, srcFile, zip);
        } else {
            byte[] buf = new byte[1024];
            int len;
            FileInputStream in = new FileInputStream(srcFile);
            zip.putNextEntry(new ZipEntry(path + "/" + folder.getName()));
            while ((len = in.read(buf)) > 0) {
                zip.write(buf, 0, len);
            }
        }
    }

    static private void addFolderToZip(String path, String srcFolder, ZipOutputStream zip) throws FileNotFoundException, IOException {
        File folder = new File(srcFolder);
        for (String fileName : folder.list()) {
            if (path.equals("") /* && path.contains("results_")*/) {
                addFileToZip(folder.getName(), srcFolder + "/" + fileName, zip);
            } else {
                addFileToZip(path + "/" + folder.getName(), srcFolder + "/" + fileName, zip);
            }
        }
    }
}

class StringUtils {

    static final byte[] HEX_CHAR_TABLE = {
        (byte) '0', (byte) '1', (byte) '2', (byte) '3',
        (byte) '4', (byte) '5', (byte) '6', (byte) '7',
        (byte) '8', (byte) '9', (byte) 'a', (byte) 'b',
        (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f'
    };

    public static String getHexString(byte[] raw)
            throws UnsupportedEncodingException {
        byte[] hex = new byte[2 * raw.length];
        int index = 0;

        for (byte b : raw) {
            int v = b & 0xFF;
            hex[index++] = HEX_CHAR_TABLE[v >>> 4];
            hex[index++] = HEX_CHAR_TABLE[v & 0xF];
        }
        return new String(hex, "ASCII");
    }

    public static void main(String args[]) throws Exception {
        byte[] byteArray = {
            (byte) 255, (byte) 254, (byte) 253,
            (byte) 252, (byte) 251, (byte) 250
        };

        System.out.println(StringUtils.getHexString(byteArray));

        /*
         * output :
         *   fffefdfcfbfa
         */

    }
}
