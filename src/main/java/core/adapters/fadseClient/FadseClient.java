package core.adapters.fadseClient;

import core.ports.simulationClient.SimulationClient;

import java.net.InetAddress;

public class FadseClient implements SimulationClient {
    @Override
    public void setIp(InetAddress ip) {

    }

    @Override
    public InetAddress getIp() {
        return null;
    }

    @Override
    public void setPort(int port) {

    }

    @Override
    public int getPort() {
        return 0;
    }

    @Override
    public void setNoOfAvailableSlots(int noOfSlots) {

    }

    @Override
    public int getNoOfAvailableSlots() {
        return 0;
    }

    @Override
    public void setNoOfOccupiedSlots(int noOfSlots) {

    }

    @Override
    public int getNoOfOccupiedSlots() {
        return 0;
    }
}
