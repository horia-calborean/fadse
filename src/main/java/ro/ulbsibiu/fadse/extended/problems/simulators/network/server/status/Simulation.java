package ro.ulbsibiu.fadse.extended.problems.simulators.network.server.status;

import org.uma.jmetal.solution.Solution;
import ro.ulbsibiu.fadse.extended.problems.simulators.network.Message;
import ro.ulbsibiu.fadse.extended.problems.simulators.network.server.Neighbor;

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Horia Calborean
 */
public class Simulation{

    private String id;
    private Message message;
    private Solution<?> solution;
    private Neighbor neighbor;
    private int retries;
    private List<Neighbor> pastClients;
    private boolean active;
    private Timestamp simulationStartedTime;

    public <T> Simulation(String id, Message message, Solution<T> solution, Neighbor neighbor, boolean active) {
        init(id, message, solution, neighbor, active);
    }

    public <T> Simulation(String id, Message message, Solution<T> solution, Neighbor neighbor) {
        init(id, message, solution, neighbor, true);
    }

    private <T> void init(String id, Message message, Solution<T> solution, Neighbor neighbor, boolean active) {
        this.id = id;
        this.message = message;
        this.solution = solution;
        this.neighbor = neighbor;
        this.active = active;
        this.pastClients = new LinkedList<>();
        this.retries = 0;
        this.simulationStartedTime = new Timestamp(System.currentTimeMillis());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public Neighbor getNeighbor() {
        return neighbor;
    }

    public void setNeighbor(Neighbor neighbor) {
        this.neighbor = neighbor;
    }

    public <S extends Solution<?>> S getSolution() {
        return (S) solution;
    }

    public void setSolution(Solution<?> solution) {
        this.solution = solution;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<Neighbor> getPastClients() {
        return pastClients;
    }

    public void setPastClients(List<Neighbor> pastClients) {
        this.pastClients = pastClients;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries++;
    }

    public void increaseRetries() {
        this.retries++;
    }

    public void changeNeighbor(Neighbor n) {
        pastClients.add(this.neighbor);
        this.neighbor = n;
    }

    public Timestamp getSimulationStartedTime() {
        return simulationStartedTime;
    }

    public void setSimulationStartedTime(Timestamp simulationStartedTime) {
        this.simulationStartedTime = simulationStartedTime;
    }

  
}
