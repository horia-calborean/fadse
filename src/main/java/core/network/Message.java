package core.network;

import core.model.individual.FadseIndividual;

import java.io.Serializable;
import java.net.InetAddress;

public class Message implements Serializable{
    public static int TYPE_REQUEST = 0;
    public static int TYPE_RESPONSE = 1;
    public static int TYPE_ACK = 2;
    public static int TYPE_ERR_SIMULATOR_NOT_INSTALLED = 3;
    public static int TYPE_CLOSE_SIMULATION_REQUEST = 4;
    private int type = 0;
    private FadseIndividual individual;
    private String messageId;
    private String simulatorName;
    private InetAddress serverIP;
    private int serverListenPort;
    private int clientListenPort;

    public FadseIndividual getIndividual() {
        return individual;
    }

    public void setIndividual(FadseIndividual individual) {
        this.individual = individual;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSimulatorName() {
        return simulatorName;
    }

    public void setSimulatorName(String simulatorName) {
        this.simulatorName = simulatorName;
    }

    public InetAddress getServerIP() {
        return serverIP;
    }

    public void setServerIP(InetAddress serverIP) {
        this.serverIP = serverIP;
    }

    public int getServerListenPort() {
        return serverListenPort;
    }

    public void setServerListenPort(int serverListenPort) {
        this.serverListenPort = serverListenPort;
    }

    public int getClientListenPort() {
        return clientListenPort;
    }

    public void setClientListenPort(int clientListenPort) {
        this.clientListenPort = clientListenPort;
    }
}