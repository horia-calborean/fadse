package core.network.client;

import core.network.ConnectionPool;
import core.network.Message;
import input.model.InputData;
import input.model.setup.CommonSetupParameters;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * IndividualReceiver - Server component that runs on simulation clients.
 * Listens for simulation requests from the FADSE server, executes simulations,
 * and sends results back.
 *
 * Improvements:
 * - Thread-safe state management (AtomicBoolean, AtomicLong)
 * - Proper resource management (no socket leaks)
 * - Socket timeout configuration (prevents hanging)
 * - Better exception handling and logging
 * - Message validation
 */
public class IndividualReceiver implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(IndividualReceiver.class.getName());

    // Configuration constants
    private static final int SOCKET_ACCEPT_TIMEOUT_MS = 5000;  // 5 seconds
    private static final int SOCKET_READ_TIMEOUT_MS = 60000;   // 60 seconds
    private static final int MAX_PORT_RETRIES = 1000;
    private static final int MAX_STARTUP_DELAY_MS = 10000;

    protected ServerSocket serverSocket;
    protected ClientSimulatorRunner clientSimulatorRunner;
    protected int retries = 0;
    protected static final Random random = new Random(System.currentTimeMillis());

    // Thread-safe state tracking
    private final AtomicLong connectionWaitStartTime = new AtomicLong(System.currentTimeMillis());
    private final AtomicBoolean simulating = new AtomicBoolean(false);
    private volatile boolean running = true;

    public IndividualReceiver(int port) {
        initSocket(port);
    }

    /**
     * Gets the timestamp when the client started waiting for the next connection.
     * Thread-safe.
     */
    public long getConnectionWaitStartTime() {
        return connectionWaitStartTime.get();
    }

    /**
     * Checks if a simulation is currently running.
     * Thread-safe.
     */
    public boolean isSimulating() {
        return simulating.get();
    }

    /**
     * Signals the receiver to stop accepting new connections and shut down gracefully.
     */
    public void shutdown() {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error closing server socket during shutdown", e);
            }
        }
    }

    protected void initSocket(int port) {
        try {
            // Random startup delay to avoid thundering herd when multiple clients start
            Thread.sleep(random.nextInt(MAX_STARTUP_DELAY_MS));

            serverSocket = new ServerSocket(port);
            // Set accept timeout so we can check 'running' flag periodically
            serverSocket.setSoTimeout(SOCKET_ACCEPT_TIMEOUT_MS);

            InetAddress ip;
            try {
                ip = InetAddress.getLocalHost();
                System.out.println("<client ip=\"" + ip.getHostAddress() + "\" listenPort=\"" + port + "\" availableSlots=\"1\" />");
                LOGGER.log(Level.INFO, String.format("Client started on %s:%d", ip.getHostAddress(), port));
            } catch (UnknownHostException e) {
                System.out.println("Client started on port:" + port);
                LOGGER.log(Level.WARNING, "Could not determine local host address", e);
            }
        } catch (BindException ex) {
            if (retries < MAX_PORT_RETRIES) {
                retries++;
                LOGGER.log(Level.WARNING, String.format("Port %d in use, trying %d (retry %d/%d)",
                    port, port + retries, retries, MAX_PORT_RETRIES));
                initSocket(port + retries);
            } else {
                String error = String.format("Failed to bind to any port after %d retries", MAX_PORT_RETRIES);
                LOGGER.log(Level.SEVERE, error, ex);
                throw new RuntimeException(error, ex);
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during socket initialization", ex);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to create server socket on port " + port, e);
            throw new RuntimeException("Failed to create server socket", e);
        }
    }

    protected void startSimulation(Message m, Simulator sim) {
        simulating.set(true);
        try {
            clientSimulatorRunner = new ClientSimulatorRunner(m.getIndividual(), sim, m);
            clientSimulatorRunner.run();
        } finally {
            simulating.set(false);
        }
    }

    /**
     * Safely closes streams and socket, handling null values gracefully.
     * Never throws exceptions - logs warnings instead.
     */
    protected void closeAllConnections(ObjectInputStream inputStream, ObjectOutputStream outputStream, Socket socket) {
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Failed to close output stream: " + ex.getMessage(), ex);
            }
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Failed to close input stream: " + ex.getMessage(), ex);
            }
        }
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Failed to close socket: " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void run() {
        int receivedIndividuals = 0;
        LOGGER.log(Level.INFO, "IndividualReceiver started and listening for connections");

        while (running) {
            ObjectInputStream inputStream = null;
            ObjectOutputStream outputStream = null;
            Socket socket = null;
            Message receivedMessage = null;
            Simulator simulator = null;
            boolean isSimulationStarted = false;

            try {
                // Reset wait time at the start of each loop iteration
                connectionWaitStartTime.set(System.currentTimeMillis());

                // Accept connection (with timeout to check 'running' flag periodically)
                try {
                    socket = serverSocket.accept();
                } catch (SocketTimeoutException e) {
                    // Timeout on accept() - this is normal, just check if still running
                    continue;
                }

                // Set socket timeouts to prevent hanging
                socket.setSoTimeout(SOCKET_READ_TIMEOUT_MS);

                receivedIndividuals++;
                LOGGER.log(Level.INFO, String.format("Received connection #%d from %s",
                    receivedIndividuals, socket.getInetAddress().getHostAddress()));

                // Create streams (order matters: OutputStream first!)
                outputStream = new ObjectOutputStream(socket.getOutputStream());
                outputStream.flush();
                inputStream = new ObjectInputStream(socket.getInputStream());

                // Read and validate message
                Object messageObj = inputStream.readObject();
                if (!(messageObj instanceof Message)) {
                    LOGGER.log(Level.SEVERE, "Received invalid message type: " +
                        (messageObj != null ? messageObj.getClass().getName() : "null"));
                    continue;
                }

                receivedMessage = (Message) messageObj;

                // Validate message contents
                if (!validateMessage(receivedMessage)) {
                    LOGGER.log(Level.SEVERE, "Received invalid message (missing required fields)");
                    receivedMessage.setType(Message.TYPE_ERR_SIMULATOR_NOT_INSTALLED);
                    outputStream.writeObject(receivedMessage);
                    outputStream.flush();
                    continue;
                }

                // NOTE: DO NOT overwrite serverIP from the message!
                // The message already contains the correct server IP from fadseConfig.ini
                // Using socket.getInetAddress() here would break routing in distributed setups
                
                // Log the server IP for debugging
                LOGGER.log(Level.FINE, String.format(
                    "Will send results to server at %s:%d (connection from %s)",
                    receivedMessage.getServerIP() != null ? receivedMessage.getServerIP().getHostAddress() : "null",
                    receivedMessage.getServerListenPort(),
                    socket.getInetAddress().getHostAddress()
                ));

                // Process message based on type
                InputData inputData = receivedMessage.getIndividual().getInputData();

                // Replace placeholder '#' with unique ID for temporary files
                Map<String, String> problemParameters = (Map<String, String>) inputData.get(CommonSetupParameters.PROBLEM_CONFIG);
                if (problemParameters != null) {
                    for (String key : problemParameters.keySet()) {
                        String value = problemParameters.get(key);
                        if (value != null && value.contains("#")) {
                            String uniqueId = System.currentTimeMillis() + "_" + receivedMessage.getMessageId();
                            problemParameters.put(key, value.replace("#", uniqueId));
                        }
                    }
                }

                // Create simulator
                simulator = SimulatorFactory.createSimulator(inputData);

                if (simulator == null) {
                    // Simulator not installed on this client
                    LOGGER.log(Level.WARNING, "Simulator not found for this client");
                    receivedMessage.setType(Message.TYPE_ERR_SIMULATOR_NOT_INSTALLED);
                    outputStream.writeObject(receivedMessage);
                    outputStream.flush();

                } else if (receivedMessage.getType() == Message.TYPE_CLOSE_SIMULATION_REQUEST) {
                    // Close/cleanup simulation request
                    LOGGER.log(Level.INFO, "Received close simulation request");
                    simulator.closeSimulation(receivedMessage.getIndividual());
                    receivedMessage.setType(Message.TYPE_ACK);
                    outputStream.writeObject(receivedMessage);
                    outputStream.flush();

                } else if (receivedMessage.getType() == Message.TYPE_REQUEST) {
                    // Normal simulation request
                    Map<String, String> dbConnectionData = (Map<String, String>) inputData.get(CommonSetupParameters.DATABASE);
                    if (dbConnectionData != null) {
                        ConnectionPool.setInputDocument(dbConnectionData);
                    }

                    // Send ACK and close initial connection
                    receivedMessage.setType(Message.TYPE_ACK);
                    outputStream.writeObject(receivedMessage);
                    outputStream.flush();

                    isSimulationStarted = true;

                } else {
                    LOGGER.log(Level.WARNING, "Received message with unknown type: " + receivedMessage.getType());
                }

            } catch (SocketTimeoutException ex) {
                LOGGER.log(Level.WARNING, "Socket timeout while reading message", ex);
            } catch (IOException ex) {
                if (running) {  // Only log if not shutting down
                    LOGGER.log(Level.SEVERE, "IOException while processing connection", ex);
                }
            } catch (ClassNotFoundException ex) {
                LOGGER.log(Level.SEVERE, "ClassNotFoundException - protocol mismatch?", ex);
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Unexpected exception in IndividualReceiver", ex);
            } finally {
                // Always close the initial connection streams
                closeAllConnections(inputStream, outputStream, socket);
            }

            // Start simulation AFTER closing initial connection
            // (simulation will open its own connection to send results)
            if (isSimulationStarted && receivedMessage != null && simulator != null) {
                try {
                    LOGGER.log(Level.INFO, String.format("Starting simulation for individual %s (message %s)",
                        receivedMessage.getIndividual().hashCode(), receivedMessage.getMessageId()));

                    startSimulation(receivedMessage, simulator);

                    LOGGER.log(Level.INFO, "Simulation completed successfully");
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Exception during simulation", ex);
                }
            }
        }

        LOGGER.log(Level.INFO, "IndividualReceiver shutting down gracefully");
    }

    /**
     * Validates that a received message has all required fields.
     *
     * @param message The message to validate
     * @return true if valid, false otherwise
     */
    private boolean validateMessage(Message message) {
        if (message == null) {
            return false;
        }
        if (message.getMessageId() == null || message.getMessageId().trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Message missing messageId");
            return false;
        }
        if (message.getIndividual() == null) {
            LOGGER.log(Level.WARNING, "Message missing individual");
            return false;
        }
        if (message.getIndividual().getInputData() == null) {
            LOGGER.log(Level.WARNING, "Individual missing inputData");
            return false;
        }
        if (message.getServerIP() == null && message.getType() != Message.TYPE_CLOSE_SIMULATION_REQUEST) {
            // ServerIP will be set from socket, but validate other types
        }
        return true;
    }
}