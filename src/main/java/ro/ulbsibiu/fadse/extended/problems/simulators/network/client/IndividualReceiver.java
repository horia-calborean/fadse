/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ulbsibiu.fadse.extended.problems.simulators.network.client;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ini4j.Wini;

import ro.ulbsibiu.fadse.environment.document.InputDocument;
import ro.ulbsibiu.fadse.extended.problems.SimulatorWrapper;
import ro.ulbsibiu.fadse.extended.problems.simulators.network.Message;
import ro.ulbsibiu.fadse.persistence.ConnectionPool;

/**
 *
 * @author Horia Calborean
 */
public class IndividualReceiver implements Runnable {

    ServerSocket serverSocket;
    private Thread simulationRunner;
    private ClientSimulatorRunner clientSimulatorRunner;
    public long connectionWaitStartTime;
    public boolean simulating = false;
    private int retries = 0;
    private static Random r = new Random(System.currentTimeMillis());

    public IndividualReceiver() throws IOException {
        String currentdir = System.getProperty("user.dir");
        File dir = new File(currentdir);
        Wini ini = new Wini(new File(dir + System.getProperty("file.separator") + "configs" + System.getProperty("file.separator") + "fadseConfig.ini"));
        init(ini.get("Client", "listenPort", int.class));
    }

    public IndividualReceiver(int port) throws IOException {
        init(port);
    }

    private void init(int port) throws IOException {
        try {
            Thread.sleep(r.nextInt(10000));
            serverSocket = new ServerSocket(port);
            InetAddress ip;
            try {
                ip = InetAddress.getLocalHost();
                System.out.println("<neighbor ip=\"" + ip.getHostAddress() + "\" listenPort=\"" + port + "\" availableSlots = \"1\" />");
            } catch (UnknownHostException e) {
                System.out.println("Client started on port:" + port);
                e.printStackTrace();
            }
        } catch (BindException ex) {
            if (retries < 1000) {
                retries++;
                init(port + retries);
            } else {
                throw ex;
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(IndividualReceiver.class.getName()).log(Level.SEVERE, "thread was intrerupted", ex);
        }


    }

    public void run() {
        //throw new UnsupportedOperationException("Not supported yet.");
        ObjectInputStream dis = null;
        ObjectOutputStream dos = null;
        Socket socket = null;
        Message m = null;
        SimulatorWrapper sim = null;
        int receivedIndividuals = 1;
        boolean simulationStart = false;
        while (true) {
            try {
                simulationStart = false;
                connectionWaitStartTime = System.currentTimeMillis();
                socket = serverSocket.accept();
                System.out.println("IndividualReceiver: Received ind- " + (receivedIndividuals++) + "");
//                System.out.println("IndividualReceiver: Received a message");
                dos = new ObjectOutputStream(socket.getOutputStream());
                dos.flush();
                dis = new ObjectInputStream(socket.getInputStream());

                // Logger.getLogger(IndividualReceiver.class.getName()).log(Level.INFO,"I'll try to read a Message...");
                m = (Message) dis.readObject();
                // Logger.getLogger(IndividualReceiver.class.getName()).log(Level.INFO,"Reading message worked.");

                m.setServerIP(socket.getInetAddress());
//                System.out.println("IndividualReceiver: Looking for simulator - " + m.getSimulatorName());
                //set the real name of the simualtor on this clent (in the environment will be written ServerSimualtor otherwise)
                m.getIndividual().getEnvironment().getInputDocument().setSimulatorName(m.getSimulatorName());
                //make unique names
                InputDocument inputDocument = m.getIndividual().getEnvironment().getInputDocument();
                for (String key : inputDocument.getSimulatorParameters().keySet()) {
                    String p = inputDocument.getSimulatorParameters().get(key);
                    inputDocument.getSimulatorParameters().put(key, p.replace("#", System.currentTimeMillis() + "_" + m.getMessageId()));
                }
                //end
                sim = SimulatorFactory.getSimulator(m.getSimulatorName(), m.getIndividual().getEnvironment());
                //send ack only if I have the requested type of simulator, else return err simulator not found
                if (sim == null) {
                    // Logger.getLogger(IndividualReceiver.class.getName()).log(Level.INFO,"TYPE_ERR_SIMULATOR_NOT_INSTALLED...");

                    m.setType(Message.TYPE_ERR_SIMULATOR_NOT_INSTALLED);
                    System.out.println("IndividualReceiver: Simulator NOT found");
                } else if (m.getType() == Message.TYPE_CLOSE_SIMULATION_REQUEST) {
                    // Logger.getLogger(IndividualReceiver.class.getName()).log(Level.INFO,"TYPE_CLOSE_SIMULATION_REQUEST...");

                    sim.closeSimulation(m.getIndividual());
                    m.setType(Message.TYPE_ACK);
                    // Logger.getLogger(IndividualReceiver.class.getName()).log(Level.INFO,"I'll try to send a response message.");
                    dos.writeObject(m);
                    dos.flush();
                    // Logger.getLogger(IndividualReceiver.class.getName()).log(Level.INFO,"I've send a response.");
                } else {
                    // Logger.getLogger(IndividualReceiver.class.getName()).log(Level.INFO,"ELSE...");

                    ConnectionPool.setInputDocument(m.getIndividual().getEnvironment().getInputDocument());
                    m.setType(Message.TYPE_ACK);

                    // Logger.getLogger(IndividualReceiver.class.getName()).log(Level.INFO,"I'll try to send a response message.");
                    dos.writeObject(m);
                    dos.flush();
                    // Logger.getLogger(IndividualReceiver.class.getName()).log(Level.INFO,"I've send a response.");
                    simulationStart = true;
                }
            } catch (IOException ex) {
                Logger.getLogger(IndividualReceiver.class.getName()).log(Level.SEVERE, "IOException", ex);
                simulationStart = false;
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(IndividualReceiver.class.getName()).log(Level.SEVERE, "ClassNotFoundException", ex);
                simulationStart = false;
            } finally {
                closeAllConnections(dis, dos, socket);
            }
            if (simulationStart) {
                Logger.getLogger(IndividualReceiver.class.getName()).log(Level.INFO, "Now I can start the simulation...");
                startSimulation(m, sim);
                Logger.getLogger(IndividualReceiver.class.getName()).log(Level.INFO, "I've finished the simulation (?)");
            }
        }
    }

    private void startSimulation(Message m, SimulatorWrapper sim) {
        //TODO start a thread then (be carefull not to start more simulations than this coputer is capable of) send Individual to simulator
        //For the time beeing make sure that only one thread is started
//        if(simulationRunner!=null && simulationRunner.isAlive()){
//            try {
//                Logger.getLogger(IndividualReceiver.class.getName()).log(Level.WARNING,"IndividualReceiver: the simulation is still running... joining");
//                simulationRunner.join();
//                connectionWaitStartTime = System.currentTimeMillis();
//            } catch (InterruptedException ex) {
//                Logger.getLogger(IndividualReceiver.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        System.out.println("Starting now the ClientSimulationRunner...");
        simulating = true;
        clientSimulatorRunner = new ClientSimulatorRunner(m.getIndividual(), sim, m);
        clientSimulatorRunner.run();
        simulating = false;
//        System.out.println("ClientSimulationRunner has finished its work.");
//        simulationRunner = new Thread(clientSimulatorRunner);
//        simulationRunner.setDaemon(true);
//        simulationRunner.start();
    }

    private void closeAllConnections(ObjectInputStream dis, ObjectOutputStream dos, Socket socket) {
        try {
            dos.close();
        } catch (IOException ex) {
            Logger.getLogger(IndividualReceiver.class.getName()).log(Level.SEVERE, "DOS could not be closed" + ex.getMessage(), ex);
        }
        try {
            dis.close();
        } catch (IOException ex) {
            Logger.getLogger(IndividualReceiver.class.getName()).log(Level.SEVERE, "DIS could not be closed" + ex.getMessage(), ex);
        }
        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(IndividualReceiver.class.getName()).log(Level.SEVERE, "Socket could not be closed" + ex.getMessage(), ex);
        }
    }
}
