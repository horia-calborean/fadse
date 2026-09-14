package core.network;

import core.model.objectives.Objective;
import input.model.InputData;
import input.model.setup.CommonSetupParameters;
import input.ports.parameter.problem.ProblemParameter;
import org.ini4j.Wini;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.ConstraintHandling;
import org.uma.jmetal.util.ranking.Ranking;
import org.uma.jmetal.util.ranking.impl.FastNonDominatedSortRanking;
import output.application.CsvUtils;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StatusObserver<S extends Solution<?>> implements Runnable {
    private final SimulationStatus simulationStatus;

    public StatusObserver(SimulationStatus simulationStatus) {
        this.simulationStatus = simulationStatus;
    }

    public void run() {
        ObjectInputStream dis;
        ObjectOutputStream dos;
        try {

            String currentDir = System.getProperty("user.dir");
            File dir = new File(currentDir);
            Wini ini = new Wini(new File(dir + FileSystems.getDefault().getSeparator() + "configs" + FileSystems.getDefault().getSeparator() + "fadseConfig.ini"));
            try (ServerSocket serverSocket = new ServerSocket(ini.get("Monitor", "listenPort", int.class))) {
                Logger.getLogger(StatusObserver.class.getName()).log(Level.CONFIG, "Monitor Started ... listening on port: " + ini.get("Monitor", "listenPort", int.class));
                while (true) {
                    try {
                        StringBuilder result = new StringBuilder();
                        Logger.getLogger(StatusObserver.class.getName()).log(Level.INFO, "StatusObserver: Waiting for connection");
                        Socket socket = serverSocket.accept();
                        Logger.getLogger(StatusObserver.class.getName()).log(Level.INFO, "Status observer: connected");
                        dos = new ObjectOutputStream(socket.getOutputStream());
                        dos.flush();
                        dis = new ObjectInputStream(socket.getInputStream());
                        //                dos.writeObject(((JSONObject) JSONSerializer.toJSON( simulationStatus.getSentMessages() )));//TODO switch to String and use JSON or XML
                        String command = (String) dis.readObject();
                        try {
                            Logger.getLogger(StatusObserver.class.getName()).log(Level.INFO, "Status observer: read command -> " + command);
                            if (command.equalsIgnoreCase("nrActiveSimulations")) {
                                result = new StringBuilder(Integer.toString(simulationStatus.getNumberOfActiveSimulations()));

                            } else if (command.equalsIgnoreCase("activeSimulationsList")) {
                                for (String id : simulationStatus.getActiveSimulationsIds()) {
                                    result.append(id).append("#");
                                }
                            } else if (command.startsWith("startTimeOf:")) {
                                String id = command.substring(command.indexOf(":") + 1);
                                Simulation sim = simulationStatus.getSimulation(id);
                                result = new StringBuilder(Long.toString(sim.getSimulationStartedTime().getTime()));
                            } else if (command.startsWith("runningTimeOf:")) {
                                String id = command.substring(command.indexOf(":") + 1);
                                Simulation sim = simulationStatus.getSimulation(id);
                                result = new StringBuilder((float) (System.currentTimeMillis() - sim.getSimulationStartedTime().getTime()) / (1000 * 60) + " min");
                            } else if (command.startsWith("clientIpOf:")) {
                                String id = command.substring(command.indexOf(":") + 1);
                                Simulation sim = simulationStatus.getSimulation(id);
                                result = new StringBuilder(sim.getClientData().getIP().getHostAddress());
                            } else if (command.startsWith("runningMoreThan:")) {
                                String minutes = command.substring(command.indexOf(":") + 1);
                                for (String id : simulationStatus.getActiveSimulationsIds()) {
                                    Simulation sim = simulationStatus.getSimulation(id);
                                    long simTime = (System.currentTimeMillis() - sim.getSimulationStartedTime().getTime()) / (1000 * 60);
                                    long maxTime = Long.parseLong(minutes);
                                    if (maxTime < simTime) {
                                        result.append(id).append("#");
                                    }
                                }
                            } else if (command.startsWith("getCurrentPop")) {

                                List<S> solutionSet = SimulationUtils.insertObjectivesValuesIntoSolutions(simulationStatus);

                                InputData inputData = simulationStatus.getInputData();
                                ProblemParameter<?>[] designVariables = (ProblemParameter<?>[]) inputData.get(CommonSetupParameters.DESIGN_VARIABLES);
                                Map<String, Objective> objectives = (Map<String, Objective>) inputData.get(CommonSetupParameters.OBJECTIVES);
                                String header = CsvUtils.generateCSVHeader(designVariables, objectives);
                                result = new StringBuilder(header);
                                result.append(CsvUtils.generateCSV(solutionSet));
                            } else if (command.startsWith("getCurrentOptimalSet")) {
                                List<S> solutionSet = SimulationUtils.insertObjectivesValuesIntoSolutions(simulationStatus);

                                InputData inputData = simulationStatus.getInputData();
                                ProblemParameter<?>[] designVariables = (ProblemParameter<?>[]) inputData.get(CommonSetupParameters.DESIGN_VARIABLES);
                                Map<String, Objective> objectives = (Map<String, Objective>) inputData.get(CommonSetupParameters.OBJECTIVES);
                                String header = CsvUtils.generateCSVHeader(designVariables, objectives);
                                result = new StringBuilder(header);
                                List<S> finalSolutionSet = new ArrayList<>(solutionSet.size());
                                for (S s : solutionSet) {
                                    int numberOfViolatedConstraints = ConstraintHandling.numberOfViolatedConstraints(s);
                                    if (numberOfViolatedConstraints == 0) {
                                        finalSolutionSet.add(s);
                                    }
                                }
                                Ranking<S> r = new FastNonDominatedSortRanking<S>().compute(finalSolutionSet);
                                result.append(CsvUtils.generateCSV(r.getSubFront(0)));
                            } else {
                                result = new StringBuilder("Error: unknown command");
                                //TODO delete simulation X , stop client and mark ind as infeasible
                            }
                        } catch (Exception e) {
                            for (StackTraceElement ste : e.getStackTrace()) {
                                result.insert(0, ste.toString() + "\n");
                            }

                            result.insert(0, "## EXCEPTION " + e.getClass() + " - " + e.getMessage() + "##\n");

                            e.fillInStackTrace();
                        }
                        Logger.getLogger(StatusObserver.class.getName()).log(Level.INFO, "StatusObserver: writing result: " + result);
                        dos.writeObject(result.toString());
                        dos.flush();
                    } catch (Exception e) {
                        e.fillInStackTrace();
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(StatusObserver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}