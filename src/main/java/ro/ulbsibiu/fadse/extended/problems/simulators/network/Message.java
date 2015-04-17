/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ro.ulbsibiu.fadse.extended.problems.simulators.network;

import java.io.Serializable;
import java.net.InetAddress;

import ro.ulbsibiu.fadse.environment.Individual;

/**
 *
 * @author Horia Calborean
 */
public class Message implements Serializable{
    public static int TYPE_REQUEST = 0;
    public static int TYPE_RESPONSE = 1;
    public static int TYPE_ACK = 2;
    public static int TYPE_ERR_SIMULATOR_NOT_INSTALLED = 3;
    public static int TYPE_CLOSE_SIMULATION_REQUEST = 4;
    private int type = 0;
    private Individual individual;
    private String messageId;
    private String simulatorName;
    private InetAddress serverIP;
    private int serverListenPort;
    private int clientListenport;

    public Individual getIndividual() {
        return individual;
    }

    public void setIndividual(Individual individual) {
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

    public int getClientListenport() {
        return clientListenport;
    }

    public void setClientListenport(int clientListenport) {
        this.clientListenport = clientListenport;
    }

   

    

}
