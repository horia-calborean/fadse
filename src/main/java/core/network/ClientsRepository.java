package core.network;

import core.model.clients.FadseClientData;
import core.model.clients.ListOfFadseClients;
import core.model.individual.FadseIndividual;
import core.model.objectives.Objective;
import input.model.InputData;
import input.model.setup.CommonSetupParameters;
import org.ini4j.Wini;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.util.ConstraintHandling;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Improved version of ClientsRepository with critical bug fixes:
 * - Thread-safe singleton pattern
 * - Null safety checks
 * - Proper exception handling
 * - Fixed concurrent modification issues
 * - Constants extracted
 * - Better logging
 */
public class ClientsRepository {
    // Constants
    private static final Logger LOGGER = Logger.getLogger(ClientsRepository.class.getName());
    private static final int DEFAULT_SLEEP_TIME_MS = 2000;
    private static final int DEFAULT_REDISTRIBUTE_CHECK_TIME_S = 2;
    private static final int MAX_RETRIES = 1;
    private static final int MAX_ITERATIONS = 10000;
    private static final int ATTRIBUTE_ARRAY_SIZE = 100;
    private static final String ATTR_COUNTER = "counter";
    private static final String ATTR_SUM = "sum";
    private static final String ATTR_TEMP_SUM = "tempSum";

    // Singleton instance with volatile for thread safety
    private static volatile ClientsRepository instance;

    protected final ResultsReceiver receiver;
    protected ListOfFadseClients fadseClients;
    protected final SimulationStatus simulationStatus;
    protected final Map<FadseIndividual, Solution<?>> individualsToSend;
    protected final InputData inputData;

    private ClientsRepository(InputData inputData) throws IOException {
        Objects.requireNonNull(inputData, "InputData cannot be null");

        this.inputData = inputData;
        this.fadseClients = ListOfFadseClients.getInstance();
        this.receiver = ResultsReceiver.getInstance();
        this.simulationStatus = SimulationStatus.getInstance();
        this.simulationStatus.setReceiver(receiver);
        // Use ConcurrentHashMap for thread safety
        this.individualsToSend = new ConcurrentHashMap<>();
    }

    /**
     * Thread-safe singleton with double-checked locking
     */
    public static ClientsRepository getInstance(InputData inputData) throws IOException {
        if (instance == null) {
            synchronized (ClientsRepository.class) {
                if (instance == null) {
                    instance = new ClientsRepository(inputData);
                }
            }
        }
        return instance;
    }

    /**
     * Gets the number of configured simulation clients.
     *
     * @return The number of clients, or 0 if no clients are configured
     */
    public int getNumberOfClients() {
        if (fadseClients == null) {
            return 0;
        }
        return fadseClients.getSize();
    }

    public void performSimulation(FadseIndividual individual, Solution<?> currentSolution) {
        Objects.requireNonNull(individual, "Individual cannot be null");
        Objects.requireNonNull(currentSolution, "Solution cannot be null");

        detectAndRescheduleCrashedClients();

        if (fadseClients == null || fadseClients.getSize() == 0) {
            LOGGER.log(Level.SEVERE, "No clients configured");
            return;
        }

        individualsToSend.put(individual, currentSolution);

        // Fix: Create a copy to avoid ConcurrentModificationException
        Map<FadseIndividual, Solution<?>> toProcess = new HashMap<>(individualsToSend);
        individualsToSend.clear();

        int iterationCount = 0;
        for (Map.Entry<FadseIndividual, Solution<?>> entry : toProcess.entrySet()) {
            if (iterationCount++ >= MAX_ITERATIONS) {
                LOGGER.log(Level.SEVERE, "Maximum iterations reached in performSimulation");
                break;
            }

            FadseIndividual ind = entry.getKey();
            Solution<?> sol = entry.getValue();

            performSimulationOnClient(ind, sol);

            if (!ind.isFeasible()) {
                LOGGER.log(Level.SEVERE, "ERROR: FadseIndividual is not feasible (any more)!");
            }
        }
    }

    private void performSimulationOnClient(FadseIndividual ind, Solution<?> solution) {
        Objects.requireNonNull(ind, "Individual cannot be null");
        Objects.requireNonNull(solution, "Solution cannot be null");

        boolean individualSent = false;
        int attemptedClients = 0;
        final int maxAttempts = fadseClients.getSize() * 2; // Try each client twice

        while (!individualSent && attemptedClients < maxAttempts) {
            FadseClientData client = fadseClients.poll();

            if (client == null) {
                LOGGER.log(Level.WARNING, "Null client retrieved from queue");
                attemptedClients++;
                sleepSafely(DEFAULT_SLEEP_TIME_MS);
                continue;
            }

            fadseClients.addLast(client);
            attemptedClients++;

            // Synchronized to prevent race condition on slot count
            // Lock on client object to ensure atomic check-and-increment
            synchronized (client) {
                int availableSlots = client.getNumberOfSlots() - client.getNumberOfOccupiedSlots();

                if (availableSlots > 0) {
                    // Reserve slot BEFORE attempting to send
                    client.setNumberOfOccupiedSlots(client.getNumberOfOccupiedSlots() + 1);

                    try {
                        individualSent = trySendIndividual(ind, solution, client);

                        if (!individualSent) {
                            // Failed to send - release the reserved slot
                            client.setNumberOfOccupiedSlots(client.getNumberOfOccupiedSlots() - 1);
                            handleFailedSend(client);
                        }
                    } catch (Exception e) {
                        // Exception during send - release the reserved slot
                        client.setNumberOfOccupiedSlots(client.getNumberOfOccupiedSlots() - 1);
                        LOGGER.log(Level.SEVERE, "Exception sending individual to client", e);
                    }
                } else {
                    // All slots full on this client
                    LOGGER.log(Level.FINE, String.format(
                        "Client %s:%d has no available slots (%d/%d occupied)",
                        client.getIP().getHostAddress(), client.getPort(),
                        client.getNumberOfOccupiedSlots(), client.getNumberOfSlots()
                    ));
                }
            }

            // If all clients are full, wait before next iteration
            if (!individualSent && attemptedClients % fadseClients.getSize() == 0) {
                sleepSafely(DEFAULT_SLEEP_TIME_MS);
            }
        }

        // If not sent after all attempts, re-add to queue
        if (!individualSent) {
            LOGGER.log(Level.WARNING, String.format(
                "Failed to send individual after %d attempts, re-queueing", attemptedClients
            ));
            individualsToSend.put(ind, solution);
        }
    }

    private boolean trySendIndividual(FadseIndividual ind, Solution<?> solution, FadseClientData client) {
        try {
            Message m = MessageSender.sendIndividual(ind, client);
            simulationStatus.addSimulation(m, client, solution);
            // Note: slot count already incremented by caller before this method
            LOGGER.log(Level.INFO, String.format(
                "Server sent individual %d to client %s:%d (slots: %d/%d)",
                ind.hashCode(), client.getIP().getHostAddress(), client.getPort(),
                client.getNumberOfOccupiedSlots(), client.getNumberOfSlots()
            ));
            return true;

        } catch (java.net.UnknownHostException ex) {
            LOGGER.log(Level.SEVERE, String.format(
                "Unknown host: %s", client.getIP()), ex);
        } catch (java.net.SocketTimeoutException ex) {
            LOGGER.log(Level.SEVERE, String.format(
                "Timeout sending to %s:%d (client not responding)",
                client.getIP().getHostAddress(), client.getPort()), ex);
        } catch (java.net.ConnectException ex) {
            LOGGER.log(Level.SEVERE, String.format(
                "Cannot connect to %s:%d (client may be down)",
                client.getIP().getHostAddress(), client.getPort()), ex);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, String.format(
                "I/O error communicating with %s:%d: %s",
                client.getIP().getHostAddress(), client.getPort(), ex.getMessage()), ex);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Unexpected exception sending individual", ex);
        }

        return false;
    }

    private void handleFailedSend(FadseClientData client) {
        if (simulationStatus.isClientSimulating(client)) {
            // Retrieve individuals from failed client and re-queue them
            Map<FadseIndividual, DoubleSolution> indOnClient =
                    simulationStatus.getIndividualsSimulatingOnClient(client);
            simulationStatus.removeSimulationsOnClient(client);

            if (indOnClient != null) {
                individualsToSend.putAll(indOnClient);
                LOGGER.log(Level.INFO,
                        "Re-queued " + indOnClient.size() + " individuals from failed client: " + client);
            }
        }
    }

    public void join() {
        LOGGER.log(Level.INFO, "Join method called");

        simulationStatus.removeRemainingSimulations();
        redistributeUnfinishedSimulations();

        LOGGER.log(Level.INFO, "All simulations complete, processing results");

        List<Message> receivedMessages = receiver.getResults();
        List<Message> cleanMessages = processReceivedMessages(receivedMessages);

        LOGGER.log(Level.INFO, "Clean messages size: " + cleanMessages.size());

        aggregateObjectiveValues(cleanMessages);
        computeAverages(cleanMessages);

        // Cleanup
        simulationStatus.clearPerGenerationData();
        individualsToSend.clear();
        receiver.clearResults();

        // Refresh clients
        Object clientsObj = inputData.get(CommonSetupParameters.FADSE_CLIENTS);
        if (clientsObj instanceof ListOfFadseClients) {
            fadseClients = (ListOfFadseClients) clientsObj;
            LOGGER.log(Level.CONFIG, "Loaded " + fadseClients.getSize() + " clients");
        } else {
            LOGGER.log(Level.SEVERE, "Failed to refresh clients list");
        }

        LOGGER.log(Level.INFO, "Join method finished");
    }

    private List<Message> processReceivedMessages(List<Message> receivedMessages) {
        // Use HashSet for O(1) lookup instead of LinkedList
        Set<FadseIndividual> duplicateDetector = new HashSet<>();
        List<Message> cleanMessages = new ArrayList<>();

        for (Message receivedMessage : receivedMessages) {
            if (receivedMessage == null) continue;

            for (Message localKeptMessage : simulationStatus.getSentMessages()) {
                if (localKeptMessage == null ||
                        !receivedMessage.getMessageId().equals(localKeptMessage.getMessageId())) {
                    continue;
                }

                boolean shouldCopy = shouldCopyResults(
                        localKeptMessage,
                        receivedMessage,
                        duplicateDetector
                );

                if (shouldCopy) {
                    copyResults(localKeptMessage, receivedMessage);

                    if (!duplicateDetector.contains(localKeptMessage.getIndividual())) {
                        cleanMessages.add(localKeptMessage);
                        duplicateDetector.add(localKeptMessage.getIndividual());
                    }
                }
            }
        }

        return cleanMessages;
    }

    private boolean shouldCopyResults(Message localKeptMessage, Message receivedMessage,
                                      Set<FadseIndividual> duplicateDetector) {
        FadseIndividual localInd = localKeptMessage.getIndividual();

        if (!duplicateDetector.contains(localInd)) {
            return true;
        }

        // Already have results, check if new ones are better
        FadseIndividual rec = receivedMessage.getIndividual();
        int objectivesSize = getObjectivesSize();

        if (rec.isFeasible() && rec.getObjectives().size() == objectivesSize) {
            // Check if all objectives have valid values
            for (Objective obj : rec.getObjectives()) {
                if (obj.getValue() == 0) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    private void copyResults(Message localKeptMessage, Message receivedMessage) {
        FadseIndividual localInd = localKeptMessage.getIndividual();
        FadseIndividual receivedInd = receivedMessage.getIndividual();

        LinkedList<Objective> objectives = new LinkedList<>(receivedInd.getObjectives());
        localInd.setObjectives(objectives);
        localInd.setFeasible(receivedInd.isFeasible());
    }

    private void aggregateObjectiveValues(List<Message> cleanMessages) {
        int objectivesSize = getObjectivesSize();

        for (Message localKeptMessage : cleanMessages) {
            FadseIndividual individual = localKeptMessage.getIndividual();
            List<Objective> objectives = individual.getObjectives();

            // Validate objectives
            if (objectives == null || objectives.size() != objectivesSize) {
                individual.setBadValuesForObjectives();
                LOGGER.log(Level.SEVERE, "Individual has incorrect number of objectives");
                continue;
            }

            boolean infeasible = false;
            Solution<?> solution = simulationStatus.getSolution(localKeptMessage.getMessageId());

            if (solution == null) {
                LOGGER.log(Level.SEVERE, "Solution not found for message: " + localKeptMessage.getMessageId());
                continue;
            }

            for (int i = 0; i < objectives.size(); i++) {
                Objective objective = objectives.get(i);

                if (objective.getValue() == 0) {
                    LOGGER.log(Level.SEVERE, "Objective value is 0, marking as infeasible");
                    individual.setBadValuesForObjectives();
                    objective.setValue(Double.MAX_VALUE);
                    infeasible = true;
                }

                double value = solution.objectives()[i];
                value += objective.getValue();
                solution.objectives()[i] = value;

                // Update tracking attributes
                updateSolutionAttributes(solution, i, objective.getValue());
            }

            if (infeasible || !individual.isFeasible()) {
                ConstraintHandling.numberOfViolatedConstraints(solution, Integer.MAX_VALUE);
                ConstraintHandling.overallConstraintViolationDegree(solution, Integer.MAX_VALUE);
            }
        }
    }

    private void updateSolutionAttributes(Solution<?> solution, int objectiveIndex, double value) {
        // Initialize counter
        if (!solution.attributes().containsKey(ATTR_COUNTER)) {
            solution.attributes().put(ATTR_COUNTER, 0);
        }
        int counter = (int) solution.attributes().get(ATTR_COUNTER);
        solution.attributes().put(ATTR_COUNTER, counter + 1);

        // Initialize sum array
        if (!solution.attributes().containsKey(ATTR_SUM)) {
            solution.attributes().put(ATTR_SUM, new String[ATTRIBUTE_ARRAY_SIZE]);
        }
        String[] sum = (String[]) solution.attributes().get(ATTR_SUM);
        sum[objectiveIndex] = (sum[objectiveIndex] == null || sum[objectiveIndex].isEmpty())
                ? String.valueOf(value)
                : sum[objectiveIndex] + " + " + value;

        // Initialize temp sum array
        if (!solution.attributes().containsKey(ATTR_TEMP_SUM)) {
            solution.attributes().put(ATTR_TEMP_SUM, new double[ATTRIBUTE_ARRAY_SIZE]);
        }
        double[] tempSum = (double[]) solution.attributes().get(ATTR_TEMP_SUM);
        tempSum[objectiveIndex] += value;
    }

    private void computeAverages(List<Message> cleanMessages) {
        Set<Solution<?>> solutions = new HashSet<>();
        int objectivesSize = getObjectivesSize();
        int benchmarkCount = getBenchmarkCount();

        // Collect unique solutions and validate
        for (Message message : cleanMessages) {
            FadseIndividual individual = message.getIndividual();

            // Validate individual
            if (individual.getObjectives().size() != objectivesSize) {
                individual.setBadValuesForObjectives();
                LOGGER.log(Level.SEVERE, "Individual missing objectives");
                continue;
            }

            for (Objective obj : individual.getObjectives()) {
                if (obj.getValue() == 0) {
                    LOGGER.log(Level.SEVERE, "Objective value is 0, marking as infeasible");
                    individual.setBadValuesForObjectives();
                    break;
                }
            }

            Solution<?> solution = simulationStatus.getSolution(message.getMessageId());
            if (solution == null) continue;

            // Validate counter
            Object counterObj = solution.attributes().get(ATTR_COUNTER);
            if (counterObj == null) {
                LOGGER.log(Level.SEVERE, "Solution missing counter attribute");
                continue;
            }

            int counter = (int) counterObj;
            int expectedCount = benchmarkCount * objectivesSize;

            if (counter != expectedCount) {
                LOGGER.log(Level.SEVERE,
                        String.format("Incomplete results: %d != %d", counter, expectedCount));
                ConstraintHandling.numberOfViolatedConstraints(solution, Integer.MAX_VALUE);
                ConstraintHandling.overallConstraintViolationDegree(solution, Integer.MAX_VALUE);
            }

            solutions.add(solution);
        }

        // Calculate averages and clean up
        for (Solution<?> solution : solutions) {
            for (int i = 0; i < solution.objectives().length; i++) {
                double objectiveSum = solution.objectives()[i];
                double objectiveAverage = objectiveSum / benchmarkCount;
                solution.objectives()[i] = objectiveAverage;

                // Clean up attributes
                cleanSolutionAttributes(solution, i);
            }
        }
    }

    private void cleanSolutionAttributes(Solution<?> solution, int index) {
        // Clean sum array
        if (solution.attributes().containsKey(ATTR_SUM)) {
            String[] sum = (String[]) solution.attributes().get(ATTR_SUM);
            sum[index] = null;
        }

        // Clean temp sum array
        if (solution.attributes().containsKey(ATTR_TEMP_SUM)) {
            double[] tempSum = (double[]) solution.attributes().get(ATTR_TEMP_SUM);
            tempSum[index] = 0.0;
        }

        // Reset counter
        solution.attributes().put(ATTR_COUNTER, 0);
    }

    private void redistributeUnfinishedSimulations() {
        LOGGER.log(Level.INFO, "Redistributing unfinished simulations");

        long startTime = System.currentTimeMillis();
        int maxTimeMinutes = getMaxSimulationTime();
        long maxWaitTimeMs = TimeUnit.MINUTES.toMillis(maxTimeMinutes * 2L);

        while (simulationStatus.getNumberOfActiveSimulations() > 0) {
            if (System.currentTimeMillis() - startTime > maxWaitTimeMs) {
                LOGGER.log(Level.SEVERE, "Maximum wait time exceeded, marking remaining as infeasible");
                markRemainingAsInfeasible();
                break;
            }

            LOGGER.log(Level.INFO,
                    "Waiting for " + simulationStatus.getActiveSimulations() + " results");

            sleepSafely(getRedistributeCheckTime());
            detectAndRescheduleCrashedClients();
        }

        LOGGER.log(Level.INFO, "Redistribution complete");
    }

    private void markRemainingAsInfeasible() {
        for (String messageId : simulationStatus.getActiveSimulationsIds()) {
            Simulation simulation = simulationStatus.getSimulation(messageId);
            if (simulation != null) {
                simulation.getMessage().getIndividual().setBadValuesForObjectives();
                simulation.setActive(false);
            }
        }
    }

    private void detectAndRescheduleCrashedClients() {
        int maxTimeMinutes = getMaxSimulationTime();
        long maxTimeMs = TimeUnit.MINUTES.toMillis(maxTimeMinutes);

        for (String messageId : simulationStatus.getActiveSimulationsIds()) {
            Simulation simulation = simulationStatus.getSimulation(messageId);

            if (simulation == null) continue;

            long elapsedTime = System.currentTimeMillis() - simulation.getSimulationStartedTime().getTime();

            if (elapsedTime > maxTimeMs) {
                handleTimedOutSimulation(simulation);
            }
        }
    }

    private void handleTimedOutSimulation(Simulation simulation) {
        LOGGER.log(Level.INFO, "Simulation timed out, retries: " + simulation.getRetries());

        if (simulation.getRetries() > MAX_RETRIES) {
            // Max retries exceeded, mark as infeasible
            simulation.getMessage().getIndividual().setBadValuesForObjectives();
            simulation.setActive(false);
            LOGGER.log(Level.INFO, "Individual marked as infeasible after max retries");
        } else {
            // Retry on different client
            simulation.increaseRetries();
            simulation.getMessage().getIndividual().setBadValuesForObjectives();
            simulation.getMessage().getIndividual().setFeasible(true);
            simulation.setSimulationStartedTime(new Timestamp(System.currentTimeMillis()));
            simulationStatus.removeSimulationsOnClient(simulation.getClientData());
            performSimulationOnClient(simulation.getMessage().getIndividual(), simulation.getSolution());
            LOGGER.log(Level.INFO, "Retrying simulation on different client");
        }

        // Send kill message to timed-out client
        sendKillMessage(simulation);
    }

    private void sendKillMessage(Simulation simulation) {
        try {
            MessageSender.sendIndividual(
                    simulation.getMessage().getIndividual(),
                    simulation.getClientData(),
                    Message.TYPE_CLOSE_SIMULATION_REQUEST
            );
        } catch (UnknownHostException ex) {
            LOGGER.log(Level.WARNING, "Unknown host when sending kill message", ex);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "I/O error sending kill message", ex);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Unexpected error sending kill message", ex);
        }
    }

    // Helper methods with proper error handling

    private int getObjectivesSize() {
        try {
            Object objectivesObj = inputData.get(CommonSetupParameters.OBJECTIVES);
            if (objectivesObj instanceof Map) {
                return ((Map<?, ?>) objectivesObj).size();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to get objectives size", e);
        }
        return 0;
    }

    private int getBenchmarkCount() {
        try {
            Object benchmarksObj = inputData.get(CommonSetupParameters.BENCHMARKS);
            if (benchmarksObj instanceof List) {
                return ((List<?>) benchmarksObj).size();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to get benchmark count", e);
        }
        return 1; // Default to 1 to avoid division by zero
    }

    private int getMaxSimulationTime() {
        try {
            Object configObj = inputData.get(CommonSetupParameters.PROBLEM_CONFIG);
            if (configObj instanceof Map) {
                Map<?, ?> config = (Map<?, ?>) configObj;
                String maxTimeStr = (String) config.get("maximumTimeOfASimulation");
                if (maxTimeStr != null) {
                    return Integer.parseInt(maxTimeStr);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to get max simulation time, using default", e);
        }
        return 10; // Default 10 minutes
    }

    private long getRedistributeCheckTime() {
        try {
            String currentDir = System.getProperty("user.dir");
            File configFile = new File(currentDir, "configs" + File.separator + "fadseConfig.ini");

            if (configFile.exists()) {
                Wini ini = new Wini(configFile);
                Integer time = ini.get("RedistributeCheck", "timeSeconds", Integer.class);
                if (time != null) {
                    return TimeUnit.SECONDS.toMillis(time);
                }
            }
        } catch (IOException ex) {
            LOGGER.log(Level.FINE, "Could not read redistribute time from config, using default", ex);
        }
        return TimeUnit.SECONDS.toMillis(DEFAULT_REDISTRIBUTE_CHECK_TIME_S);
    }

    private void sleepSafely(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException ex) {
            // Restore interrupt status
            Thread.currentThread().interrupt();
            LOGGER.log(Level.WARNING, "Thread interrupted during sleep", ex);
        }
    }

    public void closeSimulation(FadseIndividual individual) {
        throw new UnsupportedOperationException("closeSimulation not yet implemented");
    }

    public void dumpCurrentPopulation(List<DoubleSolution> population) {
        dumpCurrentPopulation("filled_" + System.currentTimeMillis(), population);
    }

    public void dumpCurrentPopulation(String filename, List<DoubleSolution> population) {
        LOGGER.log(Level.INFO, "dumpCurrentPopulation called with filename: " + filename);
        // TODO: Implement when CSV utilities are available
    }
}