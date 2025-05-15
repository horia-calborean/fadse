package core.network;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import core.model.clients.FadseClientData;
import core.model.clients.ListOfFadseClients;
import input.model.InputData;
import input.model.setup.CommonSetupParameters;
import org.ini4j.Wini;

public class ResultsReceiver implements Runnable {

    private final ServerSocket serverSocket;
    public List<Message> results;
    private static ResultsReceiver instance;
    private final SimulationStatus simulationStatus;

    private ResultsReceiver() throws IOException {
        simulationStatus = SimulationStatus.getInstance();
        results = Collections.synchronizedList(new LinkedList<>());
        String currentDir = System.getProperty("user.dir");
        File dir = new File(currentDir);
        Wini ini = new Wini(new File(dir + FileSystems.getDefault().getSeparator() + "configs" + FileSystems.getDefault().getSeparator() + "fadseConfig.ini"));
        serverSocket = new ServerSocket(ini.get("Server", "listenPort", int.class));
        Logger.getLogger(ResultsReceiver.class.getName()).log(Level.CONFIG, "listening on port - " + ini.get("Server", "listenPort", int.class));
    }

    public static ResultsReceiver getInstance() throws IOException {
        if (instance == null) {
            instance = new ResultsReceiver();
            Thread t = new Thread(instance);
            t.setDaemon(true);
            t.start();
        }
        return instance;
    }

    public void run() {
        Logger.getLogger(ResultsReceiver.class.getName()).log(Level.INFO, "thread started");
//        System.out.println();
        while (true) {
            try {
                final Socket socket = serverSocket.accept();
                //when this has been reached a client has responded. Create a thread to communicate with the client
//                Logger.getLogger(ResultsReceiver.class.getName()).log(Level.INFO, "A client is sending the response");
                Thread t = new Thread(() -> {
                    ObjectOutputStream out = null;
                    ObjectInputStream in = null;
                    try {
                        out = new ObjectOutputStream(socket.getOutputStream());
                        out.flush();
                        in = new ObjectInputStream(socket.getInputStream());

                        Message response;
                        //socket.setSoTimeout(10000);//wait for 10 seconds for a response
                        response = (Message) in.readObject();
                        if (response.getType() != Message.TYPE_RESPONSE) {
                            Logger.getLogger(ResultsReceiver.class.getName()).log(Level.SEVERE, "Received message as a response but the TYPE is not response");
                        } else {
                            results.add(response);
                            //on received response remove element from currently simulating
                            simulationStatus.removeSimulation(response.getMessageId());
                            try {
                                Logger.getLogger(ResultsReceiver.class.getName()).log(Level.INFO, "results size:[" + results.size() + "]; removed[" + socket.getInetAddress() + ":" + response.getClientListenPort() + "-id:" + response.getMessageId() + ";still simulating " + simulationStatus.getNumberOfActiveSimulations() + " ind");
                                Logger.getLogger(ResultsReceiver.class.getName()).log(Level.INFO, "Server received results for individual: " + response.getIndividual());
                            } catch (Exception e) {//simulationStatus.getNumberOfActiveSimulations() might throw concurrent modification exception
                            }
                            // utils.Utils.loadNeighbors(null)
                            ListOfFadseClients fadseClients = ListOfFadseClients.getInstance();
                            FadseClientData n = fadseClients.getByIpAndPort(socket.getInetAddress(), response.getClientListenPort());
                            if (n != null) {
                                n.setNumberOfOccupiedSlots(n.getNumberOfOccupiedSlots() - 1);
                            } else {
                                Logger.getLogger(ResultsReceiver.class.getName()).log(Level.SEVERE, "Received result from a client that is not in the neighborhood " + socket.getInetAddress() + ":" + socket.getPort());
                            }
                            //                                Logger.getLogger(ResultsReceiver.class.getName()).log(Level.INFO, "sending back ACK");
                            response.setType(Message.TYPE_ACK);
                            out.writeObject(response);
                            out.flush();
//                                Logger.getLogger(ResultsReceiver.class.getName()).log(Level.INFO, "ACK sent");
                        }
                    } catch (IOException | ClassNotFoundException ex) {
                        Logger.getLogger(ResultsReceiver.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        try {
                            assert in != null;
                            in.close();

                        } catch (IOException ex) {
                            Logger.getLogger(ResultsReceiver.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        try {
                            out.close();
                        } catch (IOException ex) {
                            Logger.getLogger(ResultsReceiver.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        try {
                            socket.close();
                        } catch (IOException ex) {
                            Logger.getLogger(ResultsReceiver.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
                t.start();
            } catch (IOException ex) {
                Logger.getLogger(ResultsReceiver.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public List<Message> getResults() {
        return results;
    }

    public void clearResults() {
        results.clear();
    }
}
