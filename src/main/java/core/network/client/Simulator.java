package core.network.client;

import core.model.individual.FadseIndividual;
import core.model.objectives.Objective;
import input.model.InputData;
import input.model.setup.CommonSetupParameters;

import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simulator - Base class for external simulator integration.
 *
 * Improvements:
 * - Null validation for all critical parameters
 * - Better exception handling in performSimulation
 * - Proper logging instead of System.out
 * - Cleanup method for resource management
 */
public abstract class Simulator {
    private static final Logger LOGGER = Logger.getLogger(Simulator.class.getName());

    protected String simulatorOutputFile;
    protected Map<String, Double> simpleObjectives;
    protected LinkedList<Objective> currentObjectives;
    protected SimulatorRunner simulatorRunner;
    protected SimulatorOutputParser simulatorOutputParser;
    protected InputData inputData;

    /**
     * Creates a new Simulator.
     *
     * @param inputData Configuration data (cannot be null)
     * @throws NullPointerException if inputData is null
     */
    public Simulator(InputData inputData) {
        this.inputData = Objects.requireNonNull(inputData, "InputData cannot be null");
        this.initSimulator();
    }

    /**
     * Initializes the simulator components.
     */
    public void initSimulator() {
        simulatorOutputFile = "";
        simulatorRunner = new SimulatorRunner(this);
        simulatorOutputParser = new SimulatorOutputParser(this);
    }

    public SimulatorRunner getRunner() {
        return this.simulatorRunner;
    }

    public SimulatorOutputParser getOutputParser() {
        return this.simulatorOutputParser;
    }

    public String getSimulatorOutputFile() {
        return this.simulatorOutputFile;
    }

    public void setSimulatorOutputFile(String value) {
        this.simulatorOutputFile = value;
    }

    /**
     * Performs a simulation for the given individual.
     * This method orchestrates the entire simulation process:
     * 1. Set parameters
     * 2. Run simulator
     * 3. Parse results
     * 4. Update individual with objectives
     *
     * @param individual The individual to simulate (cannot be null)
     * @throws NullPointerException if individual is null
     * @throws RuntimeException if simulation fails
     */
    public void performSimulation(FadseIndividual individual) {
        Objects.requireNonNull(individual, "Individual cannot be null");

        LOGGER.log(Level.INFO, "========== PERFORM SIMULATION ==========");

        try {
            // Set simulation parameters
            if (individual.getParameters() == null) {
                throw new IllegalStateException("Individual has no parameters");
            }
            simulatorRunner.setParameters(individual.getParameters());

            // Get and validate objectives
            @SuppressWarnings("unchecked")
            Map<String, Objective> objectivesMap = (Map<String, Objective>) inputData.get(CommonSetupParameters.OBJECTIVES);
            if (objectivesMap == null || objectivesMap.isEmpty()) {
                throw new IllegalStateException("No objectives configured");
            }

            simulatorOutputParser.setObjectives(objectivesMap);

            // Run simulation
            LOGGER.log(Level.INFO, "Starting simulation execution...");
            simulatorRunner.setIndividual(individual);
            this.simulatorRunner.run(true);

            // Parse and set results
            LinkedList<Objective> results = this.simulatorOutputParser.getResults(individual);
            if (results == null || results.isEmpty()) {
                LOGGER.log(Level.WARNING, "No results obtained from simulation");
                individual.setBadValuesForObjectives();
            } else {
                individual.setObjectives(results);
                LOGGER.log(Level.INFO, "Simulation completed successfully");
            }

        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Simulation failed for individual", e);
            individual.setBadValuesForObjectives();
            throw e;  // Propagate to caller
        }
    }

    /**
     * Closes/terminates a running simulation.
     *
     * @param individual The individual being simulated
     */
    public void closeSimulation(FadseIndividual individual) {
        LOGGER.log(Level.INFO, "Closing simulation for individual");
        simulatorRunner.stopRunning();
    }

    public InputData getInputData() {
        return inputData;
    }

    @Override
    public String toString() {
        String name = (String) inputData.get(CommonSetupParameters.NAME);
        return "Simulator[" + (name != null ? name : "unknown") + "]";
    }
}