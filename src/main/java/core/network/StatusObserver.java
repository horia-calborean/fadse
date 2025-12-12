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
import java.net.SocketTimeoutException;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StatusObserver<S extends Solution<?>> implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(StatusObserver.class.getName());

    // Configuration for DOS prevention
    private static final int MAX_CONCURRENT_CONNECTIONS =
        Integer.getInteger("fadse.monitor.max.connections", 5);
    private static final int SOCKET_TIMEOUT_MS = 30000; // 30 seconds
    private static final int THREAD_POOL_SIZE =
        Integer.getInteger("fadse.monitor.thread.pool", 3);

    private final SimulationStatus simulationStatus;
    private final Semaphore connectionLimiter;
    private final AtomicInteger activeConnections;
    private final ExecutorService threadPool;
    private volatile boolean running = true;

    public StatusObserver(SimulationStatus simulationStatus) {
        this.simulationStatus = simulationStatus;
        this.connectionLimiter = new Semaphore(MAX_CONCURRENT_CONNECTIONS, true);
        this.activeConnections = new AtomicInteger(0);
        this.threadPool = Executors.newFixedThreadPool(
            THREAD_POOL_SIZE,
            r -> {
                Thread t = new Thread(r, "StatusObserver-Worker");
                t.setDaemon(true);
                return t;
            }
        );
    }

    public void run() {
        try {
            String currentDir = System.getProperty("user.dir");
            File dir = new File(currentDir);
            Wini ini = new Wini(new File(dir + FileSystems.getDefault().getSeparator() + "configs" + FileSystems.getDefault().getSeparator() + "fadseConfig.ini"));
            int listenPort = ini.get("Monitor", "listenPort", int.class);

            try (ServerSocket serverSocket = new ServerSocket(listenPort)) {
                // Set timeout so we can check 'running' flag periodically
                serverSocket.setSoTimeout(5000);

                LOGGER.log(Level.CONFIG, "Monitor started, listening on port: " + listenPort);
                LOGGER.log(Level.CONFIG, String.format(
                    "Monitor limits: max_connections=%d, thread_pool=%d",
                    MAX_CONCURRENT_CONNECTIONS, THREAD_POOL_SIZE
                ));

                while (running) {
                    try {
                        // Try to acquire permit (blocks if too many connections)
                        if (!connectionLimiter.tryAcquire(1, TimeUnit.SECONDS)) {
                            LOGGER.log(Level.WARNING, String.format(
                                "Connection limit reached (%d/%d active), rejecting new connections",
                                activeConnections.get(), MAX_CONCURRENT_CONNECTIONS
                            ));
                            continue;
                        }

                        Socket socket = null;
                        try {
                            socket = serverSocket.accept();
                            socket.setSoTimeout(SOCKET_TIMEOUT_MS);

                            int active = activeConnections.incrementAndGet();
                            LOGGER.log(Level.FINE, String.format(
                                "Monitor connection from %s (active: %d/%d)",
                                socket.getInetAddress().getHostAddress(),
                                active, MAX_CONCURRENT_CONNECTIONS
                            ));

                            // Handle in thread pool
                            final Socket clientSocket = socket;
                            threadPool.submit(() -> handleMonitorRequest(clientSocket));

                        } catch (Exception e) {
                            // Failed to accept or submit - release permit
                            connectionLimiter.release();
                            if (socket != null) {
                                closeQuietly(socket);
                            }
                            throw e;
                        }

                    } catch (SocketTimeoutException e) {
                        // Normal timeout - allows checking 'running' flag
                        continue;
                    } catch (InterruptedException e) {
                        LOGGER.log(Level.WARNING, "StatusObserver interrupted", e);
                        Thread.currentThread().interrupt();
                        break;
                    }
                }

                LOGGER.log(Level.INFO, "StatusObserver shutting down...");
                shutdownGracefully();

            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error with monitor server socket", e);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Cannot start StatusObserver", ex);
        }
    }

    /**
     * Handles a single monitor request from a client
     */
    private void handleMonitorRequest(Socket socket) {
        ObjectOutputStream dos = null;
        ObjectInputStream dis = null;

        try {
                        StringBuilder result = new StringBuilder();

                        dos = new ObjectOutputStream(socket.getOutputStream());
                        dos.flush();
                        dis = new ObjectInputStream(socket.getInputStream());
                        String command = (String) dis.readObject();

                        // Validate command
                        if (command == null || command.trim().isEmpty()) {
                            LOGGER.log(Level.WARNING, "Received empty command from " + socket.getInetAddress());
                            result.append("Error: empty command");
                        } else if (command.length() > 1000) {
                            LOGGER.log(Level.WARNING, "Received excessively long command from " + socket.getInetAddress());
                            result.append("Error: command too long");
                        } else {
                        try {
                            LOGGER.log(Level.INFO, "Monitor received command: " + command);
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
                            } else if (command.equalsIgnoreCase("networkHealth")) {
                                // Show network diagnostics and health status
                                result = new StringBuilder(NetworkDiagnostics.getInstance().getHealthSummary());
                            } else if (command.equalsIgnoreCase("simulationStats")) {
                                // Show simulation status statistics
                                Map<String, Object> stats = simulationStatus.getStatistics();
                                result = new StringBuilder("=== Simulation Statistics ===\n");
                                StringBuilder finalResult = result;
                                stats.forEach((key, value) ->
                                    finalResult.append(String.format("%s: %s\n", key, value))
                                );
                            } else {
                                result = new StringBuilder("Error: unknown command");
                                //TODO delete simulation X , stop client and mark ind as infeasible
                            }
                        } catch (Exception e) {
                            // Build detailed error message
                            StringBuilder errorMsg = new StringBuilder();
                            errorMsg.append("## EXCEPTION: ").append(e.getClass().getSimpleName())
                                   .append(" - ").append(e.getMessage()).append(" ##\n");

                            // Add limited stack trace (first 10 frames)
                            StackTraceElement[] trace = e.getStackTrace();
                            int limit = Math.min(trace.length, 10);
                            for (int i = 0; i < limit; i++) {
                                errorMsg.append(trace[i].toString()).append("\n");
                            }
                            if (trace.length > limit) {
                                errorMsg.append("... ").append(trace.length - limit)
                                       .append(" more frames\n");
                            }

                            result = new StringBuilder(errorMsg.toString());
                            LOGGER.log(Level.SEVERE, "Error processing monitor command: " + command, e);
                        }
                        } // End of command validation

                        LOGGER.log(Level.FINE, "Sending monitor response (length: " + result.length() + ")");
                        dos.writeObject(result.toString());
                        dos.flush();

                    } catch (SocketTimeoutException e) {
                        LOGGER.log(Level.WARNING, "Monitor request timed out from " + socket.getInetAddress());
                    } catch (ClassNotFoundException e) {
                        LOGGER.log(Level.SEVERE, "Invalid command format from " + socket.getInetAddress(), e);
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, "I/O error handling monitor request from " + socket.getInetAddress(), e);
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Unexpected error handling monitor request from " + socket.getInetAddress(), e);
                    } finally {
                        // Always close resources and release permit
                        closeQuietly(dis);
                        closeQuietly(dos);
                        closeQuietly(socket);

                        int active = activeConnections.decrementAndGet();
                        connectionLimiter.release();
                        LOGGER.log(Level.FINE, "Monitor connection closed (active: " + active + ")");
                    }
    }

    /**
     * Gracefully shuts down the thread pool
     */
    private void shutdownGracefully() {
        running = false;
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(30, TimeUnit.SECONDS)) {
                LOGGER.log(Level.WARNING, "Monitor thread pool did not terminate, forcing shutdown");
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Helper method to close resources without throwing exceptions
     */
    private void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                Logger.getLogger(StatusObserver.class.getName()).log(Level.FINE, "Error closing resource", e);
            }
        }
    }
}