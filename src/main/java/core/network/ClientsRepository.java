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
import java.nio.file.FileSystems;
import java.sql.Timestamp;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientsRepository {
    private static ClientsRepository instance;
    protected final ResultsReceiver receiver;
    protected ListOfFadseClients fadseClients;
    protected final SimulationStatus simulationStatus;
    protected final Map<FadseIndividual, Solution<?>> individualsToSend;//there are multiple individuals for a single solution (10 benchmarks , 1 solution)
    protected InputData inputData;

    private ClientsRepository(InputData inputData) throws IOException {
        this.inputData = inputData;
        fadseClients = ListOfFadseClients.getInstance();
        receiver = ResultsReceiver.getInstance();
        simulationStatus = SimulationStatus.getInstance();
        simulationStatus.setReceiver(receiver);
        individualsToSend = new HashMap<>();
    }

    public static synchronized ClientsRepository getInstance(InputData inputData) throws Exception {
        if (instance == null) {
            instance = new ClientsRepository(inputData);
        }
        return instance;
    }
    
    public void performSimulation(FadseIndividual individual, Solution<?> currentSolution) {
        detectAndRescheduleCrashedClients();

        if (fadseClients == null || (fadseClients.getSize() == 0)) {
            Logger.getLogger(ClientsRepository.class.getName()).log(Level.SEVERE, "No neighbors configured");
            return;
        }

        // TODO -> Is this currentSolution really needed ? -> Andrei
        individualsToSend.put(individual, currentSolution);
        FadseIndividual ind;
        Solution<?> s;
        while (!individualsToSend.isEmpty()) {
            ind = individualsToSend.keySet().iterator().next();
            s = individualsToSend.get(ind);
            performSimulationOnClient(ind, s);
            if (!ind.isFeasible()) {
                Logger.getLogger(ClientsRepository.class.getName()).log(Level.SEVERE, "ERROR: FadseIndividual is not feasible (any more)! Let's call Horia.");
            }
        }
    }

    private void performSimulationOnClient(FadseIndividual ind, Solution<?> solution) {
        boolean individualSent = false;
        for (int i = 0; i < fadseClients.getSize(); i++) {
            FadseClientData n = fadseClients.poll();
            fadseClients.addLast(n);
            if (n.getNumberOfSlots() - n.getNumberOfOccupiedSlots() > 0) {
                try {
                    Message m = MessageSender.sendIndividual(ind, n);
                    simulationStatus.addSimulation(m, n, solution);//currentSolution is set by the Simulator Wrapper, USE CAREFULLY
                    n.setNumberOfOccupiedSlots(n.getNumberOfOccupiedSlots() + 1);//this neighbor has just filled one of his slots
                    individualSent = true;
                    individualsToSend.remove(ind);
                    Logger.getLogger(ClientsRepository.class.getName()).log(Level.INFO, "FadseIndividual sent to: " + n);
                } catch (UnknownHostException ex) {
                    Logger.getLogger(ClientsRepository.class.getName()).log(Level.SEVERE, "Don't know about host", ex);
                } catch (IOException ex) {
                    ex.fillInStackTrace();
                    Logger.getLogger(ClientsRepository.class.getName()).log(Level.SEVERE, "Couldn't get I/O for the connection or ACK not received from" + n.getIP() + ":" + n.getPort(), "");
                } catch (Exception ex) {
                    Logger.getLogger(ClientsRepository.class.getName()).log(Level.SEVERE, "Other exception", ex);
                }
                if (individualSent) {
                    break;//get out of the for loop if everything went ok
                } else {//the try has gone bad - we could not connect to the client. Maybe he had some individuals simulating on him, we can not expect a result returning
                    if (simulationStatus.isClientSimulating(n)) {
                        //find out which was the individual(s) we sent to the client and re add them to the individualsToSend list
                        Map<FadseIndividual, DoubleSolution> indOnClient = simulationStatus.getIndividualsSimulatingOnClient(n);
                        simulationStatus.removeSimulationsOnClient(n);
                        individualsToSend.putAll(indOnClient);
                    }
                }
            } else {
//                    Logger.getLogger(ClientsRepository.class.getName()).log(Level.INFO, "Neighbor: " + n.getIp()+":"+n.getPort() + " has all the slots full. Trying to sleep for 200 ms before trying the next neighbor.");
                try {


                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ClientsRepository.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void join() {
        //do not return until all the neighbors have returned their results
        //while(still active simulations){}
        Logger.getLogger(ClientsRepository.class.getName()).log(Level.INFO, "Join method called");
        simulationStatus.removeRemainingSimulations();
        redistributeUnfinishedSimulations();
        Logger.getLogger(ClientsRepository.class.getName()).log(Level.INFO, "ClientsRepository.join - all the simulations are done");
        List<Message> receivedMessages = receiver.getResults();
        //Look in the received messages and in the messages sent. We have to find for each local kept message a received message with the objectives filled
        //Here we transfer the values of the objectives (from the remote ind) to the local individuals
        //It is time to detect if we have multiple individuals for the same solution-benchmark and choose only one of them
        //cleaning up the lists before proceeding. It can happen that (if a client is assumed crashed) we have multiple results sent back from different clients for teh same individual-benchmark
        //this will cause that individual to have 11 results instead of 10 for example. we have to find such duplicates and remove teh worse one of them (if one says it is infeasible)

        List<FadseIndividual> duplicateDetector = new LinkedList<>();
        List<Message> cleanMessages = new LinkedList<>();
        for (Message receivedMessage : receivedMessages) {
            for (Message localKeptMessage : simulationStatus.getSentMessages()) {
                if (receivedMessage.getMessageId().equals(localKeptMessage.getMessageId())) {
                    boolean copy = false;
                    if (duplicateDetector.contains(localKeptMessage.getIndividual())) {
                        //we already have its results, but we should look in the received one if it is in fact better than the one before
                        FadseIndividual rec = receivedMessage.getIndividual();
                        int objectivesSize = ((Map<String, Objective>)(inputData.get(CommonSetupParameters.OBJECTIVES))).size();
                        if (rec.isFeasible() && rec.getObjectives().size() != objectivesSize) {//is feasible and has all of its objective
                            //it does not matter if the old one was also feasible we just copy the results either way
                            copy = true;
                            for (int i = 0; i < rec.getObjectives().size(); i++) {
                                if (rec.getObjectives().get(i).getValue() == 0) {
                                    copy = false;
                                }
                            }
                        }
                        Logger.getLogger(ClientsRepository.class.getName()).log(Level.INFO, "We already have results for individual with benchmark: "+localKeptMessage.getIndividual().getSelectedBenchmark()+" should we copy its results?");
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
        Logger.getLogger(ClientsRepository.class.getName()).log(Level.INFO, "Clean messages size: " + cleanMessages.size());
        //At this point the local individuals have their objectives filled
        for (Message localKeptMessage : cleanMessages) {

            boolean infeasible = false;
            List<Objective> objectives = localKeptMessage.getIndividual().getObjectives();
            //FAILSAFE mechanism check if the number of objectives is correct
            try {
                int objectivesSize = ((Map<String, Objective>)(inputData.get(CommonSetupParameters.OBJECTIVES))).size();
                if (objectives.size() != objectivesSize) {
                    localKeptMessage.getIndividual().setBadValuesForObjectives();
                    Logger.getLogger(ClientsRepository.class.getName()).log(Level.SEVERE, "individual has not all the objectives filled");
                    infeasible = true;
                }
            } catch (Exception e) {
                Logger.getLogger(ClientsRepository.class.getName()).log(Level.SEVERE, "Something wrong with the objectives: " + e.getMessage());
            }
            for (int i = 0; i < objectives.size(); i++) {
                //obtain the solution of this individual
                Solution<?> s = simulationStatus.getSolution(localKeptMessage.getMessageId());
                Objective o = objectives.get(i);
                double value = s.objectives()[i];
                if (o.getValue() == 0) {
                    Logger.getLogger(ClientsRepository.class.getName()).log(Level.SEVERE, "individual has objectives set to 0 - marking him as infeasible[1]");
                    localKeptMessage.getIndividual().setBadValuesForObjectives();
                    o.setValue(Double.MAX_VALUE);
                    infeasible = true;
                }
//                        System.out.println("value for solution["+simulationStatus.getSolution(sentM.getMessageId()).getDecisionVariables()+"] for objective["+i+"] = "+o.getValue());
                value = (o.getValue() + value);//Add all the values. later we will divide it by the number of benchmarks
                s.objectives()[i] = value;
                // TODO - If the following lines are useless, remove them. If not, investigate how and when to initialize these arrays
                int counter = (int) s.attributes().get("counter");
                s.attributes().put("counter", counter + 1);
                String sum = ((String[])s.attributes().get("sum"))[i];
                s.attributes().put("sum", sum + "+" + o.getValue());
                Double tempSum = ((Double[])s.attributes().get("tempSum"))[i];
                s.attributes().put("tempSum", tempSum + "+" + o.getValue());
                //s.setObjective(i, o.getValue());
                if (infeasible || !localKeptMessage.getIndividual().isFeasible()) {
                    ConstraintHandling.numberOfViolatedConstraints(s, (Integer.MAX_VALUE));
                    //s.setNumberOfViolatedConstraint(s.getNumberOfViolatedConstraint() + environment.getInputDocument().getRules().size());
                    ConstraintHandling.overallConstraintViolationDegree(s,(Integer.MAX_VALUE));//TODO think of a value to put here
                }
            }

        }

        //compute the average
        //since the same solution exists  nrOfBenchmarks times in the messages sent list we have to divide by nr of benchmarks only once,
        //so we first build a set of all the solutions (no duplicates)
        Set<Solution<?>> solutions = new HashSet<>();
        for (Message localkeptMessage : cleanMessages) {
            boolean infeasible = false;
            //FAILSAFE test individual for correctness - test if ind has the correct number of objectives
            try {
                int objectivesSize = ((Map<String, Objective>)(inputData.get(CommonSetupParameters.OBJECTIVES))).size();
                if (localkeptMessage.getIndividual().getObjectives().size() != objectivesSize) {
                    localkeptMessage.getIndividual().setBadValuesForObjectives();
                    Logger.getLogger(ClientsRepository.class.getName()).log(Level.SEVERE, "individual has not all the objectives filled[1]");
                    infeasible = true;
                }
                //FAILSAFE test individual for correctness - test if objectives are not 0
                for (Objective o : localkeptMessage.getIndividual().getObjectives()) {
                    if (o.getValue() == 0) {
                        Logger.getLogger(ClientsRepository.class.getName()).log(Level.SEVERE, "individual has objectives set to 0 - marking him as infeasible[2]");
                        localkeptMessage.getIndividual().setBadValuesForObjectives();
                        infeasible = true;
                    }
                }
                Solution<?> localKeptSolution = simulationStatus.getSolution(localkeptMessage.getMessageId());
                //FAILSAFE - not all the benchmarks responded
                int benchmarkSize = ((List<String>)(inputData.get(CommonSetupParameters.BENCHMARKS))).size();
                if ((int)localKeptSolution.attributes().get("counter") != benchmarkSize * objectivesSize) {
                    Logger.getLogger(ClientsRepository.class.getName()).log(Level.SEVERE, "individual does not have results for all the benchmarks, or has more results (" + (localKeptSolution.attributes().get("counter") + "!=" + benchmarkSize * objectivesSize) + ") : ");
                    for (int i = 0; i < objectivesSize; i++) {
                        Logger.getLogger(ClientsRepository.class.getName()).log(Level.SEVERE, ((String[])localKeptSolution.attributes().get("sum"))[i]);
                    }
                    infeasible = true;
//TODO - TEST if the objectives are set in this method or calling set bad values is already too late
                }
                if (infeasible) {
                    ConstraintHandling.numberOfViolatedConstraints(localKeptSolution, (Integer.MAX_VALUE));
                    ConstraintHandling.overallConstraintViolationDegree(localKeptSolution, (Integer.MAX_VALUE)); //TODO think of a value to put here
                }
                solutions.add(localKeptSolution);
            } catch (Exception ignored) {
            }

        }
        for (Solution<?> s : solutions) {
            //System.out.println(s.getNumberOfViolatedConstraint() != 0 ? "Infeasible" : "Feasible");
            for (int i = 0; i < s.objectives().length; i++) {
                double value = s.objectives()[i];
                int benchmarkSize = ((List<String>)(inputData.get(CommonSetupParameters.BENCHMARKS))).size();
                value = value / benchmarkSize;//compute the average
                s.objectives()[i] = ((Double[])s.attributes().get("tempSum"))[i] / benchmarkSize;
                System.out.println(((String[])s.attributes().get("sum"))[i] + "/" + benchmarkSize + " = " + ((Double[])s.attributes().get("tempSum"))[i] / benchmarkSize + "=" + value);
                //cleaning up the solution - has to be done for algorithms that reuse the same object as PSO algorithms
                String[] sum = ((String[])s.attributes().get("sum"));
                sum[i] = null;
                s.attributes().put("sum", sum);
                Double[] tempSum = ((Double[])s.attributes().get("tempSum"));
                tempSum[i] = 0.0;
                s.attributes().put("tempSum", tempSum);
                s.attributes().put("counter", 0);
            }
        }
        //JOIN ended
        //FREE (Willy) memory
        simulationStatus.clearPerGenerationData();
        individualsToSend.clear();
        receiver.clearResults();
        Logger.getLogger(ClientsRepository.class.getName()).log(Level.INFO, "Join method finished.");
        //refreshing the neighbors
        fadseClients = (ListOfFadseClients) inputData.get(CommonSetupParameters.FADSE_CLIENTS);
        Logger.getLogger(ClientsRepository.class.getName()).log(Level.CONFIG, "Loaded " + fadseClients.getSize() + " neighbors...");

    }

    /**
     * This method looks if there are still unfinished simulations on the client. If there are it waits for them for a period of time.
     * If there are clients that have crashed it tries to send the individual to another client.
     */
    private void redistributeUnfinishedSimulations() {
        Logger.getLogger(ClientsRepository.class.getName()).log(Level.INFO, "redistributeUnfinishedSimulations called...");
        long startTime = System.currentTimeMillis();
        while (simulationStatus.getNumberOfActiveSimulations() > 0) {
            Map<String, String> problemConfigParameters = (Map<String, String>) inputData.get(CommonSetupParameters.PROBLEM_CONFIG);
            int maxTime = Integer.parseInt(problemConfigParameters.get("maximumTimeOfASimulation"));
            if (System.currentTimeMillis() - startTime > 1000L * 60 * maxTime * 2) {//we have been waiting for too long something might have happened in detectAndRescheduleCrashedClients
                //just make them all infeasible and move on with our life
                for (String messageId : simulationStatus.getActiveSimulationsIds()) {
                    Simulation s = simulationStatus.getSimulation(messageId);
                    s.getMessage().getIndividual().setBadValuesForObjectives();
                    s.setActive(false);
                }
                break;//leave the while
            }
            try {
                Logger.getLogger(ClientsRepository.class.getName()).log(Level.INFO, "Still waiting for " + simulationStatus.getActiveSimulations() + " results");
                try {
                    String currentDir = System.getProperty("user.dir");
                    File dir = new File(currentDir);
                    Wini ini = new Wini(new File(dir + FileSystems.getDefault().getSeparator() + "configs" + FileSystems.getDefault().getSeparator() + "fadseConfig.ini"));
                    int time = ini.get("RedistributeCheck", "timeSeconds", int.class);
                    Thread.sleep(time * 1000L);
                } catch (IOException ex) {
                    //the time could not be read from the config file switching to the default value
                    Thread.sleep(2000);
                }

                //after a period of time try to redistribute the not finished simulations
                detectAndRescheduleCrashedClients();
            } catch (InterruptedException ex) {
                Logger.getLogger(ClientsRepository.class.getName()).log(Level.SEVERE, "redistributeUnfinishedSimulations[1]: " + ex.getMessage());
            }
        }
        Logger.getLogger(ClientsRepository.class.getName()).log(Level.INFO, "redistributeUnfinishedSimulations finished.");
    }

    private void detectAndRescheduleCrashedClients() {
//        Logger.getLogger(ClientsRepository.class.getName()).log(Level.INFO, "detectAndRescheduleCrashedClients called...");
        for (String messageId : simulationStatus.getActiveSimulationsIds()) {
//            Logger.getLogger(ClientsRepository.class.getName()).log(Level.INFO, "handling messageID " + messageId + "...");
            Simulation s = simulationStatus.getSimulation(messageId);
            // TODO -> Refactor & get rid of Gap -> Andrei
            Map<String, String> problemConfigParameters = (Map<String, String>) inputData.get(CommonSetupParameters.PROBLEM_CONFIG);
            int maxTime = Integer.parseInt(problemConfigParameters.get("maximumTimeOfASimulation"));
            if (s != null && System.currentTimeMillis() - s.getSimulationStartedTime().getTime() > 1000L * 60 * maxTime) {
                //maximum allocated time has passed - check how many retries and mark ind as infeasible if number of retries exceeded
                //avoid deadlock if all the clients are simulating indefinitely
                Logger.getLogger(ClientsRepository.class.getName()).log(Level.INFO, "Retries for this message: " + s.getRetries());
                if (s.getRetries() > 1) {
                    s.getMessage().getIndividual().setBadValuesForObjectives();
                    s.setActive(false);
                    Logger.getLogger(ClientsRepository.class.getName()).log(Level.INFO, "FadseIndividual has been marked as infeasible.");
                } else {
                    //resend it to another client for simulation
                    s.increaseRetries();
                    s.getMessage().getIndividual().setBadValuesForObjectives();//set the objectives to bad values just in case
                    s.getMessage().getIndividual().setFeasible(true);
                    //remove the message that we are currently not waiting for from the waiting list
                    //simulationStatus.removeSimulationsOnClient(s.getNeighbor());//TODO test
                    s.setSimulationStartedTime(new Timestamp(System.currentTimeMillis()));
                    simulationStatus.removeSimulationsOnClient(s.getClientData());
                    performSimulationOnClient(s.getMessage().getIndividual(), s.getSolution());//TODO test this
                    Logger.getLogger(ClientsRepository.class.getName()).log(Level.INFO, "One more try...");
                }
                try {
                    // send a "kill simulation message" to the client (this way we can avoid all the clients being stuck on a simulation)
                    MessageSender.sendIndividual(s.getMessage().getIndividual(), s.getClientData(), Message.TYPE_CLOSE_SIMULATION_REQUEST);
                } catch (UnknownHostException ex) {
                    Logger.getLogger(ClientsRepository.class.getName()).log(Level.SEVERE, "detectAndRescheduleCrashedClients[1]" + ex.getMessage());
                } catch (IOException ex) {
                    Logger.getLogger(ClientsRepository.class.getName()).log(Level.SEVERE, "detectAndRescheduleCrashedClients[2]" + ex.getMessage());
                } catch (Exception ex) {
                    Logger.getLogger(ClientsRepository.class.getName()).log(Level.SEVERE, "detectAndRescheduleCrashedClients[3]" + ex.getMessage());
                }
            }
        }
//        Logger.getLogger(ClientsRepository.class.getName()).log(Level.INFO, "detectAndRescheduleCrashedClients finished.");
    }

    public void closeSimulation(FadseIndividual individual) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void dumpCurrentPopulation(List<DoubleSolution> population) {
        dumpCurrentPopulation("filled" + System.currentTimeMillis(), population);
    }

    public void dumpCurrentPopulation(String filename, List<DoubleSolution> population) {
        // TODO - Commented out by Andrei -> To be used, when needed
//        InputData inputData = simulationStatus.getInputData();
//        ProblemParameter<?>[] designVariables = (ProblemParameter<?>[]) inputData.get(CommonSetupParameters.DESIGN_VARIABLES);
//        Map<String, Objective> objectives = (Map<String, Objective>) inputData.get(CommonSetupParameters.OBJECTIVES);
//        String result = CsvUtils.generateCSVHeader(designVariables, objectives);
//        result += CsvUtils.generateCSV(population);
//
//        System.out.println("Result of the population (" + filename + "):\n" + result);
//
//        try {
//            boolean created = (new File(environment.getResultsFolder())).mkdirs();
//            BufferedWriter out = new BufferedWriter(new FileWriter(environment.getResultsFolder() + FileSystems.getDefault().getSeparator() + filename + ".csv"));
//            out.write(result);
//            out.close();
//        } catch (IOException e) {
//            System.err.println(e.getMessage());
//            e. fillInStackTrace();
//        }
    }
}