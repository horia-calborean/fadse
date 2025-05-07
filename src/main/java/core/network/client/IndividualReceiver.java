package core.network.client;

import core.network.ConnectionPool;
import core.network.Message;
import input.model.InputData;
import input.model.setup.CommonSetupParameters;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;
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

    public IndividualReceiver(int port) {
        initSocket(port);
    }

    protected void initSocket(int port) {
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
                System.out.println("Exception from Individual Receiver");
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(IndividualReceiver.class.getName()).log(Level.SEVERE, "thread was interrupted", ex);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void startSimulation(Message m, Simulator sim) {
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
        Message receivedMessage = null;
        Simulator simulator = null;
        int receivedIndividuals = 0;
        boolean isSimulationStarted = false;
        while (true) {
            try {
                isSimulationStarted = false;
                connectionWaitStartTime = System.currentTimeMillis();
                socket = serverSocket.accept();
                System.out.println("IndividualReceiver -> new individual -> count = " + (++receivedIndividuals));
                outputStream = new ObjectOutputStream(socket.getOutputStream());
                outputStream.flush();
                inputStream = new ObjectInputStream(socket.getInputStream());

                receivedMessage = (Message) inputStream.readObject();
                receivedMessage.setServerIP(socket.getInetAddress());
                // TODO - Commented out by Andrei
                //receivedMessage.getIndividual().getInputData().getInputDocument().setSimulatorName(receivedMessage.getSimulatorName());

                InputData inputData = receivedMessage.getIndividual().getInputData();
                Map<String, String> problemParameters = (Map<String, String>) inputData.get(CommonSetupParameters.PROBLEM_CONFIG);
                for (String key : problemParameters.keySet()) {
                    String p = problemParameters.get(key);
                    problemParameters.put(key, p.replace("#", System.currentTimeMillis() + "_" + receivedMessage.getMessageId()));
                }

                simulator = SimulatorFactory.createSimulator(inputData);

                if (simulator == null) {

                    receivedMessage.setType(Message.TYPE_ERR_SIMULATOR_NOT_INSTALLED);
                    System.out.println("IndividualReceiver: Simulator NOT found");
                } else if (receivedMessage.getType() == Message.TYPE_CLOSE_SIMULATION_REQUEST) {

                    simulator.closeSimulation(receivedMessage.getIndividual());
                    receivedMessage.setType(Message.TYPE_ACK);

                    outputStream.writeObject(receivedMessage);
                    outputStream.flush();
                } else {
                    Map<String, String> dbConnectionData = (Map<String, String>) inputData.get(CommonSetupParameters.DATABASE);
                    ConnectionPool.setInputDocument(dbConnectionData);
                    receivedMessage.setType(Message.TYPE_ACK);

                    outputStream.writeObject(receivedMessage);
                    outputStream.flush();

                    isSimulationStarted = true;
                }
            } catch (IOException ex) {
                Logger.getLogger(IndividualReceiver.class.getName()).log(Level.SEVERE, "IOException", ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(IndividualReceiver.class.getName()).log(Level.SEVERE, "ClassNotFoundException", ex);
            } finally {
                assert outputStream != null;
                closeAllConnections(inputStream, outputStream, socket);
            }
            if (isSimulationStarted) {
                Logger.getLogger(IndividualReceiver.class.getName()).log(Level.INFO, "Now I can start the simulation...");
                startSimulation(receivedMessage, simulator);
                Logger.getLogger(IndividualReceiver.class.getName()).log(Level.INFO, "I've finished the simulation (?)");
            }
        }
    }
}