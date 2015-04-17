/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ulbsibiu.fadse.extended.problems.simulators.network.server;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.ini4j.Wini;

import ro.ulbsibiu.fadse.extended.problems.simulators.network.Message;
import ro.ulbsibiu.fadse.extended.problems.simulators.network.server.status.SimulationStatus;

/**
 *
 * @author Horia Calborean
 */
public class ResultsReceiver implements Runnable {

    private ServerSocket serverSocket;
    public List<Message> results;
    private static ResultsReceiver instance;
    private SimulationStatus simulationStatus;

    private ResultsReceiver() throws IOException {
        simulationStatus = SimulationStatus.getInstance();
        results = Collections.synchronizedList(new LinkedList<Message>());
        String currentdir = System.getProperty("user.dir");
        File dir = new File(currentdir);
        Wini ini = new Wini(new File(dir + System.getProperty("file.separator") + "configs" + System.getProperty("file.separator") + "fadseConfig.ini"));
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
                Thread t = new Thread(new Runnable() {

                    public void run() {
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
                                //on received response remove element from curently simulating
                                simulationStatus.removeSimulation(response.getMessageId());
                                try {
                                    Logger.getLogger(ResultsReceiver.class.getName()).log(Level.INFO, "results size:[" + results.size() + "]; removed[" + socket.getInetAddress() + ":" + response.getClientListenport() + "-id:" + response.getMessageId() + ";still simulating " + simulationStatus.getNumberOfActiveSimulations() + " ind");
                                    Logger.getLogger(ResultsReceiver.class.getName()).log(Level.INFO, "the individual is: " + response.getIndividual());
                                } catch (Exception e) {//simulationStatus.getNumberOfActiveSimulations() might throw concurent modification exception
                                }
                                try {
                                    // utils.Utils.loadNeighbors(null)
                                    Neighbor n = Neighborhood.getInstance(response.getIndividual().getEnvironment().getNeighborsConfigFile()).getByIpAndPort(socket.getInetAddress(), response.getClientListenport());
                                    if (n != null) {
                                        n.setNumberOfOcupiedSlots(n.getNumberOfOcupiedSlots() - 1);
                                    } else {
                                        Logger.getLogger(ResultsReceiver.class.getName()).log(Level.SEVERE, "Received result from a client that is not in the neighborhood " + socket.getInetAddress() + ":" + socket.getPort());
                                    }
                                } catch (ParserConfigurationException ex) {
                                    Logger.getLogger(ResultsReceiver.class.getName()).log(Level.SEVERE, null, ex);
                                }
//                                Logger.getLogger(ResultsReceiver.class.getName()).log(Level.INFO, "sending back ACK");
                                response.setType(Message.TYPE_ACK);
                                out.writeObject(response);
                                out.flush();
//                                Logger.getLogger(ResultsReceiver.class.getName()).log(Level.INFO, "ACK sent");
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(ResultsReceiver.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ClassNotFoundException ex) {
                            Logger.getLogger(ResultsReceiver.class.getName()).log(Level.SEVERE, null, ex);
                        } finally {
                            try {
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
                    }
                });
                t.start();
            } catch (IOException ex) {
                Logger.getLogger(ResultsReceiver.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
//                try {
//                    socket.close();
//                } catch (IOException ex) {
//                    Logger.getLogger(ResultsReceiver.class.getName()).log(Level.SEVERE, null, ex);
//                }
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
