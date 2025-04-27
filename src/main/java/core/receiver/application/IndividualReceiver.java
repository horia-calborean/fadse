package core.receiver.application;

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

public class IndividualReceiver implements Runnable {
    protected ServerSocket serverSocket;
    protected ClientSimulatorRunner clientSimulatorRunner;
    protected int retries = 0;
    protected static final Random random = new Random(System.currentTimeMillis());
    public long connectionWaitStartTime;
    public boolean simulating = false;

    public IndividualReceiver(int port) throws IOException {
        initSocket(port);
    }

    protected void initSocket(int port) throws IOException {
        try {
            Thread.sleep(random.nextInt(10000));
            serverSocket = new ServerSocket(port);
            InetAddress ip;
            try {
                ip = InetAddress.getLocalHost();
                System.out.println("<neighbor ip=\"" + ip.getHostAddress() + "\" listenPort=\"" + port + "\" availableSlots = \"1\" />");
            } catch (UnknownHostException e) {
                System.out.println("Client started on port:" + port);
                e.fillInStackTrace();
            }
        } catch (BindException ex) {
            if (retries < 1000) {
                retries++;
                initSocket(port + retries);
            } else {
                throw ex;
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(IndividualReceiver.class.getName()).log(Level.SEVERE, "thread was interrupted", ex);
        }
    }

    protected void startSimulation(Message m, SimulatorWrapper sim) {
        simulating = true;
        clientSimulatorRunner = new ClientSimulatorRunner(m.getIndividual(), sim, m);
        clientSimulatorRunner.run();
        simulating = false;
    }

    protected void closeAllConnections(ObjectInputStream inputStream, ObjectOutputStream outputStream, Socket socket) {
        try {
            outputStream.close();
        } catch (IOException ex) {
            Logger.getLogger(IndividualReceiver.class.getName()).log(Level.SEVERE, "Output stream could not be closed" + ex.getMessage(), ex);
        }
        try {
            inputStream.close();
        } catch (IOException ex) {
            Logger.getLogger(IndividualReceiver.class.getName()).log(Level.SEVERE, "Input stream could not be closed" + ex.getMessage(), ex);
        }
        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(IndividualReceiver.class.getName()).log(Level.SEVERE, "Socket could not be closed" + ex.getMessage(), ex);
        }
    }

    @Override
    public void run() {
        ObjectInputStream inputStream = null;
        ObjectOutputStream outputStream = null;
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
                System.out.println("IndividualReceiver: Received ind- " + (receivedIndividuals++));
                outputStream = new ObjectOutputStream(socket.getOutputStream());
                outputStream.flush();
                inputStream = new ObjectInputStream(socket.getInputStream());

                m = (Message) inputStream.readObject();

                m.setServerIP(socket.getInetAddress());

                m.getIndividual().getEnvironment().getInputDocument().setSimulatorName(m.getSimulatorName());

                InputDocument inputDocument = m.getIndividual().getEnvironment().getInputDocument();
                for (String key : inputDocument.getSimulatorParameters().keySet()) {
                    String p = inputDocument.getSimulatorParameters().get(key);
                    inputDocument.getSimulatorParameters().put(key, p.replace("#", System.currentTimeMillis() + "_" + m.getMessageId()));
                }

                sim = SimulatorFactory.getSimulator(m.getIndividual().getEnvironment());

                if (sim == null) {

                    m.setType(Message.TYPE_ERR_SIMULATOR_NOT_INSTALLED);
                    System.out.println("IndividualReceiver: Simulator NOT found");
                } else if (m.getType() == Message.TYPE_CLOSE_SIMULATION_REQUEST) {

                    sim.closeSimulation(m.getIndividual());
                    m.setType(Message.TYPE_ACK);

                    outputStream.writeObject(m);
                    outputStream.flush();
                } else {
                    ConnectionPool.setInputDocument(m.getIndividual().getEnvironment().getInputDocument());
                    m.setType(Message.TYPE_ACK);

                    outputStream.writeObject(m);
                    outputStream.flush();

                    simulationStart = true;
                }
            } catch (IOException ex) {
                Logger.getLogger(IndividualReceiver.class.getName()).log(Level.SEVERE, "IOException", ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(IndividualReceiver.class.getName()).log(Level.SEVERE, "ClassNotFoundException", ex);
            } finally {
                assert outputStream != null;
                closeAllConnections(inputStream, outputStream, socket);
            }
            if (simulationStart) {
                Logger.getLogger(IndividualReceiver.class.getName()).log(Level.INFO, "Now I can start the simulation...");
                startSimulation(m, sim);
                Logger.getLogger(IndividualReceiver.class.getName()).log(Level.INFO, "I've finished the simulation (?)");
            }
        }
    }
}