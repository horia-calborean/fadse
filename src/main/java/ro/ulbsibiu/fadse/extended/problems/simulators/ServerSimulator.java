/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ulbsibiu.fadse.extended.problems.simulators;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import jmetal.base.Solution;
import jmetal.base.SolutionSet;

import org.ini4j.Wini;

import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.environment.Individual;
import ro.ulbsibiu.fadse.environment.Objective;
import ro.ulbsibiu.fadse.extended.problems.SimulatorWrapper;
import ro.ulbsibiu.fadse.extended.problems.simulators.network.Message;
import ro.ulbsibiu.fadse.extended.problems.simulators.network.server.MessageSender;
import ro.ulbsibiu.fadse.extended.problems.simulators.network.server.Neighbor;
import ro.ulbsibiu.fadse.extended.problems.simulators.network.server.Neighborhood;
import ro.ulbsibiu.fadse.extended.problems.simulators.network.server.ResultsReceiver;
import ro.ulbsibiu.fadse.extended.problems.simulators.network.server.status.Simulation;
import ro.ulbsibiu.fadse.extended.problems.simulators.network.server.status.SimulationStatus;
import ro.ulbsibiu.fadse.utils.Utils;

/**
 *
 * @author Horia Calborean
 */
public class ServerSimulator extends SimulatorWrapper {

//    private List<String> currentlySimulating;
//    private Map<Message, Solution> sentToSimulation;
    private ResultsReceiver receiver;
    private LinkedList<Neighbor> neighbors;
    private SimulationStatus simulationStatus;
    private Map<Individual, Solution> individualsToSend;//there are multiple individuals for a single solution (10 benchmarks , 1 solution)

    public ServerSimulator(Environment environment) throws ClassNotFoundException, IOException, ParserConfigurationException {
        super(environment);
        Logger.getLogger(ServerSimulator.class.getName()).log(Level.INFO, "ServerSimulator started...");
        neighbors = (Neighborhood.getInstance(environment.getNeighborsConfigFile())).getNeighbors();
        Logger.getLogger(ServerSimulator.class.getName()).log(Level.CONFIG, "Loaded " + neighbors.size() + " neighbors...");
        //start a thread that monitors the responses of the neighbors
        receiver = ResultsReceiver.getInstance();
        simulationStatus = SimulationStatus.getInstance();
        simulationStatus.setReceiver(receiver);//TODO solve bad design
        individualsToSend = new HashMap<Individual, Solution>();
    }

    @Override
    public void performSimulation(Individual individual) {
        Logger.getLogger(ServerSimulator.class.getName()).log(Level.INFO, individual.toString());
        //check neighbor status to see if some of the neighbors are simulating for too long and stop the simualtion there
        detectAndRescheduleCrashedClients();
        //see for each neighbor if it still has a slot free.
        //Send the individual to the neighbor


        if (neighbors == null || neighbors.size() < 1) {
            Logger.getLogger(ServerSimulator.class.getName()).log(Level.SEVERE, "No neighbors configured");
            return;
        }
        individualsToSend.put(individual, currentSolution);
        Individual ind;
        Solution s;
        while (individualsToSend.size() > 0) {
            ind = individualsToSend.keySet().iterator().next();
            s = individualsToSend.get(ind);
            performSimulationOnClient(ind, s);
            if (!ind.isFeasible()) {
                Logger.getLogger(ServerSimulator.class.getName()).log(Level.SEVERE, "ERROR: Individual is not feasible (any more)! Let's call Horia.");
            }


        }
//        Logger.getLogger(ServerSimulator.class.getName()).log(Level.INFO, "perform simulation method ended for " + individual.toString());
    }

    private void performSimulationOnClient(Individual ind, Solution solution) {
        boolean individualSent = false;
        for (int i = 0; i < neighbors.size(); i++) {
            Neighbor n = neighbors.poll();
            neighbors.addLast(n);
//                Logger.getLogger(ServerSimulator.class.getName()).log(Level.INFO, "Checking client " + i + ": " + n);
            // for (Neighbor n : neighbors) {
            if (n.getNumberOfSlots() - n.getNumberOfOcupiedSlots() > 0) {
                try {
//                        Logger.getLogger(ServerSimulator.class.getName()).log(Level.INFO, "Found an available client... " + n.toString());
                    Message m = MessageSender.sendIndividual(ind, n);
                    simulationStatus.addSimulation(m, n, solution);//currentSolution is set by the Simulator Wrapper, USE CAREFULLY
                    n.setNumberOfOcupiedSlots(n.getNumberOfOcupiedSlots() + 1);//this neighbor has just filde one of his slots
                    individualSent = true;
                    individualsToSend.remove(ind);
                    Logger.getLogger(ServerSimulator.class.getName()).log(Level.INFO, "Individual sent to: " + n);
                } catch (UnknownHostException ex) {
                    Logger.getLogger(ServerSimulator.class.getName()).log(Level.SEVERE, "Don't know about host", ex);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(ServerSimulator.class.getName()).log(Level.SEVERE, "Couldn't get I/O for the connection or ACK not received from" + n.getIp() + ":" + n.getPort(), "");
                } catch (Exception ex) {
                    Logger.getLogger(ServerSimulator.class.getName()).log(Level.SEVERE, "Other exception", ex);
                }
                if (individualSent) {
                    break;//get out of the for loop if everything went ok
                } else {//the try has gone bad - we could not connect to the client. Maybe he had some individuals simulating on him we can not expect a result returning
                    if (simulationStatus.isClientSimulating(n)) {
                        //find out which was the individual(s) we sent to the client and re add them to the individualsToSend list
                        Map<Individual, Solution> indOnClient = simulationStatus.getIndividualsSimulatingOnClient(n);
                        simulationStatus.removeSimulationsOnClient(n);
                        individualsToSend.putAll(indOnClient);
                    }
                }
            } else {
//                    Logger.getLogger(ServerSimulator.class.getName()).log(Level.INFO, "Neighbor: " + n.getIp()+":"+n.getPort() + " has all the slots full. Trying to sleep for 200 ms before trying the next neighbor.");
                try {


                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ServerSimulator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void join() {
        //do not return until all the neighbors have returned their results
        //while(still active simulations){}
        Logger.getLogger(ServerSimulator.class.getName()).log(Level.INFO, "Join method called");
        simulationStatus.removeRemainingSimulations();
        redistributeUnfinishedSimulations();
        Logger.getLogger(ServerSimulator.class.getName()).log(Level.INFO, "ServerSimulator.join - all the simulations are done");
        List<Message> receivedMessages = receiver.getResults();
        //Look in the received messages and in the sent messages. We have to find for each local kept mesage a received message with the objectives filled
        //Here we transfer the values of the objectives (from the remote ind) to the local individuals
        //It is time to detect if we have multiple individuals for the same solution-benchmark and choose only one of them
        //cleaning up the lists before proceding. It can happen that (if a client is assumed crashed) we have multiple results sent back from different clients for teh same individual-benchmark
        //this will cause that individual to have 11 results instead of 10 for example. we have to find such duplicates and remove teh worse one of them (if one says it is infeasible)

        List<Individual> duplicateDetector = new LinkedList<Individual>();
        List<Message> cleanMessages = new LinkedList<Message>();
        for (Message receivedMessage : receivedMessages) {
            for (Message localKeptMessage : simulationStatus.getSentMessages()) {
                if (receivedMessage.getMessageId().equals(localKeptMessage.getMessageId())) {
                    boolean copy = false;
                    if (duplicateDetector.contains(localKeptMessage.getIndividual())) {
                        //we already have its results but we should look in the received one if it is in fact better than the one before
                        Individual rec = receivedMessage.getIndividual();
                        if (rec.isFeasible() && rec.getObjectives().size() != environment.getInputDocument().getObjectives().size()) {//is feasible and has all of its objective
                            //it does not matter if the old one was also feasible we just copy the results either way
                            copy = true;
                            for (int i = 0; i < rec.getObjectives().size(); i++) {
                                if (rec.getObjectives().get(i).getValue() == 0) {
                                    copy = false;
                                }
                            }
                        }
                        Logger.getLogger(ServerSimulator.class.getName()).log(Level.INFO, "We already have results for individual with benchmark: "+localKeptMessage.getIndividual().getBenchmark()+" should we copy its results?");
                    } else {
                        cleanMessages.add(localKeptMessage);
                        duplicateDetector.add(localKeptMessage.getIndividual());
                        copy = true;
                    }
                    if (copy) {
                        localKeptMessage.getIndividual().setObjectives((LinkedList<Objective>) receivedMessage.getIndividual().getObjectives());
                        localKeptMessage.getIndividual().setFeasible(receivedMessage.getIndividual().isFeasible());
                    }
                }
            }
        }
        Logger.getLogger(ServerSimulator.class.getName()).log(Level.INFO, "Clean messages size: " + cleanMessages.size());
        //At this point the local individuals have their objectives filled
        for (Message localKeptMessage : cleanMessages) {

            boolean infeasible = false;
            List<Objective> objs = localKeptMessage.getIndividual().getObjectives();
            //FAILSAFE mechanism check if the number of objectives is correct
            try {
                if (objs.size() != environment.getInputDocument().getObjectives().size()) {
                    localKeptMessage.getIndividual().markAsInfeasibleAndSetBadValuesForObjectives("Wrong number of objectives [2]");
                    Logger.getLogger(ServerSimulator.class.getName()).log(Level.SEVERE, "individual has not all the objectives filled");
                    infeasible = true;
                }
            } catch (Exception e) {
                Logger.getLogger(ServerSimulator.class.getName()).log(Level.SEVERE, "Something wrong with the objectives: " + e.getMessage());
            }
            for (int i = 0; i < objs.size(); i++) {
                //obtain the solution of this individual
                Solution s = simulationStatus.getSolution(localKeptMessage.getMessageId());
                Objective o = objs.get(i);
                double value = s.getObjective(i);
                if (o.getValue() == 0) {
                    Logger.getLogger(ServerSimulator.class.getName()).log(Level.SEVERE, "individual has objectives set to 0 - marking him as infeasible[1]");
                    localKeptMessage.getIndividual().markAsInfeasibleAndSetBadValuesForObjectives("individual has objectives set to 0[1]");
                    o.setValue(Double.MAX_VALUE);
                    infeasible = true;
                }
//                        System.out.println("value for solution["+simulationStatus.getSolution(sentM.getMessageId()).getDecisionVariables()+"] for objective["+i+"] = "+o.getValue());
                value = (o.getValue() + value);//Add all the values. later we will divide it by the number of benchmarks
                s.setObjective(i, value);
                s.setCounter(s.getCounter() + 1);
                s.setSum(i, s.getSum(i) + "+" + o.getValue());
                s.setTempSum(i, s.getTempSum(i) + o.getValue());
                //s.setObjective(i, o.getValue());
                if (infeasible || !localKeptMessage.getIndividual().isFeasible()) {
                    s.setNumberOfViolatedConstraint(Integer.MAX_VALUE);
                    //s.setNumberOfViolatedConstraint(s.getNumberOfViolatedConstraint() + environment.getInputDocument().getRules().size());
                    s.setOverallConstraintViolation(Integer.MAX_VALUE);//TODO think of a value to put here
                }
            }

        }

        //compute the average
        //since the same solution exists  nrOfBenchmarks times in the sent messages list we have to divide by nr of benchmarks only once
        //so we first build a set of all the solutions (no duplicates)
        Set<Solution> solutions = new HashSet<Solution>();
        for (Message localkeptMessage : cleanMessages) {
            boolean infeasible = false;
            //FAILSAFE test individual for corectness - test if ind has the correct number of objectives
            try {
                if (localkeptMessage.getIndividual().getObjectives().size() != environment.getInputDocument().getObjectives().size()) {
                    localkeptMessage.getIndividual().markAsInfeasibleAndSetBadValuesForObjectives("Wrong number of objectives [1]");
                    Logger.getLogger(ServerSimulator.class.getName()).log(Level.SEVERE, "individual has not all the objectives filled[1]");
                    infeasible = true;
                }
                //FAILSAFE test individual for corectness - test if objectives are not 0
                for (Objective o : localkeptMessage.getIndividual().getObjectives()) {
                    if (o.getValue() == 0) {
                        Logger.getLogger(ServerSimulator.class.getName()).log(Level.SEVERE, "individual has objectives set to 0 - marking him as infeasible[2]");
                        localkeptMessage.getIndividual().markAsInfeasibleAndSetBadValuesForObjectives("individual has objectives set to 0[2]");
                        infeasible = true;
                    }
                }
                Solution localKeptSolution = simulationStatus.getSolution(localkeptMessage.getMessageId());
                //FAILSAFE - not all the benchmarks responded
                if (localKeptSolution.getCounter() != environment.getInputDocument().getBenchmarks().size() * environment.getInputDocument().getObjectives().values().size()) {
                    Logger.getLogger(ServerSimulator.class.getName()).log(Level.SEVERE, "individual does not have results for all the benchmarks, or has more results (" + (localKeptSolution.getCounter() + "!=" + environment.getInputDocument().getBenchmarks().size() * environment.getInputDocument().getObjectives().values().size()) + ") : ");
                    for (int i = 0; i < environment.getInputDocument().getObjectives().values().size(); i++) {
                        Logger.getLogger(ServerSimulator.class.getName()).log(Level.SEVERE, localKeptSolution.getSum(i));
                    }
                    infeasible = true;
//TODO - TEST if the objectives are set in this method or calling set bad values is already too late
                }
                if (infeasible) {
                    localKeptSolution.setNumberOfViolatedConstraint(Integer.MAX_VALUE);
                    localKeptSolution.setOverallConstraintViolation(Integer.MAX_VALUE);//TODO think of a value to put here
                }
                solutions.add(localKeptSolution);
            } catch (Exception e) {
            }

        }
        for (Solution s : solutions) {
            //System.out.println(s.getNumberOfViolatedConstraint() != 0 ? "Infeasible" : "Feasible");
            for (int i = 0; i < s.numberOfObjectives(); i++) {
                double value = s.getObjective(i);
                value = value / environment.getInputDocument().getBenchmarks().size();//compute the average
                s.setObjective(i, s.getTempSum(i) / environment.getInputDocument().getBenchmarks().size());
                System.out.println(s.getSum(i) + "/" + environment.getInputDocument().getBenchmarks().size() + " = " + s.getTempSum(i) / environment.getInputDocument().getBenchmarks().size() + "=" + value);
                //cleaning up the solution - has to be done for algorithms that reuse the same object like PSO algorithms
                s.setSum(i, null);
                s.setTempSum(i, 0);
                s.setCounter(0);
            }
        }
        //JOIN ended
        //FREE (Willy) memory
        simulationStatus.clearPerGenerationData();
        individualsToSend.clear();
        receiver.clearResults();
        Logger.getLogger(ServerSimulator.class.getName()).log(Level.INFO, "Join method finished.");
        //refreshing the neighbors
        try {
            neighbors = Neighborhood.getRefreshedNeighbors();
            Logger.getLogger(ServerSimulator.class.getName()).log(Level.CONFIG, "Loaded " + neighbors.size() + " neighbors...");
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(ServerSimulator.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * This method looks if there are still unfinished simulations on the client. If there are it waits for them for a period of time.
     * If there are clients that have crashed it tries to send the individual to another client.
     */
    private void redistributeUnfinishedSimulations() {
        Logger.getLogger(ServerSimulator.class.getName()).log(Level.INFO, "redistributeUnfinishedSimulations called...");
        long startTime = System.currentTimeMillis();
        while (simulationStatus.getNumberOfActiveSimulations() > 0) {
            int maxTime = Integer.parseInt(environment.getInputDocument().getSimulatorParameter("maximumTimeOfASimulation"));
            if (System.currentTimeMillis() - startTime > 1000 * 60 * maxTime * 2) {//we have been waiting for too long something might have happened in detectAndRescheduleCrashedClients
                //just make them all infeasible and move on with our life
                for (String messageId : simulationStatus.getActiveSimulationsIds()) {
                    Simulation s = simulationStatus.getSimulation(messageId);
                    s.getMessage().getIndividual().markAsInfeasibleAndSetBadValuesForObjectives("We waited too long for this one to finnish");
                    s.setActive(false);
                }
                break;//leave the while
            }
            try {
                Logger.getLogger(ServerSimulator.class.getName()).log(Level.INFO, "Still waiting for " + simulationStatus.getActiveSimulations() + " results");
                try {
                    String currentdir = System.getProperty("user.dir");
                    File dir = new File(currentdir);
                    Wini ini = new Wini(new File(dir + System.getProperty("file.separator") + "configs" + System.getProperty("file.separator") + "fadseConfig.ini"));
                    int time = ini.get("RedistributeCheck", "timeSeconds", int.class);
                    Thread.sleep(time * 1000);
                } catch (IOException ex) {
                    //the time could not be read from the config file switching to the default value
                    Thread.sleep(2000);
                }

                //after a period of time try to redistribute the not finished simulations
                detectAndRescheduleCrashedClients();
            } catch (InterruptedException ex) {
                Logger.getLogger(ServerSimulator.class.getName()).log(Level.SEVERE, "redistributeUnfinishedSimulations[1]: " + ex.getMessage());
            }
        }
        Logger.getLogger(ServerSimulator.class.getName()).log(Level.INFO, "redistributeUnfinishedSimulations finished.");
    }

    private void detectAndRescheduleCrashedClients() {
//        Logger.getLogger(ServerSimulator.class.getName()).log(Level.INFO, "detectAndRescheduleCrashedClients called...");
        for (String messageId : simulationStatus.getActiveSimulationsIds()) {
//            Logger.getLogger(ServerSimulator.class.getName()).log(Level.INFO, "handling messageID " + messageId + "...");
            Simulation s = simulationStatus.getSimulation(messageId);
            int maxTime = Integer.parseInt(environment.getInputDocument().getSimulatorParameter("maximumTimeOfASimulation"));
            if (s != null && System.currentTimeMillis() - s.getSimulationStartedTime().getTime() > 1000 * 60 * maxTime) {
                //maximum allocated time has passed - check how many retries and mark ind as infeasible if number of retries exceeded
                //avoid deadlock if all the clients are simulating indefinitely
                Logger.getLogger(ServerSimulator.class.getName()).log(Level.INFO, "Retries for this message: " + s.getRetries());
                if (s.getRetries() > 1) {
                    s.getMessage().getIndividual().markAsInfeasibleAndSetBadValuesForObjectives("Too many retries");
                    s.setActive(false);
                    Logger.getLogger(ServerSimulator.class.getName()).log(Level.INFO, "Individual has been marked as infeasible.");
                } else {
                    //resend it to another client for simulation
                    s.increaseRetries();
                    s.getMessage().getIndividual().markAsInfeasibleAndSetBadValuesForObjectives("retring individual. It will be set as feasible again. But we set the objectives to bad values just in case");//set the objectives to bad values just in case
                    s.getMessage().getIndividual().setFeasible(true);
                    //remove the messsage that we are curently not waiting for from the waiting list
                    //simulationStatus.removeSimulationsOnClient(s.getNeighbor());//TODO test
                    s.setSimulationStartedTime(new Timestamp(System.currentTimeMillis()));
                    simulationStatus.removeSimulationsOnClient(s.getNeighbor());
                    performSimulationOnClient(s.getMessage().getIndividual(), s.getSolution());//TODO test this
                    Logger.getLogger(ServerSimulator.class.getName()).log(Level.INFO, "One more try...");
                }
                try {
                    // send a "kill simulation message" to the client (this way we can avoid all the clients being stuck on a simulation)
                    MessageSender.sendIndividual(s.getMessage().getIndividual(), s.getNeighbor(), Message.TYPE_CLOSE_SIMULATION_REQUEST);
                } catch (UnknownHostException ex) {
                    Logger.getLogger(ServerSimulator.class.getName()).log(Level.SEVERE, "detectAndRescheduleCrashedClients[1]" + ex.getMessage());
                } catch (IOException ex) {
                    Logger.getLogger(ServerSimulator.class.getName()).log(Level.SEVERE, "detectAndRescheduleCrashedClients[2]" + ex.getMessage());
                } catch (Exception ex) {
                    Logger.getLogger(ServerSimulator.class.getName()).log(Level.SEVERE, "detectAndRescheduleCrashedClients[3]" + ex.getMessage());
                }
            }
        }
//        Logger.getLogger(ServerSimulator.class.getName()).log(Level.INFO, "detectAndRescheduleCrashedClients finished.");
    }

    @Override
    public void closeSimulation(Individual individual) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void dumpCurrentPopulation(SolutionSet population) {        
        dumpCurrentPopulation("filled" + System.currentTimeMillis(), population);
    }

    public void dumpCurrentPopulation(String filename, SolutionSet population) {
        String result = (new Utils()).generateCSVHeadder(simulationStatus.getEnvironment());
        result += (new Utils()).generateCSV(population);
        
        System.out.println("Result of the population (" + filename + "):\n" + result);
        
        try {
            (new File(environment.getResultsFolder())).mkdirs();
            BufferedWriter out = new BufferedWriter(new FileWriter(environment.getResultsFolder() + System.getProperty("file.separator") + filename + ".csv"));
            out.write(result);
            out.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public Environment getEnvironment(){
        return this.environment;
    }
}
