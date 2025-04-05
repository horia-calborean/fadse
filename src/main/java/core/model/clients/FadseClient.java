package core.model.clients;

import java.io.Serializable;
import java.net.InetAddress;

public class FadseClient implements Serializable {
    protected InetAddress ip;
    protected int port;
    protected int numberOfSlots;
    protected int numberOfOccupiedSlots;

    public InetAddress getIP() {
        return ip;
    }

    public void setIP(InetAddress ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getNumberOfOccupiedSlots() {
        return numberOfOccupiedSlots;
    }

    public void setNumberOfOccupiedSlots(int numberOfOccupiedSlots) {
        this.numberOfOccupiedSlots = numberOfOccupiedSlots;
    }

    public int getNumberOfSlots() {
        return numberOfSlots;
    }

    public void setNumberOfSlots(int numberOfSlots) {
        this.numberOfSlots = numberOfSlots;
    }

    @Override
    public String toString() {
        return "[" + ip + ":" + port + "]";
    }
}