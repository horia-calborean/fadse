package core.model.clients;

import java.net.InetAddress;
import java.util.LinkedList;

public class ListOfFadseClients {
    protected LinkedList<FadseClientData> data;

    public ListOfFadseClients() {
        data = new LinkedList<>();
    }

    public void add(FadseClientData data) {
        this.data.add(data);
    }

    public int getSize() {
        return data.size();
    }

    public LinkedList<FadseClientData> getAll() {
        return data;
    }

    public FadseClientData getByIpAndPort(InetAddress ip, int port) {
        FadseClientData fadseClient = null;

        for (FadseClientData client : data) {
            try {
                if (client.getIP().equals(ip) && client.getPort() == port) {
                    fadseClient = client;
                    break;
                }
            } catch (NullPointerException e) {
                if (ip != null) {
                    System.out.println("Searching for: " + ip.getCanonicalHostName() + ":" + port);
                } else {
                    System.out.println("Inet address is null - happens if there was an error while communicating with the client");
                }
                System.out.println("Neighbors size = " + data.size());
                e.fillInStackTrace();
            }
        }
        return fadseClient;
    }

    public void addLast(FadseClientData n) {
        data.addLast(n);
    }

    public FadseClientData poll() {
        return data.poll();
    }
}