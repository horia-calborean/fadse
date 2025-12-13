package core.network;

import core.model.clients.FadseClientData;
import core.model.clients.ListOfFadseClients;
import org.ini4j.Wini;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Improved ResultsReceiver with critical bug fixes:
 * - Thread pool instead of unbounded thread creation
 * - Proper resource management (no socket/stream leaks)
 * - Thread-safe singleton pattern
 * - Graceful shutdown support
 * - Cached configuration
 * - Better exception handling
 * - Encapsulated results list
 *
 * This class runs as a server that receives simulation results from distributed clients.
 * It uses a fixed thread pool to handle incoming connections efficiently without
 * creating unbounded threads.
 */
public class ResultsReceiver implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ResultsReceiver.class.getName());

    // Configuration
    private static final int THREAD_POOL_SIZE =
            Integer.getInteger("fadse.results.threadpool.size", 10);
    private static final int SHUTDOWN_TIMEOUT_SECONDS = 30;
    private static final int SOCKET_TIMEOUT_MS = 5000;  // 5 second timeout for accept

    // Singleton instance
    private static volatile ResultsReceiver instance;

    // Server components
    private final ServerSocket serverSocket;
    private final ExecutorService threadPool;
    private final List<Message> results;
    private final SimulationStatus simulationStatus;

    // Shutdown control
    private volatile boolean running = true;

    /**
     * Private constructor - use getInstance() instead.
     *
     * @throws IOException if server socket cannot be created
     */
    private ResultsReceiver() throws IOException {
        this.simulationStatus = SimulationStatus.getInstance();
        this.results = Collections.synchronizedList(new LinkedList<>());

        // Create thread pool with fixed size (prevents unbounded thread creation)
        this.threadPool = Executors.newFixedThreadPool(
                THREAD_POOL_SIZE,
                r -> {
                    Thread t = new Thread(r, "ResultsReceiver-Worker");
                    t.setDaemon(true);  // Don't prevent JVM shutdown
                    return t;
                }
        );

        // Load configuration and create server socket
        int listenPort = loadListenPort();
        this.serverSocket = new ServerSocket(listenPort);
        this.serverSocket.setSoTimeout(SOCKET_TIMEOUT_MS);  // Timeout for accept()

        LOGGER.log(Level.CONFIG, String.format(
                "ResultsReceiver initialized: port=%d, threadPool=%d threads",
                listenPort, THREAD_POOL_SIZE
        ));

        // Register shutdown hook for clean shutdown on JVM exit
        registerShutdownHook();
    }

    /**
     * Gets the singleton instance of ResultsReceiver.
     * Thread-safe using double-checked locking.
     *
     * @return The ResultsReceiver instance
     * @throws IOException if initialization fails
     */
    public static ResultsReceiver getInstance() throws IOException {
        if (instance == null) {
            synchronized (ResultsReceiver.class) {
                if (instance == null) {
                    instance = new ResultsReceiver();

                    // Start receiver thread
                    Thread receiverThread = new Thread(instance, "ResultsReceiver-Main");
                    receiverThread.setDaemon(true);
                    receiverThread.start();

                    LOGGER.log(Level.INFO, "ResultsReceiver singleton created and started");
                }
            }
        }
        return instance;
    }

    /**
     * Loads the listen port from configuration file.
     *
     * @return The listen port number
     * @throws IOException if configuration cannot be loaded
     */
    private int loadListenPort() throws IOException {
        String currentDir = System.getProperty("user.dir");
        File configFile = new File(currentDir, "configs" + File.separator + "fadseConfig.ini");

        if (!configFile.exists()) {
            throw new IOException("Configuration file not found: " + configFile.getAbsolutePath());
        }

        try {
            Wini ini = new Wini(configFile);
            Integer port = ini.get("Server", "listenPort", Integer.class);

            if (port == null) {
                throw new IOException("Server listen port not configured in fadseConfig.ini");
            }

            if (port < 1 || port > 65535) {
                throw new IOException("Invalid listen port: " + port);
            }

            return port;
        } catch (IOException e) {
            throw new IOException("Failed to load server configuration: " + e.getMessage(), e);
        }
    }

    /**
     * Main server loop - accepts incoming connections and handles them in thread pool.
     */
    @Override
    public void run() {
        LOGGER.log(Level.INFO, "ResultsReceiver thread started, waiting for client responses...");

        int consecutiveErrors = 0;
        final int MAX_CONSECUTIVE_ERRORS = 10;

        while (running) {
            try {
                // Accept incoming connection (with timeout)
                Socket socket = serverSocket.accept();

                // Reset error counter on successful accept
                consecutiveErrors = 0;

                // Validate socket before submitting
                if (socket != null && socket.isConnected()) {
                    // Submit to thread pool instead of creating new thread
                    threadPool.submit(() -> handleResponse(socket));
                } else {
                    LOGGER.log(Level.WARNING, "Received invalid socket connection");
                    closeQuietly(socket);
                }

            } catch (SocketTimeoutException e) {
                // Timeout is normal - allows checking 'running' flag
                consecutiveErrors = 0; // Reset on timeout (normal operation)
                continue;
            } catch (IOException e) {
                if (running) {
                    consecutiveErrors++;
                    LOGGER.log(Level.SEVERE, String.format(
                        "Error accepting connection (consecutive errors: %d/%d)",
                        consecutiveErrors, MAX_CONSECUTIVE_ERRORS), e);

                    // Exponential backoff on repeated errors
                    long backoffTime = Math.min(1000L * (1L << Math.min(consecutiveErrors, 5)), 30000L);
                    sleepQuietly(backoffTime);

                    // If too many consecutive errors, something is seriously wrong
                    if (consecutiveErrors >= MAX_CONSECUTIVE_ERRORS) {
                        LOGGER.log(Level.SEVERE, "Too many consecutive accept errors, shutting down receiver");
                        running = false;
                    }
                }
            }
        }

        LOGGER.log(Level.INFO, "ResultsReceiver thread stopped");
    }

    /**
     * Handles a single response from a client.
     * Uses proper resource management to prevent leaks.
     *
     * @param socket The client socket
     */
    private void handleResponse(Socket socket) {
        ObjectOutputStream out = null;
        ObjectInputStream in = null;

        try {
            // Set socket timeout to prevent hanging on slow/dead clients
            socket.setSoTimeout(60000); // 60 seconds - same as READ_TIMEOUT_MS in MessageSender

            // Validate socket is still connected
            if (!socket.isConnected() || socket.isClosed()) {
                LOGGER.log(Level.WARNING, "Socket is not connected or already closed");
                return;
            }

            // Create streams
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            // Read response message
            Message response = (Message) in.readObject();

            // Validate response is not null
            if (response == null) {
                LOGGER.log(Level.SEVERE, String.format(
                        "Received null message from %s", socket.getInetAddress()
                ));
                return;
            }

            // Validate message ID
            if (response.getMessageId() == null || response.getMessageId().trim().isEmpty()) {
                LOGGER.log(Level.SEVERE, String.format(
                        "Received message without ID from %s", socket.getInetAddress()
                ));
                return;
            }

            // Validate message type
            if (response.getType() != Message.TYPE_RESPONSE) {
                LOGGER.log(Level.SEVERE, String.format(
                        "Received message from %s but type is %s (expected RESPONSE)",
                        socket.getInetAddress(), response.getTypeAsString()
                ));
                return;
            }

            // Validate individual exists in response
            if (response.getIndividual() == null) {
                LOGGER.log(Level.SEVERE, String.format(
                        "Received response without individual from %s (messageId: %s)",
                        socket.getInetAddress(), response.getMessageId()
                ));
                return;
            }

            // CRITICAL: Send ACK back to client IMMEDIATELY before processing
            // This prevents client timeout during potentially slow processing
            response.setType(Message.TYPE_ACK);
            out.writeObject(response);
            out.flush();

            LOGGER.log(Level.FINE, String.format(
                    "Sent ACK to client %s:%d for message %s",
                    socket.getInetAddress().getHostAddress(),
                    response.getClientListenPort(),
                    response.getMessageId()
            ));

            // Now process response asynchronously (won't block client)
            processResponse(response, socket);

        } catch (java.net.SocketTimeoutException e) {
            LOGGER.log(Level.WARNING, String.format(
                    "Timeout reading response from %s (client may be hung or slow)",
                    socket.getInetAddress()
            ), e);
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, String.format(
                    "Invalid message format from %s (incompatible protocol version?)",
                    socket.getInetAddress()
            ), e);
        } catch (java.io.EOFException e) {
            LOGGER.log(Level.WARNING, String.format(
                    "Connection closed prematurely by %s (client crash?)",
                    socket.getInetAddress()
            ), e);
        } catch (java.net.SocketException e) {
            LOGGER.log(Level.WARNING, String.format(
                    "Socket error with %s (connection reset or broken pipe?)",
                    socket.getInetAddress()
            ), e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, String.format(
                    "I/O error handling response from %s: %s",
                    socket.getInetAddress(), e.getMessage()
            ), e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, String.format(
                    "Unexpected error handling response from %s", socket.getInetAddress()
            ), e);
        } finally {
            // Properly close all resources (null-safe, never throws)
            closeQuietly(in);
            closeQuietly(out);
            closeQuietly(socket);
        }
    }

    /**
     * Processes a response message.
     *
     * @param response The response message
     * @param socket The client socket
     */
    private void processResponse(Message response, Socket socket) {
        // Defensive: Verify response and socket are valid
        if (response == null) {
            LOGGER.log(Level.SEVERE, "Cannot process null response");
            return;
        }

        if (socket == null || socket.getInetAddress() == null) {
            LOGGER.log(Level.SEVERE, "Cannot process response with null socket");
            return;
        }

        // Add to results - synchronized for thread safety
        synchronized (results) {
            results.add(response);
        }

        // Track in network diagnostics
        // Estimate message size (rough approximation)
        long estimatedSize = 1024; // Default 1KB
        if (response.getIndividual() != null && response.getIndividual().getObjectives() != null) {
            estimatedSize += response.getIndividual().getObjectives().size() * 64;
        }
        NetworkDiagnostics.getInstance().recordReceived(estimatedSize);

        // Remove from active simulations
        try {
            simulationStatus.removeSimulation(response.getMessageId());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, String.format(
                "Error removing simulation %s from status", response.getMessageId()), e);
        }

        // Log receipt
        try {
            int activeSimulations = simulationStatus.getNumberOfActiveSimulations();
            LOGGER.log(Level.INFO, String.format(
                    "Received result: results=%d, messageId=%s, from=%s:%d, active=%d",
                    results.size(),
                    response.getMessageId(),
                    socket.getInetAddress().getHostAddress(),
                    response.getClientListenPort(),
                    activeSimulations
            ));

            if (response.getIndividual() != null) {
                LOGGER.log(Level.FINE, String.format(
                        "Individual hash: %d, feasible: %b",
                        response.getIndividual().hashCode(),
                        response.getIndividual().isFeasible()
                ));
            } else {
                LOGGER.log(Level.WARNING, String.format(
                        "Received response without individual (messageId: %s)",
                        response.getMessageId()
                ));
            }
        } catch (Exception e) {
            // Log but don't fail - might be concurrent modification
            LOGGER.log(Level.FINE, "Error logging receipt information", e);
        }

        // Update client slot count
        try {
            updateClientSlots(socket, response);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error updating client slots", e);
        }
    }

    /**
     * Updates the client's occupied slot count.
     *
     * @param socket The client socket
     * @param response The response message
     */
    private void updateClientSlots(Socket socket, Message response) {
        try {
            ListOfFadseClients fadseClients = ListOfFadseClients.getInstance();
            FadseClientData client = fadseClients.getByIpAndPort(
                    socket.getInetAddress(),
                    response.getClientListenPort()
            );

            if (client != null) {
                int occupiedSlots = client.getNumberOfOccupiedSlots();
                if (occupiedSlots > 0) {
                    client.setNumberOfOccupiedSlots(occupiedSlots - 1);
                    LOGGER.log(Level.FINER, String.format(
                            "Client %s:%d now has %d occupied slots",
                            socket.getInetAddress().getHostAddress(),
                            response.getClientListenPort(),
                            occupiedSlots - 1
                    ));
                } else {
                    LOGGER.log(Level.WARNING, String.format(
                            "Client %s:%d slot count already at 0",
                            socket.getInetAddress().getHostAddress(),
                            response.getClientListenPort()
                    ));
                }
            } else {
                LOGGER.log(Level.WARNING, String.format(
                        "Received result from unknown client: %s:%d",
                        socket.getInetAddress().getHostAddress(),
                        response.getClientListenPort()
                ));
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error updating client slots", e);
        }
    }

    /**
     * Gets a copy of the results list.
     * Thread-safe - returns a copy to prevent concurrent modification.
     *
     * @return Copy of the results list
     */
    public List<Message> getResults() {
        synchronized (results) {
            return new LinkedList<>(results);
        }
    }

    /**
     * Clears all results.
     * Thread-safe.
     */
    public void clearResults() {
        synchronized (results) {
            results.clear();
        }
        LOGGER.log(Level.FINE, "Results list cleared");
    }

    /**
     * Gets the number of results received.
     * Thread-safe.
     *
     * @return Number of results
     */
    public int getResultsCount() {
        return results.size();
    }

    /**
     * Initiates graceful shutdown of the ResultsReceiver.
     * Stops accepting new connections and waits for active handlers to complete.
     */
    public void shutdown() {
        LOGGER.log(Level.INFO, "Initiating ResultsReceiver shutdown...");

        running = false;

        // Close server socket to unblock accept()
        closeQuietly(serverSocket);

        // Shutdown thread pool
        threadPool.shutdown();

        try {
            if (!threadPool.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                LOGGER.log(Level.WARNING,
                        "Thread pool did not terminate within timeout, forcing shutdown");
                threadPool.shutdownNow();

                // Wait again for forced shutdown
                if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                    LOGGER.log(Level.SEVERE, "Thread pool did not terminate after forced shutdown");
                }
            }
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "Interrupted during shutdown", e);
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        LOGGER.log(Level.INFO, "ResultsReceiver shutdown complete");
    }

    /**
     * Checks if the receiver is running.
     *
     * @return true if running, false if shut down
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Closes a resource quietly (no exception thrown).
     *
     * @param closeable The resource to close
     */
    private void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                LOGGER.log(Level.FINE, "Error closing resource: " + e.getMessage());
            }
        }
    }

    /**
     * Sleeps quietly (no exception thrown).
     *
     * @param millis Milliseconds to sleep
     */
    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Resets the singleton instance (for testing only).
     */
    static void resetInstance() {
        if (instance != null) {
            instance.shutdown();
            instance = null;
        }
    }

    /**
     * Registers a shutdown hook to ensure clean shutdown on JVM exit
     */
    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.log(Level.INFO, "JVM shutting down, closing ResultsReceiver");
            shutdown();
        }, "ResultsReceiver-ShutdownHook"));
    }

    /**
     * Gets the port the receiver is listening on
     */
    public int getListenPort() {
        return serverSocket != null ? serverSocket.getLocalPort() : -1;
    }

    /**
     * Checks if the receiver has been shut down
     */
    public boolean isShutdown() {
        return !running || threadPool.isShutdown();
    }
}

