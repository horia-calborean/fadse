package core.network.client;

import java.io.*;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import core.model.individual.FadseIndividual;
import input.model.InputData;
import input.model.setup.CommonSetupParameters;
import input.ports.parameter.problem.ProblemParameter;

/**
 * SimulatorRunner - Executes external simulator processes for FADSE individuals.
 *
 * Improvements:
 * - Proper resource management (streams always closed)
 * - Consumes both stdout and stderr to prevent process hanging
 * - Thread-safe process management
 * - Better exception handling with proper logging
 * - Null safety validation
 * - Configurable process timeout
 */
public class SimulatorRunner implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(SimulatorRunner.class.getName());

    // Configuration
    private static final long PROCESS_TIMEOUT_MINUTES = 60;  // 1 hour default

    protected Simulator simulator;
    protected LinkedHashMap<String, String> simpleParameters;
    protected ProblemParameter<?>[] currentParameters;
    protected volatile Process p = null;  // Volatile for thread-safe stopRunning()
    protected FadseIndividual individual;

    public SimulatorRunner(Simulator simulator) {
        this.simulator = Objects.requireNonNull(simulator, "Simulator cannot be null");
        this.simpleParameters = new LinkedHashMap<>();
    }

    /**
     * Adds or updates a simulation parameter.
     *
     * @param name Parameter name (cannot be null)
     * @param value Parameter value (cannot be null)
     */
    public void addSimpleParameter(String name, String value) {
        Objects.requireNonNull(name, "Parameter name cannot be null");
        Objects.requireNonNull(value, "Parameter value cannot be null");
        simpleParameters.put(name, value);
    }

    public Map<String, String> getSimpleParameters(){
        return this.simpleParameters;
    }

    /**
     * Builds the command line arguments for the simulator process.
     *
     * @return Array of command line arguments
     * @throws IllegalStateException if individual not set or configuration missing
     */
    protected String[] getCommandLine() {
        if (individual == null) {
            throw new IllegalStateException("Individual not set - call setIndividual() first");
        }

        InputData inputData = individual.getInputData();
        if (inputData == null) {
            throw new IllegalStateException("Individual has no InputData");
        }

        @SuppressWarnings("unchecked")
        Map<String, String> problemConfigParameters = (Map<String, String>) inputData.get(CommonSetupParameters.PROBLEM_CONFIG);
        if (problemConfigParameters == null) {
            throw new IllegalStateException("No PROBLEM_CONFIG found in InputData");
        }

        String simulator_executable = problemConfigParameters.get("simulator_executable");
        if (simulator_executable == null || simulator_executable.trim().isEmpty()) {
            throw new IllegalStateException("simulator_executable not configured");
        }

        LinkedList<String> params = new LinkedList<>();
        params.add(simulator_executable);

        for (Map.Entry<String, String> param : this.simpleParameters.entrySet()) {
            String str = getParameterPrefix(param.getKey()) + param.getKey();
            if (!param.getValue().isEmpty()) {
                str += " " + param.getValue();
            }
            params.add(str);
        }

        return params.toArray(new String[0]);
    }


    protected String getParameterPrefix(String parameterName){
        return "-";
    }

    protected void prepareParameters() {
        if (this.currentParameters == null) {
            return;
        }

        for (ProblemParameter<?> param : this.currentParameters) {
            if (param != null && param.getName() != null && param.getValue() != null) {
                addSimpleParameter(param.getName(), param.getValue().toString());
            } else {
                LOGGER.log(Level.WARNING, "Skipping null parameter");
            }
        }
    }

    /**
     * Sets the simulation parameters.
     *
     * @param parameters Array of parameters (cannot be null)
     */
    public void setParameters(ProblemParameter<?>[] parameters) {
        Objects.requireNonNull(parameters, "Parameters cannot be null");
        this.currentParameters = parameters;
        this.prepareParameters();
    }


    /**
     * Runs the simulation (or just prints the command if reallyRun is false).
     *
     * @param reallyRun If true, actually executes the simulator. If false, just logs the command.
     */
    public void run(boolean reallyRun) {
        if (!reallyRun) {
            try {
                String[] commandLine = getCommandLine();
                String executeCommand = String.join(" ", commandLine);
                InputData inputData = individual.getInputData();
                String simulatorName = (String) inputData.get(CommonSetupParameters.NAME);

                LOGGER.log(Level.INFO, String.format(
                    "DRY RUN - Would have started simulator [%s] with command:\n%s\n" +
                    "TO RUN IT: change in Simulator::performSimulation -> this.simulatorRunner.run(true);",
                    simulatorName, executeCommand
                ));
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error during dry run", e);
            }
        } else {
            this.run();
        }
    }

    /**
     * Executes the simulator process and waits for completion.
     * Properly consumes both stdout and stderr to prevent process hanging.
     * Implements timeout to prevent indefinite waiting.
     */
    public void run() {
        String[] commandLineArgs = null;
        String simulatorName = "Unknown";

        try {
            commandLineArgs = this.getCommandLine();
            InputData inputData = individual.getInputData();
            simulatorName = (String) inputData.get(CommonSetupParameters.NAME);

            String executeCommand = String.join(" ", commandLineArgs);
            LOGGER.log(Level.INFO, String.format(
                "Starting simulator [%s] with command:\n%s",
                simulatorName, executeCommand
            ));

            // Execute simulator
            p = Runtime.getRuntime().exec(commandLineArgs);

            // CRITICAL: Consume both stdout and stderr in separate threads
            // to prevent process hanging when buffers fill up
            StreamGobbler outputGobbler = new StreamGobbler(
                p.getInputStream(), "STDOUT", simulatorName, false
            );
            StreamGobbler errorGobbler = new StreamGobbler(
                p.getErrorStream(), "STDERR", simulatorName, true
            );

            outputGobbler.start();
            errorGobbler.start();

            // Wait for the simulation to end (with timeout)
            boolean finished = p.waitFor(PROCESS_TIMEOUT_MINUTES, TimeUnit.MINUTES);

            if (!finished) {
                LOGGER.log(Level.SEVERE, String.format(
                    "Simulator [%s] timed out after %d minutes - killing process",
                    simulatorName, PROCESS_TIMEOUT_MINUTES
                ));
                p.destroyForcibly();
                throw new RuntimeException("Simulation timeout");
            }

            // Wait for stream consumers to finish
            outputGobbler.join(5000);
            errorGobbler.join(5000);

            int exitCode = p.exitValue();
            if (exitCode != 0) {
                LOGGER.log(Level.WARNING, String.format(
                    "Simulator [%s] exited with non-zero status: %d",
                    simulatorName, exitCode
                ));
            } else {
                LOGGER.log(Level.INFO, String.format(
                    "Simulator [%s] completed successfully",
                    simulatorName
                ));
            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, String.format(
                "IOException while running simulator [%s]: %s",
                simulatorName, e.getMessage()
            ), e);
            throw new RuntimeException("Failed to execute simulator", e);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.SEVERE, String.format(
                "Simulator [%s] interrupted",
                simulatorName
            ), e);
            if (p != null) {
                p.destroyForcibly();
            }
            throw new RuntimeException("Simulation interrupted", e);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, String.format(
                "Unexpected error running simulator [%s]",
                simulatorName
            ), e);
            throw new RuntimeException("Simulation failed", e);

        } finally {
            p = null;
        }
    }

    /**
     * Helper thread to consume process output streams.
     * Prevents process from hanging when output buffers fill up.
     */
    private static class StreamGobbler extends Thread {
        private final InputStream inputStream;
        private final String streamType;
        private final String simulatorName;
        private final boolean logOutput;

        public StreamGobbler(InputStream inputStream, String streamType,
                           String simulatorName, boolean logOutput) {
            this.inputStream = inputStream;
            this.streamType = streamType;
            this.simulatorName = simulatorName;
            this.logOutput = logOutput;
            this.setDaemon(true);
            this.setName("StreamGobbler-" + streamType + "-" + simulatorName);
        }

        @Override
        public void run() {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (logOutput) {
                        LOGGER.log(Level.INFO, String.format(
                            "[%s:%s] %s", simulatorName, streamType, line
                        ));
                    }
                    // Always consume the line even if not logging
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, String.format(
                    "Error reading %s from simulator [%s]: %s",
                    streamType, simulatorName, e.getMessage()
                ));
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, "Error closing stream reader", e);
                    }
                }
            }
        }
    }

    /**
     * Forcibly stops the running simulation process.
     * Thread-safe due to volatile process field.
     */
    public void stopRunning() {
        Process process = p;  // Volatile read
        if (process != null) {
            LOGGER.log(Level.WARNING, "Forcibly terminating simulation process");
            process.destroyForcibly();
        }
    }

    public FadseIndividual getIndividual() {
        return individual;
    }

    public void setIndividual(FadseIndividual individual) {
        this.individual = individual;
    }
}