package core.network.client;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import core.model.individual.FadseIndividual;
import core.network.Message;

/**
 * ResultsSender - Sends simulation results from client back to server.
 *
 * Improvements:
 * - Iterative retries (no recursion/stack overflow risk)
 * - Proper resource management (no leaks)
 * - Exponential backoff between retries
 * - Configurable retry limits
 * - Socket timeout configuration
 */
public class ResultsSender {

    private static final Logger LOGGER = Logger.getLogger(ResultsSender.class.getName());

    // Configuration
    private static final int MAX_RETRIES = 5;
    private static final int INITIAL_RETRY_DELAY_MS = 1000;  // 1 second
    private static final int SOCKET_TIMEOUT_MS = 60000;      // 60 seconds
    private static final int CONNECTION_TIMEOUT_MS = 10000;  // 10 seconds

    /**
     * Sends simulation results to the server with automatic retry on failure.
     *
     * @param ind The individual with simulation results
     * @param m The message containing routing information
     * @throws IOException if all retry attempts fail
     */
    public void send(FadseIndividual ind, Message m) throws IOException {
        // Validate inputs
        if (ind == null) {
            throw new IllegalArgumentException("Individual cannot be null");
        }
        if (m == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }
        if (m.getServerIP() == null) {
            throw new IllegalArgumentException("Message missing server IP");
        }

        InetAddress address = m.getServerIP();
        int port = m.getServerListenPort();

        LOGGER.log(Level.INFO, String.format("Sending results to %s:%d (message %s)",
            address.getHostAddress(), port, m.getMessageId()));

        IOException lastException = null;

        // Retry loop (iterative, not recursive!)
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            Socket socket = null;
            ObjectOutputStream out = null;
            ObjectInputStream in = null;

            try {
                // Create connection
                socket = new Socket();
                socket.connect(new java.net.InetSocketAddress(address, port), CONNECTION_TIMEOUT_MS);
                socket.setSoTimeout(SOCKET_TIMEOUT_MS);

                // Prepare message
                m.setIndividual(ind);
                m.setType(Message.TYPE_RESPONSE);

                // Send message
                out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject(m);
                out.flush();

                LOGGER.log(Level.FINE, String.format("Sent results to %s:%d (attempt %d)",
                    address.getHostAddress(), port, attempt));

                // Wait for ACK
                in = new ObjectInputStream(socket.getInputStream());
                Object responseObj = in.readObject();

                if (!(responseObj instanceof Message)) {
                    LOGGER.log(Level.WARNING, String.format(
                        "Received invalid response type: %s (attempt %d/%d)",
                        responseObj != null ? responseObj.getClass().getName() : "null",
                        attempt, MAX_RETRIES
                    ));

                    if (attempt < MAX_RETRIES) {
                        Thread.sleep(INITIAL_RETRY_DELAY_MS * attempt);
                        continue;
                    } else {
                        throw new IOException("Invalid response type from server");
                    }
                }

                Message response = (Message) responseObj;

                // Validate ACK
                if (response.getType() == Message.TYPE_ACK &&
                    response.getMessageId() != null &&
                    response.getMessageId().equals(m.getMessageId())) {

                    LOGGER.log(Level.INFO, String.format("ACK received for message %s", m.getMessageId()));
                    return;  // Success!

                } else {
                    LOGGER.log(Level.WARNING, String.format(
                        "Invalid ACK: type=%d, messageId=%s (expected %s) - attempt %d/%d",
                        response.getType(), response.getMessageId(), m.getMessageId(),
                        attempt, MAX_RETRIES
                    ));

                    if (attempt < MAX_RETRIES) {
                        Thread.sleep(INITIAL_RETRY_DELAY_MS * attempt);
                        continue;
                    } else {
                        throw new IOException("Invalid ACK from server");
                    }
                }

            } catch (SocketTimeoutException ex) {
                lastException = ex;
                LOGGER.log(Level.WARNING, String.format(
                    "Timeout sending results to %s:%d (attempt %d/%d)",
                    address.getHostAddress(), port, attempt, MAX_RETRIES
                ), ex);

                if (attempt < MAX_RETRIES) {
                    try {
                        Thread.sleep(INITIAL_RETRY_DELAY_MS * attempt);  // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Interrupted during retry", ie);
                    }
                }

            } catch (EOFException ex) {
                lastException = ex;
                LOGGER.log(Level.WARNING, String.format(
                    "Connection closed by server %s:%d (attempt %d/%d)",
                    address.getHostAddress(), port, attempt, MAX_RETRIES
                ), ex);

                if (attempt < MAX_RETRIES) {
                    try {
                        Thread.sleep(INITIAL_RETRY_DELAY_MS * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Interrupted during retry", ie);
                    }
                }

            } catch (IOException ex) {
                lastException = ex;
                LOGGER.log(Level.SEVERE, String.format(
                    "IOException sending results to %s:%d (attempt %d/%d)",
                    address.getHostAddress(), port, attempt, MAX_RETRIES
                ), ex);

                if (attempt < MAX_RETRIES) {
                    try {
                        Thread.sleep(INITIAL_RETRY_DELAY_MS * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Interrupted during retry", ie);
                    }
                }

            } catch (ClassNotFoundException ex) {
                LOGGER.log(Level.SEVERE, "Protocol mismatch with server", ex);
                throw new IOException("Protocol mismatch with server", ex);

            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted during send", ex);

            } finally {
                // Always close resources (null-safe)
                closeQuietly(in);
                closeQuietly(out);
                closeQuietly(socket);
            }
        }

        // All retries exhausted
        String errorMsg = String.format(
            "Client failed to send results to server %s:%d after %d attempts",
            address.getHostAddress(), port, MAX_RETRIES
        );
        LOGGER.log(Level.SEVERE, errorMsg, lastException);
        throw new IOException(errorMsg, lastException);
    }

    /**
     * Closes a closeable resource without throwing exceptions.
     */
    private void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error closing resource: " + e.getMessage());
            }
        }
    }
}