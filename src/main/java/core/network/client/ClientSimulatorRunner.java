package core.network.client;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientSimulatorRunner implements Runnable {

    private Individual individual;
    private SimulatorWrapper simulator;
    private Message m;

    public ClientSimulatorRunner(Individual individual, SimulatorWrapper simulator, Message m) {
//        System.out.println("Client Simulator Runner configured");
        this.individual = individual;
        this.simulator = simulator;
        this.m = m;
    }

    public void run() {
        try {
//            System.out.println("Client Simulator Runner started ...");
            //simualtor.evaluate(individual.getSolution());
            simulator.performSimulation(individual);
            //retrieve the results of the simulation
//            System.out.println("ClientSimulatorRunner: Simulation ended. Prepare to send back the results");
            ResultsSender resSender = new ResultsSender();
            resSender.send(individual, m);
        } catch (IOException ex) {
            Logger.getLogger(ClientSimulatorRunner.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
//            System.out.println("Client Simulator Runner finished.");
        }
    }

    public Individual getIndividual() {
        return individual;
    }
}
