package core.network;

import core.model.clients.FadseClientData;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Network diagnostics and health monitoring for FADSE distributed simulation.
 *
 * Features:
 * - Tracks success/failure rates per client
 * - Circuit breaker pattern to avoid hammering dead clients
 * - Network statistics and monitoring
 * - Health checks
 *
 * @author FADSE Development Team
 */
public class NetworkDiagnostics {

    private static final Logger LOGGER = Logger.getLogger(NetworkDiagnostics.class.getName());

    // Singleton instance
    private static volatile NetworkDiagnostics instance;

    // Circuit breaker configuration
    private static final int FAILURE_THRESHOLD = 5; // Failures before opening circuit
    private static final long CIRCUIT_OPEN_TIME_MS = 60000; // 1 minute
    private static final int SUCCESS_THRESHOLD = 2; // Successes to close circuit

    // Client health tracking
    private final Map<String, ClientHealth> clientHealth;

    // Global statistics
    private final AtomicLong totalMessagesSent = new AtomicLong(0);
    private final AtomicLong totalMessagesFailed = new AtomicLong(0);
    private final AtomicLong totalMessagesReceived = new AtomicLong(0);
    private final AtomicLong totalBytesReceived = new AtomicLong(0);
    private final AtomicLong totalConnectionErrors = new AtomicLong(0);
    private final AtomicLong totalTimeouts = new AtomicLong(0);

    private NetworkDiagnostics() {
        this.clientHealth = new ConcurrentHashMap<>();
    }

    public static NetworkDiagnostics getInstance() {
        if (instance == null) {
            synchronized (NetworkDiagnostics.class) {
                if (instance == null) {
                    instance = new NetworkDiagnostics();
                }
            }
        }
        return instance;
    }

    /**
     * Records a successful message send to a client
     */
    public void recordSuccess(FadseClientData client) {
        if (client == null) return;

        String key = getClientKey(client);
        ClientHealth health = clientHealth.computeIfAbsent(key, k -> new ClientHealth(client));
        health.recordSuccess();
        totalMessagesSent.incrementAndGet();
    }

    /**
     * Records a failed message send to a client
     */
    public void recordFailure(FadseClientData client, FailureType type) {
        if (client == null) return;

        String key = getClientKey(client);
        ClientHealth health = clientHealth.computeIfAbsent(key, k -> new ClientHealth(client));
        health.recordFailure(type);
        totalMessagesFailed.incrementAndGet();

        if (type == FailureType.TIMEOUT) {
            totalTimeouts.incrementAndGet();
        } else if (type == FailureType.CONNECTION_REFUSED || type == FailureType.UNREACHABLE) {
            totalConnectionErrors.incrementAndGet();
        }
    }

    /**
     * Records a received message
     */
    public void recordReceived(long bytes) {
        totalMessagesReceived.incrementAndGet();
        totalBytesReceived.addAndGet(bytes);
    }

    /**
     * Checks if a client is healthy enough to send messages to.
     * Implements circuit breaker pattern.
     */
    public boolean isClientHealthy(FadseClientData client) {
        if (client == null) return false;

        String key = getClientKey(client);
        ClientHealth health = clientHealth.get(key);

        if (health == null) {
            return true; // Unknown client, assume healthy
        }

        return health.isHealthy();
    }

    /**
     * Gets the health status of a client
     */
    public ClientHealth getClientHealth(FadseClientData client) {
        if (client == null) return null;
        return clientHealth.get(getClientKey(client));
    }

    /**
     * Performs a quick health check on a client (ping)
     */
    public boolean performHealthCheck(FadseClientData client, int timeoutMs) {
        try {
            InetAddress address = client.getIP();

            // Try ICMP ping first (requires elevated privileges)
            if (address.isReachable(timeoutMs)) {
                return true;
            }

            // Fallback: try TCP connection
            try (Socket socket = new Socket()) {
                socket.connect(new java.net.InetSocketAddress(address, client.getPort()), timeoutMs);
                return socket.isConnected();
            }

        } catch (IOException e) {
            LOGGER.log(Level.FINE, String.format(
                "Health check failed for %s:%d",
                client.getIP().getHostAddress(), client.getPort()
            ), e);
            return false;
        }
    }

    /**
     * Gets global network statistics
     */
    public NetworkStats getGlobalStats() {
        return new NetworkStats(
            totalMessagesSent.get(),
            totalMessagesFailed.get(),
            totalMessagesReceived.get(),
            totalBytesReceived.get(),
            totalConnectionErrors.get(),
            totalTimeouts.get()
        );
    }

    /**
     * Gets a summary of all client health
     */
    public String getHealthSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Network Health Summary ===\n");
        sb.append(String.format("Total Messages Sent: %d\n", totalMessagesSent.get()));
        sb.append(String.format("Total Messages Failed: %d\n", totalMessagesFailed.get()));
        sb.append(String.format("Total Messages Received: %d\n", totalMessagesReceived.get()));
        sb.append(String.format("Total Bytes Received: %d (%.2f MB)\n",
            totalBytesReceived.get(), totalBytesReceived.get() / (1024.0 * 1024.0)));
        sb.append(String.format("Total Connection Errors: %d\n", totalConnectionErrors.get()));
        sb.append(String.format("Total Timeouts: %d\n", totalTimeouts.get()));

        double successRate = totalMessagesSent.get() > 0
            ? (totalMessagesSent.get() - totalMessagesFailed.get()) * 100.0 / totalMessagesSent.get()
            : 100.0;
        sb.append(String.format("Overall Success Rate: %.2f%%\n\n", successRate));

        sb.append("=== Per-Client Health ===\n");
        clientHealth.forEach((key, health) -> {
            sb.append(String.format("%s: %s\n", key, health.toString()));
        });

        return sb.toString();
    }

    /**
     * Resets all statistics
     */
    public void reset() {
        clientHealth.clear();
        totalMessagesSent.set(0);
        totalMessagesFailed.set(0);
        totalMessagesReceived.set(0);
        totalBytesReceived.set(0);
        totalConnectionErrors.set(0);
        totalTimeouts.set(0);
    }

    private String getClientKey(FadseClientData client) {
        return client.getIP().getHostAddress() + ":" + client.getPort();
    }

    /**
     * Failure types for categorization
     */
    public enum FailureType {
        TIMEOUT,
        CONNECTION_REFUSED,
        UNREACHABLE,
        PROTOCOL_ERROR,
        OTHER
    }

    /**
     * Circuit breaker states
     */
    public enum CircuitState {
        CLOSED,      // Normal operation
        OPEN,        // Too many failures, blocking requests
        HALF_OPEN    // Testing if service recovered
    }

    /**
     * Health information for a single client
     */
    public static class ClientHealth {
        private final FadseClientData client;
        private final AtomicInteger successCount = new AtomicInteger(0);
        private final AtomicInteger failureCount = new AtomicInteger(0);
        private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
        private final Map<FailureType, AtomicInteger> failuresByType = new ConcurrentHashMap<>();

        private volatile CircuitState state = CircuitState.CLOSED;
        private volatile long circuitOpenedAt = 0;

        public ClientHealth(FadseClientData client) {
            this.client = client;
            for (FailureType type : FailureType.values()) {
                failuresByType.put(type, new AtomicInteger(0));
            }
        }

        public void recordSuccess() {
            successCount.incrementAndGet();
            consecutiveFailures.set(0);

            // If in HALF_OPEN state and got enough successes, close circuit
            if (state == CircuitState.HALF_OPEN) {
                if (successCount.get() >= SUCCESS_THRESHOLD) {
                    state = CircuitState.CLOSED;
                    LOGGER.log(Level.INFO, String.format(
                        "Circuit CLOSED for client %s:%d (recovered)",
                        client.getIP().getHostAddress(), client.getPort()
                    ));
                }
            }
        }

        public void recordFailure(FailureType type) {
            failureCount.incrementAndGet();
            int consecutive = consecutiveFailures.incrementAndGet();
            failuresByType.get(type).incrementAndGet();

            // Open circuit if too many consecutive failures
            if (consecutive >= FAILURE_THRESHOLD && state == CircuitState.CLOSED) {
                state = CircuitState.OPEN;
                circuitOpenedAt = System.currentTimeMillis();
                LOGGER.log(Level.WARNING, String.format(
                    "Circuit OPEN for client %s:%d after %d consecutive failures",
                    client.getIP().getHostAddress(), client.getPort(), consecutive
                ));
            }
        }

        public boolean isHealthy() {
            long now = System.currentTimeMillis();

            switch (state) {
                case CLOSED:
                    return true;

                case OPEN:
                    // Check if enough time has passed to try again
                    if (now - circuitOpenedAt > CIRCUIT_OPEN_TIME_MS) {
                        state = CircuitState.HALF_OPEN;
                        consecutiveFailures.set(0);
                        LOGGER.log(Level.INFO, String.format(
                            "Circuit HALF_OPEN for client %s:%d (testing recovery)",
                            client.getIP().getHostAddress(), client.getPort()
                        ));
                        return true;
                    }
                    return false;

                case HALF_OPEN:
                    return true; // Allow limited requests to test

                default:
                    return true;
            }
        }

        public CircuitState getState() {
            return state;
        }

        public int getSuccessCount() {
            return successCount.get();
        }

        public int getFailureCount() {
            return failureCount.get();
        }

        public double getSuccessRate() {
            int total = successCount.get() + failureCount.get();
            return total > 0 ? (successCount.get() * 100.0 / total) : 100.0;
        }

        @Override
        public String toString() {
            return String.format(
                "State=%s, Success=%d, Failures=%d (consecutive=%d), Rate=%.2f%%",
                state, successCount.get(), failureCount.get(),
                consecutiveFailures.get(), getSuccessRate()
            );
        }
    }

    /**
     * Global network statistics
     */
    public static class NetworkStats {
        public final long messagesSent;
        public final long messagesFailed;
        public final long messagesReceived;
        public final long bytesReceived;
        public final long connectionErrors;
        public final long timeouts;

        public NetworkStats(long sent, long failed, long received, long bytes,
                          long connErrors, long timeouts) {
            this.messagesSent = sent;
            this.messagesFailed = failed;
            this.messagesReceived = received;
            this.bytesReceived = bytes;
            this.connectionErrors = connErrors;
            this.timeouts = timeouts;
        }

        public double getSuccessRate() {
            return messagesSent > 0
                ? (messagesSent - messagesFailed) * 100.0 / messagesSent
                : 100.0;
        }
    }
}

