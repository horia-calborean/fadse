/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ulbsibiu.fadse.extended.problems.simulators.network.server.status;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.environment.Individual;
import ro.ulbsibiu.fadse.extended.problems.simulators.network.Message;
import ro.ulbsibiu.fadse.extended.problems.simulators.network.server.Neighbor;
import ro.ulbsibiu.fadse.extended.problems.simulators.network.server.ResultsReceiver;
import ro.ulbsibiu.fadse.utils.Utils;
import jmetal.base.Algorithm;
import jmetal.base.Solution;

/**
 *
 * @author Horia Ioan
 */
public class SimulationStatus {

    static class theLock extends Object {
    }
    ResultsReceiver receiver;
    static private final theLock lockObject = new theLock();
    private static SimulationStatus instance;
    private Map<String, Simulation> simulations;
    private List<String> toRemove;
    private Algorithm algorithm;//might be or might not be set
    private Environment environment;//might be or might not be set

    private SimulationStatus() {
        toRemove = Collections.synchronizedList(new LinkedList<String>());
        simulations = Collections.synchronizedMap(new HashMap<String, Simulation>());
        Thread t = new Thread(new StatusObserver(this));
        t.setDaemon(true);
        t.start();
//        Thread t2 = new Thread(new PopulationDumper(this));
//        t2.start();
    }

    public synchronized static SimulationStatus getInstance() {
        if (instance == null) {
            instance = new SimulationStatus();
        }
        return instance;
    }

    public void addSimulation(Message m, Neighbor n, Solution s) {
        synchronized (lockObject) {
//            Logger.getLogger(SimulationStatus.class.getName()).log(Level.INFO, "SimulationStatus: added - " + m.getMessageId());
            simulations.put(m.getMessageId(), new Simulation(m.getMessageId(), m, s, n));
        }
    }

    public void removeSimulation(String messageId) {
        synchronized (lockObject) {
            removeRemainingSimulations();
            if (simulations.get(messageId) != null) {//try removing and also see if the object was there
                simulations.get(messageId).setActive(false);
//                Logger.getLogger(SimulationStatus.class.getName()).log(Level.INFO, "SimulationStatus: removed - " + messageId);
            } else {
                toRemove.add(messageId);
                //the remove came before the add
            }
        }
    }

    /**
     * removes the simulations that ended but are still in the queue marked as active
     */
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
        List<String> activeSimualtions = new LinkedList<String>();
        for (Simulation s : simulations.values()) {
            if (s.isActive()) {
                activeSimualtions.add(s.getNeighbor().getIp()+":"+s.getNeighbor().getPort()+"-"+s.getId());
            }
        }
        return activeSimualtions;
    }
    public List<String> getActiveSimulationsIds() {
        List<String> activeSimualtions = new LinkedList<String>();
        for (Simulation s : simulations.values()) {
            if (s.isActive()) {
                activeSimualtions.add(s.getId());
            }
        }
        return activeSimualtions;
    }

    public List<Message> getSentMessages() {
        List<Message> messages = new LinkedList<Message>();
        for (Simulation s : simulations.values()) {
            messages.add(s.getMessage());
        }
        return messages;
    }

    public Solution getSolution(String id) {
        return simulations.get(id).getSolution();
    }

    public void clearPerGenerationData() {//TODO think it over
        toRemove.clear();
        simulations.clear();
    }

    public boolean isClientSimulating(Neighbor n) {
        for (Simulation s : simulations.values()) {
            if (s.isActive()) {
                Neighbor client = s.getNeighbor();
                if (client.getIp().getHostAddress().equalsIgnoreCase(n.getIp().getHostAddress()) && client.getPort()==n.getPort()) {
                    return true;
                }
            }
        }
        return false;
    }

    public Map<Individual,Solution> getIndividualsSimulatingOnClient(Neighbor n) {
        Map<Individual, Solution> individualsOnClient = new HashMap< Individual, Solution>();
        for (Simulation s : simulations.values()) {
            if (s.isActive()) {
                Neighbor client = s.getNeighbor();
                if (client.getIp().getHostAddress().equalsIgnoreCase(n.getIp().getHostAddress())&& client.getPort()==n.getPort()) {
                    individualsOnClient.put(s.getMessage().getIndividual(),s.getSolution());
                }
            }
        }
        return individualsOnClient;
    }

    public void removeSimulationsOnClient(Neighbor n) {
        for (Simulation s : simulations.values()) {
            if (s.isActive()) {
                Neighbor client = s.getNeighbor();
                if (client.getIp().getHostAddress().equalsIgnoreCase(n.getIp().getHostAddress())&& client.getPort()==n.getPort()) {
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

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public ResultsReceiver getReceiver() {
        return receiver;
    }

    public void setReceiver(ResultsReceiver receiver) {
        this.receiver = receiver;
    }

//    private class PopulationDumper implements Runnable {
//
//        private SimulationStatus simulationStatus;
//        private Utils u;
//
//        public PopulationDumper(SimulationStatus simulationStatus) {
//            this.simulationStatus = simulationStatus;
//            u = new Utils();
//        }
//
//        public void run() {
//            long startTime = System.currentTimeMillis();
//            while (true) {
//                if (((System.currentTimeMillis() - startTime) / (1000 * 60) > 20)) {//every 20 minutes dump the current population
//                    u.insertObjectivesValuesIntoSolutions(simulationStatus);
//
//                    String headder = u.generateCSVHeadder(simulationStatus.getEnvironment());
//                    String result = headder;
//                    result += u.generateCSV(simulationStatus.getAlgorithm().getCurrentSolutionSet());
//                    try {
//                        BufferedWriter out = new BufferedWriter(new FileWriter("" + System.currentTimeMillis() + ".csv"));
//                        out.write(result);
//                        out.close();
//                    } catch (IOException e) {
//                    }
//                    startTime = System.currentTimeMillis();
//                }
//            }
//        }
//    }
}
