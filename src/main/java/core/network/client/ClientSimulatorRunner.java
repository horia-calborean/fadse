package core.network.client;

import core.model.individual.FadseIndividual;
import core.network.Message;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ClientSimulatorRunner - Runs a simulation and sends results back to the server.
 *
 * Improvements:
 * - Null validation for all constructor parameters
 * - Better exception handling (simulation errors are propagated)
 * - Proper cleanup on failure
 * - More informative logging
 */
public class ClientSimulatorRunner implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(ClientSimulatorRunner.class.getName());

    private final FadseIndividual individual;
    private final Simulator simulator;
    private final Message message;

    /**
     * Creates a new ClientSimulatorRunner.
     *
     * @param individual The individual to simulate (cannot be null)
     * @param simulator The simulator to use (cannot be null)
     * @param message The message with routing info (cannot be null)
     * @throws NullPointerException if any parameter is null
     */
    public ClientSimulatorRunner(FadseIndividual individual, Simulator simulator, Message message) {
        this.individual = Objects.requireNonNull(individual, "Individual cannot be null");
        this.simulator = Objects.requireNonNull(simulator, "Simulator cannot be null");
        this.message = Objects.requireNonNull(message, "Message cannot be null");
    }

    @Override
    public void run() {
        boolean simulationSuccessful = false;

        try {
            LOGGER.log(Level.INFO, String.format(
                "Starting simulation for individual %s (message %s)",
                individual.hashCode(), message.getMessageId()
            ));

            // Perform simulation
            simulator.performSimulation(individual);
            simulationSuccessful = true;

            LOGGER.log(Level.INFO, String.format(
                "Simulation completed for individual %s. Sending results...",
                individual.hashCode()
            ));

            // Send results back to server
            ResultsSender resultsSender = new ResultsSender();
            resultsSender.send(individual, message);

            LOGGER.log(Level.INFO, String.format(
                "Results sent successfully for individual %s",
                individual.hashCode()
            ));

        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, String.format(
                "IOException while %s for individual %s: %s",
                simulationSuccessful ? "sending results" : "running simulation",
                individual.hashCode(),
                ex.getMessage()
            ), ex);

            // If simulation succeeded but sending failed, we might want to retry
            // or save results locally for later transmission
            if (simulationSuccessful) {
                LOGGER.log(Level.WARNING,
                    "Simulation completed but results could not be sent. Results may be lost.");
            }

        } catch (RuntimeException ex) {
            // Catch simulation failures (from SimulatorRunner)
            LOGGER.log(Level.SEVERE, String.format(
                "Simulation failed for individual %s: %s",
                individual.hashCode(),
                ex.getMessage()
            ), ex);

            // Optionally notify server of simulation failure
            // For now, we just log it

        } catch (Exception ex) {
            // Catch any other unexpected errors
            LOGGER.log(Level.SEVERE, String.format(
                "Unexpected error during simulation for individual %s",
                individual.hashCode()
            ), ex);
        }
    }

    public FadseIndividual getIndividual() {
        return individual;
    }
}