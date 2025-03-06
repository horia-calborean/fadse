package core.ports.simulationClient;

import java.net.InetAddress;

public interface SimulationClient {
    void setIp(InetAddress ip);
    InetAddress getIp();

    void setPort(int port);
    int getPort();

    void setNoOfAvailableSlots(int noOfSlots);
    int getNoOfAvailableSlots();

    void setNoOfOccupiedSlots(int noOfSlots);
    int getNoOfOccupiedSlots();
}