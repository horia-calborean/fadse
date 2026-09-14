package core.network;

import core.model.clients.FadseClientData;
import org.uma.jmetal.solution.Solution;

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

public class Simulation {

    private String id;
    private Message message;
    private Solution<?> solution;
    private FadseClientData clientData;
    private int retries;
    private List<FadseClientData> pastClients;
    private boolean active;
    private Timestamp simulationStartedTime;

    public <T> Simulation(String id, Message message, Solution<T> solution, FadseClientData neighbor, boolean active) {
        init(id, message, solution, neighbor, active);
    }

    public <T> Simulation(String id, Message message, Solution<T> solution, FadseClientData neighbor) {
        init(id, message, solution, neighbor, true);
    }

    private <T> void init(String id, Message message, Solution<T> solution, FadseClientData neighbor, boolean active) {
        this.id = id;
        this.message = message;
        this.solution = solution;
        this.clientData = neighbor;
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

    public FadseClientData getClientData() {
        return clientData;
    }

    public void setClientData(FadseClientData clientData) {
        this.clientData = clientData;
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

    public List<FadseClientData> getPastClients() {
        return pastClients;
    }

    public void setPastClients(List<FadseClientData> pastClients) {
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

    public void changeNeighbor(FadseClientData n) {
        pastClients.add(this.clientData);
        this.clientData = n;
    }

    public Timestamp getSimulationStartedTime() {
        return simulationStartedTime;
    }

    public void setSimulationStartedTime(Timestamp simulationStartedTime) {
        this.simulationStartedTime = simulationStartedTime;
    }
}