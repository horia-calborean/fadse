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
        RuntimeException retryableFailure = null;
        try {
            LOGGER.log(Level.INFO, String.format(
                "Starting simulation for individual %s (message %s)",
                individual.hashCode(), message.getMessageId()
            ));

            // Perform simulation
            simulator.performSimulation(individual);
        } catch (RuntimeException ex) {
            LOGGER.log(Level.SEVERE, String.format(
                "Simulation failed for individual %s: %s",
                individual.hashCode(),
                ex.getMessage()
            ), ex);
            individual.setBadValuesForObjectives();
            individual.setFeasible(false);
            retryableFailure = ex;
        }

        if (retryableFailure == null) {
            sendResults();
        } else {
            sendRetryableFailure(retryableFailure);
        }
    }

    private void sendResults() {
        try {
            LOGGER.log(Level.INFO, String.format(
                "Sending results for individual %s (feasible=%s)...",
                individual.hashCode(), individual.isFeasible()
            ));
            createResultsSender().send(individual, message);
            LOGGER.log(Level.INFO, String.format(
                "Results sent successfully for individual %s",
                individual.hashCode()
            ));
        } catch (IOException | RuntimeException ex) {
            LOGGER.log(Level.SEVERE, String.format(
                "Could not send results for individual %s: %s",
                individual.hashCode(), ex.getMessage()
            ), ex);
        }
    }

    private void sendRetryableFailure(RuntimeException failure) {
        try {
            LOGGER.log(Level.INFO, String.format(
                    "Reporting retryable failure for individual %s", individual.hashCode()));
            createResultsSender().sendRetryableFailure(
                    individual,
                    message,
                    failure.getClass().getSimpleName() + ": " + String.valueOf(failure.getMessage()));
        } catch (IOException | RuntimeException ex) {
            LOGGER.log(Level.SEVERE, String.format(
                    "Could not report retryable failure for individual %s: %s",
                    individual.hashCode(), ex.getMessage()), ex);
        }
    }

    public FadseIndividual getIndividual() {
        return individual;
    }

    protected ResultsSender createResultsSender() {
        return new ResultsSender();
    }
}
