package core.network;

import core.model.clients.FadseClientData;
import core.model.individual.FadseIndividual;
import input.model.InputData;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.solution.Solution;

import java.util.*;

public class SimulationStatus {

    static class theLock {
    }

    ResultsReceiver receiver;
    static private final theLock lockObject = new theLock();
    private static SimulationStatus instance;
    private final Map<String, Simulation> simulations;
    private final List<String> toRemove;
    private Algorithm<?> algorithm;//might be or might not be set
    private InputData inputData;//might be or might not be set

    private SimulationStatus() {
        toRemove = Collections.synchronizedList(new LinkedList<>());
        simulations = Collections.synchronizedMap(new HashMap<>());
        Thread t = new Thread(new StatusObserver<>(this));
        t.setDaemon(true);
        t.start();
    }

    public synchronized static SimulationStatus getInstance() {
        if (instance == null) {
            instance = new SimulationStatus();
        }
        return instance;
    }

    public <T> void addSimulation(Message m, FadseClientData n, Solution<T> s) {
        synchronized (lockObject) {
            simulations.put(m.getMessageId(), new Simulation(m.getMessageId(), m, s, n));
        }
    }

    public void removeSimulation(String messageId) {
        synchronized (lockObject) {
            removeRemainingSimulations();
            if (simulations.get(messageId) != null) {//try removing and also see if the object was there
                simulations.get(messageId).setActive(false);
            } else {
                toRemove.add(messageId);
            }
        }
    }


    public void removeRemainingSimulations() {
        synchronized (lockObject) {
            for (String toR : toRemove) {
                for (Simulation s : simulations.values()) {
                    if (s.getId().equals(toR)) {
                        s.setActive(false);
                    }
                }
            }
        }
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