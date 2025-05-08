package core.network.client;

import core.model.individual.FadseIndividual;
import core.network.Message;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientSimulatorRunner implements Runnable {

    private final FadseIndividual individual;
    private final Simulator simulator;
    private final Message message;

    public ClientSimulatorRunner(FadseIndividual individual, Simulator simulator, Message m) {
        this.individual = individual;
        this.simulator = simulator;
        this.message = m;
    }

    public void run() {
        try {
            simulator.performSimulation(individual);
            ResultsSender resSender = new ResultsSender();
            resSender.send(individual, message);
        } catch (IOException ex) {
            Logger.getLogger(ClientSimulatorRunner.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public FadseIndividual getIndividual() {
        return individual;
    }
}