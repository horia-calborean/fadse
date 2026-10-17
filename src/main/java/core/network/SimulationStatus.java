package core.network;

import core.model.clients.FadseClientData;
import core.model.individual.FadseIndividual;
import input.model.InputData;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.solution.Solution;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimulationStatus {

    private static final Logger LOGGER = Logger.getLogger(SimulationStatus.class.getName());
    private static final Object LOCK = new Object();

    // Warn about unusually large generations without discarding result correlations.
    private static final int MAX_SIMULATION_HISTORY =
        Integer.getInteger("fadse.simulation.max.history", 10000);
    private static final int HISTORY_WARNING_THRESHOLD =
        (int)(MAX_SIMULATION_HISTORY * 0.9);

    ResultsReceiver receiver;
    private static SimulationStatus instance;
    private final Map<String, Simulation> simulations;
    private final Set<String> toRemove;
    private Algorithm<?> algorithm;//might be or might not be set
    private InputData inputData;//might be or might not be set

    // Track statistics
    private final AtomicLong totalSimulationsAdded = new AtomicLong(0);
    private final AtomicLong totalSimulationsRemoved = new AtomicLong(0);

    private SimulationStatus() {
        // FIXED: Use ConcurrentHashMap instead of synchronized HashMap to prevent ConcurrentModificationException
        toRemove = ConcurrentHashMap.newKeySet();
        simulations = new ConcurrentHashMap<>();
        // FIXED: Don't start thread in constructor to avoid race condition
    }

    /**
     * Start the status observer thread. Should be called after initialization.
     */
    public void startStatusObserver() {
        Thread t = new Thread(new StatusObserver<>(this));
        t.setDaemon(true);
        t.start();
    }

    public static SimulationStatus getInstance() {
        // FIXED: Thread-safe double-checked locking
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new SimulationStatus();
                }
            }
        }
        return instance;
    }

    public <T> void addSimulation(Message m, FadseClientData n, Solution<T> s) {
        // FIXED: ConcurrentHashMap is thread-safe, no need for explicit synchronization
        simulations.put(m.getMessageId(), new Simulation(m.getMessageId(), m, s, n));

        totalSimulationsAdded.incrementAndGet();

        // Inactive entries are still needed to connect received results to the
        // authoritative server-side individual and solution. The whole map is
        // released by clearPerGenerationData() after join() aggregates results.
        if (simulations.size() == HISTORY_WARNING_THRESHOLD + 1) {
            LOGGER.log(Level.WARNING, String.format(
                "Current generation has %d simulations; retaining result correlations until join",
                simulations.size()
            ));
        }
    }

    public void removeSimulation(String messageId) {
        // FIXED: Process pending removals safely with ConcurrentHashMap
        processPendingRemovals();

        Simulation sim = simulations.get(messageId);
        if (sim != null) {
            sim.setActive(false);
            totalSimulationsRemoved.incrementAndGet();
        } else {
            toRemove.add(messageId);
        }
    }

    /**
     * Gets statistics about simulation tracking
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total_added", totalSimulationsAdded.get());
        stats.put("total_removed", totalSimulationsRemoved.get());
        stats.put("current_size", simulations.size());
        stats.put("active_count", getNumberOfActiveSimulations());
        stats.put("pending_removals", toRemove.size());
        stats.put("max_history", MAX_SIMULATION_HISTORY);
        return stats;
    }

    public void processPendingRemovals() {
        // FIXED: Safe iteration with ConcurrentHashMap
        if (toRemove.isEmpty()) {
            return;
        }

        for (String id : toRemove) {
            Simulation sim = simulations.get(id);
            if (sim != null) {
                sim.setActive(false);
            }
        }
        toRemove.clear();
    }

    public void removeRemainingSimulations() {
        processPendingRemovals();
    }

    public int getNumberOfActiveSimulations() {
        int counter = 0;
        for (Simulation s : simulations.values()) {
            if (s.isActive()) {
                counter++;
            }
        }
        return counter;
    }

    public List<String> getActiveSimulations() {
        List<String> activeSimulations = new LinkedList<>();
        for (Simulation s : simulations.values()) {
            if (s.isActive()) {
                activeSimulations.add(s.getClientData().getIP()+":"+s.getClientData().getPort()+"-"+s.getId());
            }
        }
        return activeSimulations;
    }
    public List<String> getActiveSimulationsIds() {
        List<String> activeSimulations = new LinkedList<>();
        for (Simulation s : simulations.values()) {
            if (s.isActive()) {
                activeSimulations.add(s.getId());
            }
        }
        return activeSimulations;
    }

    public List<Message> getSentMessages() {
        List<Message> messages = new LinkedList<>();
        for (Simulation s : simulations.values()) {
            messages.add(s.getMessage());
        }
        return messages;
    }

    public <T extends Solution<?>> T getSolution(String id) {
        return simulations.get(id).getSolution();
    }

    public void clearPerGenerationData() {//TODO think it over
        toRemove.clear();
        simulations.clear();
    }

    public boolean isClientSimulating(FadseClientData n) {
        for (Simulation s : simulations.values()) {
            if (s.isActive()) {
                FadseClientData client = s.getClientData();
                if (client.getIP().getHostAddress().equalsIgnoreCase(n.getIP().getHostAddress()) && client.getPort()==n.getPort()) {
                    return true;
                }
            }
        }
        return false;
    }

    public <T, S extends Solution<T>> Map<FadseIndividual,S> getIndividualsSimulatingOnClient(FadseClientData n) {
        Map<FadseIndividual, S> individualsOnClient = new HashMap<>();
        for (Simulation s : simulations.values()) {
            if (s.isActive()) {
                FadseClientData client = s.getClientData();
                if (client.getIP().getHostAddress().equalsIgnoreCase(n.getIP().getHostAddress())&& client.getPort()==n.getPort()) {
                    individualsOnClient.put(s.getMessage().getIndividual(),s.getSolution());
                }
            }
        }
        return individualsOnClient;
    }

    public void removeSimulationsOnClient(FadseClientData n) {
        for (Simulation s : simulations.values()) {
            if (s.isActive()) {
                FadseClientData client = s.getClientData();
                if (client.getIP().getHostAddress().equalsIgnoreCase(n.getIP().getHostAddress())&& client.getPort()==n.getPort()) {
                    removeSimulation(s.getId());
                }
            }
        }
    }

    public Simulation getSimulation(String messageId) {
        return simulations.get(messageId);
    }

    public Map<String, Simulation> getSimulations() {
        return simulations;
    }

    public Algorithm<?> getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(Algorithm<?> algorithm) {
        this.algorithm = algorithm;
    }

    public InputData getInputData() {
        return inputData;
    }

    public void setInputData(InputData inputData) {
        this.inputData = inputData;
    }

    public ResultsReceiver getReceiver() {
        return receiver;
    }

    public void setReceiver(ResultsReceiver receiver) {
        this.receiver = receiver;
    }
}
