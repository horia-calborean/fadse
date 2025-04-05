package core.model.clients;

import java.net.InetAddress;
import java.util.LinkedList;

public class ListOfFadseClients {
    protected LinkedList<FadseClient> clients;

    public ListOfFadseClients() {
        clients = new LinkedList<>();
    }

    public void add(FadseClient client) {
        clients.add(client);
    }

    public int getSize() {
        return clients.size();
    }

    public LinkedList<FadseClient> getAll() {
        return clients;
    }

    FadseClient getByIpAndPort(InetAddress inetAddress, int port) {
        FadseClient fadseClient = null;

        for (FadseClient client : clients) {
            try {
                if (client.getIP().equals(inetAddress) && client.getPort() == port) {
                    fadseClient = client;
                    break;
                }
            } catch (NullPointerException e) {
                if (inetAddress != null) {
                    System.out.println("Searching for: " + inetAddress.getCanonicalHostName() + ":" + port);
                } else {
                    System.out.println("Inet address is null - happens if there was an error while communicating with the client");
                }
                System.out.println("Neighbors size = " + clients.size());
                e.fillInStackTrace();
            }
        }
        return fadseClient;
    }
}