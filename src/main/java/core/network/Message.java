package core.network;

import core.model.individual.FadseIndividual;

import java.io.Serial;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.Objects;

/**
 * Message class for communication between FADSE server and simulation clients.
 *
 * This class is serialized and sent over the network, so it must maintain
 * backward compatibility. Any changes to field structure should increment
 * the serialVersionUID.
 *
 * Message types:
 * - TYPE_REQUEST: Request to simulate an individual
 * - TYPE_RESPONSE: Simulation results from client
 * - TYPE_ACK: Acknowledgment of received message
 * - TYPE_ERR_SIMULATOR_NOT_INSTALLED: Error - simulator not available on client
 * - TYPE_CLOSE_SIMULATION_REQUEST: Request to terminate a simulation
 *
 * @author FADSE Development Team
 */
public class Message implements Serializable {

    // Critical: serialVersionUID for version compatibility
    // Change this if you modify the class structure (add/remove/change fields)
    @Serial
    private static final long serialVersionUID = 1L;

    // Message type constants (final to prevent modification)
    public static final int TYPE_REQUEST = 0;
    public static final int TYPE_RESPONSE = 1;
    public static final int TYPE_ACK = 2;
    public static final int TYPE_ERR_SIMULATOR_NOT_INSTALLED = 3;
    public static final int TYPE_CLOSE_SIMULATION_REQUEST = 4;

    // Valid port range
    private static final int MIN_PORT = 1;
    private static final int MAX_PORT = 65535;

    // Message fields
    private int type = TYPE_REQUEST;
    private FadseIndividual individual;
    private String messageId;
    private String simulatorName;
    private InetAddress serverIP;
    private int serverListenPort;
    private int clientListenPort;

    /**
     * Default constructor.
     */
    public Message() {
    }

    /**
     * Gets the individual to be simulated.
     *
     * @return The individual, may be null
     */
    public FadseIndividual getIndividual() {
        return individual;
    }

    /**
     * Sets the individual to be simulated.
     *
     * @param individual The individual to simulate
     * @throws IllegalArgumentException if individual is null for REQUEST or CLOSE types
     */
    public void setIndividual(FadseIndividual individual) {
        // Validate based on message type
        if ((type == TYPE_REQUEST || type == TYPE_CLOSE_SIMULATION_REQUEST) && individual == null) {
            throw new IllegalArgumentException("Individual cannot be null for request messages");
        }
        this.individual = individual;
    }

    /**
     * Gets the message type.
     *
     * @return The message type (one of TYPE_* constants)
     */
    public int getType() {
        return type;
    }

    /**
     * Sets the message type.
     *
     * @param type The message type
     * @throws IllegalArgumentException if type is not a valid message type
     */
    public void setType(int type) {
        if (type < TYPE_REQUEST || type > TYPE_CLOSE_SIMULATION_REQUEST) {
            throw new IllegalArgumentException(
                    String.format("Invalid message type: %d. Must be between %d and %d",
                            type, TYPE_REQUEST, TYPE_CLOSE_SIMULATION_REQUEST)
            );
        }
        this.type = type;
    }

    /**
     * Gets the unique message identifier.
     *
     * @return The message ID
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Sets the unique message identifier.
     *
     * @param messageId The message ID
     * @throws IllegalArgumentException if messageId is null or empty
     */
    public void setMessageId(String messageId) {
        if (messageId == null || messageId.trim().isEmpty()) {
            throw new IllegalArgumentException("Message ID cannot be null or empty");
        }
        this.messageId = messageId;
    }

    /**
     * Gets the simulator name.
     *
     * @return The simulator name (e.g., "gem5", "sniper")
     */
    public String getSimulatorName() {
        return simulatorName;
    }

    /**
     * Sets the simulator name.
     *
     * @param simulatorName The simulator name
     * @throws IllegalArgumentException if simulatorName is null or empty for REQUEST types
     */
    public void setSimulatorName(String simulatorName) {
        if (type == TYPE_REQUEST && (simulatorName == null || simulatorName.trim().isEmpty())) {
            throw new IllegalArgumentException("Simulator name cannot be null or empty for request messages");
        }
        this.simulatorName = simulatorName;
    }

    /**
     * Gets the server IP address.
     *
     * @return The server IP address
     */
    public InetAddress getServerIP() {
        return serverIP;
    }

    /**
     * Sets the server IP address.
     *
     * @param serverIP The server IP address
     * @throws IllegalArgumentException if serverIP is null
     */
    public void setServerIP(InetAddress serverIP) {
        if (serverIP == null) {
            throw new IllegalArgumentException("Server IP cannot be null");
        }
        this.serverIP = serverIP;
    }

    /**
     * Gets the server listen port.
     *
     * @return The server port number
     */
    public int getServerListenPort() {
        return serverListenPort;
    }

    /**
     * Sets the server listen port.
     *
     * @param serverListenPort The server port number
     * @throws IllegalArgumentException if port is invalid (not in range 1-65535)
     */
    public void setServerListenPort(int serverListenPort) {
        validatePort(serverListenPort, "Server listen port");
        this.serverListenPort = serverListenPort;
    }

    /**
     * Gets the client listen port.
     *
     * @return The client port number
     */
    public int getClientListenPort() {
        return clientListenPort;
    }

    /**
     * Sets the client listen port.
     *
     * @param clientListenPort The client port number
     * @throws IllegalArgumentException if port is invalid (not in range 1-65535)
     */
    public void setClientListenPort(int clientListenPort) {
        validatePort(clientListenPort, "Client listen port");
        this.clientListenPort = clientListenPort;
    }

    /**
     * Validates a port number.
     *
     * @param port The port to validate
     * @param fieldName The field name for error messages
     * @throws IllegalArgumentException if port is invalid
     */
    private void validatePort(int port, String fieldName) {
        if (port < MIN_PORT || port > MAX_PORT) {
            throw new IllegalArgumentException(
                    String.format("%s must be between %d and %d, got: %d",
                            fieldName, MIN_PORT, MAX_PORT, port)
            );
        }
    }

    /**
     * Validates that the message is complete and ready to send.
     *
     * @throws IllegalStateException if required fields are missing
     */
    public void validate() {
        if (messageId == null || messageId.trim().isEmpty()) {
            throw new IllegalStateException("Message ID is required");
        }

        if (type == TYPE_REQUEST || type == TYPE_CLOSE_SIMULATION_REQUEST) {
            if (individual == null) {
                throw new IllegalStateException("Individual is required for request messages");
            }
            if (simulatorName == null || simulatorName.trim().isEmpty()) {
                throw new IllegalStateException("Simulator name is required for request messages");
            }
            if (serverIP == null) {
                throw new IllegalStateException("Server IP is required for request messages");
            }
            if (serverListenPort < MIN_PORT || serverListenPort > MAX_PORT) {
                throw new IllegalStateException("Valid server listen port is required");
            }
        }
    }

    /**
     * Returns a string representation of the message type.
     *
     * @return Human-readable message type
     */
    public String getTypeAsString() {
        switch (type) {
            case TYPE_REQUEST:
                return "REQUEST";
            case TYPE_RESPONSE:
                return "RESPONSE";
            case TYPE_ACK:
                return "ACK";
            case TYPE_ERR_SIMULATOR_NOT_INSTALLED:
                return "ERR_SIMULATOR_NOT_INSTALLED";
            case TYPE_CLOSE_SIMULATION_REQUEST:
                return "CLOSE_SIMULATION_REQUEST";
            default:
                return "UNKNOWN(" + type + ")";
        }
    }

    @Override
    public String toString() {
        return String.format(
                "Message{type=%s(%d), messageId='%s', simulator='%s', " +
                        "individual=%s, serverIP=%s, serverPort=%d, clientPort=%d}",
                getTypeAsString(), type, messageId, simulatorName,
                individual != null ? individual.hashCode() : "null",
                serverIP != null ? serverIP.getHostAddress() : "null",
                serverListenPort, clientListenPort
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        return type == message.type &&
                serverListenPort == message.serverListenPort &&
                clientListenPort == message.clientListenPort &&
                Objects.equals(individual, message.individual) &&
                Objects.equals(messageId, message.messageId) &&
                Objects.equals(simulatorName, message.simulatorName) &&
                Objects.equals(serverIP, message.serverIP);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, individual, messageId, simulatorName,
                serverIP, serverListenPort, clientListenPort);
    }

    /**
     * Creates a builder for fluent message construction.
     *
     * @return A new MessageBuilder instance
     */
    public static MessageBuilder builder() {
        return new MessageBuilder();
    }

    /**
     * Builder class for fluent message construction.
     *
     * Example usage:
     * <pre>
     * Message msg = Message.builder()
     *     .type(Message.TYPE_REQUEST)
     *     .messageId("msg-123")
     *     .individual(individual)
     *     .simulatorName("gem5")
     *     .serverIP(InetAddress.getLocalHost())
     *     .serverListenPort(8080)
     *     .clientListenPort(9090)
     *     .build();
     * </pre>
     */
    public static class MessageBuilder {
        private final Message message;

        private MessageBuilder() {
            this.message = new Message();
        }

        public MessageBuilder type(int type) {
            message.setType(type);
            return this;
        }

        public MessageBuilder individual(FadseIndividual individual) {
            message.individual = individual;
            return this;
        }

        public MessageBuilder messageId(String messageId) {
            message.setMessageId(messageId);
            return this;
        }

        public MessageBuilder simulatorName(String simulatorName) {
            message.simulatorName = simulatorName;
            return this;
        }

        public MessageBuilder serverIP(InetAddress serverIP) {
            message.setServerIP(serverIP);
            return this;
        }

        public MessageBuilder serverListenPort(int port) {
            message.setServerListenPort(port);
            return this;
        }

        public MessageBuilder clientListenPort(int port) {
            message.setClientListenPort(port);
            return this;
        }

        public Message build() {
            message.validate();
            return message;
        }
    }
}