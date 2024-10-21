package ro.ulbsibiu.fadse.extended.problems.simulators.network.client;

import org.ini4j.Wini;
import ro.ulbsibiu.fadse.environment.document.InputDocument;
import ro.ulbsibiu.fadse.extended.problems.SimulatorWrapper;
import ro.ulbsibiu.fadse.extended.problems.simulators.network.Message;
import ro.ulbsibiu.fadse.persistence.ConnectionPool;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.nio.file.FileSystems;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FadseClient implements Runnable {
    private static final Random randomGen = new Random(System.currentTimeMillis());
    protected long connectionWaitStartTime;
    protected boolean simulating = false;
    private ServerSocket socket;
    private int retries = 0;

    public FadseClient() throws IOException {
        File currentDirectory = new File(System.getProperty("user.dir"));
        String fileSeparator = FileSystems.getDefault().getSeparator();
        Wini iniFile = new Wini(new File(currentDirectory + fileSeparator + "configs" + fileSeparator + "fadseConfig.ini"));

        initClient(iniFile.get("Client", "listenPort", int.class));
    }

    public FadseClient(int port) throws IOException {
        initClient(port);
    }

    private void initClient(int port) throws IOException {
        try {
            Thread.sleep(randomGen.nextInt(10000));
            socket = new ServerSocket(port);
            String ip;
            try {
                ip = InetAddress.getLocalHost().getHostAddress();
                System.out.println("<neighbor ip=\"" + ip + "\" listenPort=\"" + port + "\" availableSlots = \"1\" />");
            } catch (UnknownHostException e) {
                System.out.println("Client started on port:" + port);
                e.printStackTrace();
            }
        } catch (BindException exception) {
            if (retries < 1000) {
                retries++;
                initClient(port + retries);
            } else {
                throw exception;
            }
        } catch (InterruptedException exception) {
            Logger.getLogger(FadseClient.class.getName()).log(Level.SEVERE, "thread was intrerupted", exception);
        }
    }

    public long getConnectionStartTime() {
        return connectionWaitStartTime;
    }

    public boolean isSimulating() {
        return simulating;
    }

    public void run() {
        ObjectInputStream inputStream = null;
        ObjectOutputStream outputStream = null;
        Socket socketChannel = null;
        Message message = null;
        SimulatorWrapper simulator = null;
        int receivedIndividuals = 1;
        boolean simulationStart = false;

        while (true) {
            try {
                simulationStart = false;
                connectionWaitStartTime = System.currentTimeMillis();
                socketChannel = socket.accept();
                System.out.println("FADSE client: Received individual -> " + (receivedIndividuals++));

                outputStream = new ObjectOutputStream(socketChannel.getOutputStream());
                outputStream.flush();
                inputStream = new ObjectInputStream(socketChannel.getInputStream());

                message = (Message) inputStream.readObject();

                message.setServerIP(socketChannel.getInetAddress());

                message.getIndividual().getEnvironment().getInputDocument().setSimulatorName(message.getSimulatorName());

                InputDocument inputDocument = message.getIndividual().getEnvironment().getInputDocument();

                for (String key : inputDocument.getSimulatorParameters().keySet()) {
                    String p = inputDocument.getSimulatorParameters().get(key);
                    inputDocument.getSimulatorParameters().put(key, p.replace("#", System.currentTimeMillis() + "_" + message.getMessageId()));
                }

                simulator = SimulatorFactory.getSimulator(message.getSimulatorName(), message.getIndividual().getEnvironment());

                if (simulator == null) {
                    message.setType(Message.TYPE_ERR_SIMULATOR_NOT_INSTALLED);
                    System.out.println("FADSE Client: Simulator NOT found");
                } else if (message.getType() == Message.TYPE_CLOSE_SIMULATION_REQUEST) {
                    simulator.closeSimulation(message.getIndividual());
                    message.setType(Message.TYPE_ACK);
                    outputStream.writeObject(message);
                    outputStream.flush();
                } else {
                    ConnectionPool.setInputDocument(message.getIndividual().getEnvironment().getInputDocument());
                    message.setType(Message.TYPE_ACK);
                    outputStream.writeObject(message);
                    outputStream.flush();
                    simulationStart = true;
                }
            } catch (IOException ex) {
                Logger.getLogger(FadseClient.class.getName()).log(Level.SEVERE, "IOException", ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(FadseClient.class.getName()).log(Level.SEVERE, "ClassNotFoundException", ex);
            } finally {
                assert outputStream != null;
                closeAllConnections(inputStream, outputStream, socketChannel);
            }
            if (simulationStart) {
                Logger.getLogger(FadseClient.class.getName()).log(Level.INFO, "Now I can start the simulation...");
                startSimulation(message, simulator);
                Logger.getLogger(FadseClient.class.getName()).log(Level.INFO, "I've finished the simulation (?)");
            }
        }
    }

    private void startSimulation(Message message, SimulatorWrapper simulator) {
        simulating = true;
        ClientSimulatorRunner clientSimulatorRunner = new ClientSimulatorRunner(message.getIndividual(), simulator, message);
        clientSimulatorRunner.run();
        simulating = false;
    }

    private void closeAllConnections(ObjectInputStream inputStream, ObjectOutputStream outputStream, Socket socketChannel) {
        try {
            outputStream.close();
        } catch (IOException exception) {
            Logger.getLogger(FadseClient.class.getName()).log(Level.SEVERE, "Output stream could not be closed" + exception.getMessage(), exception);
        }
        try {
            inputStream.close();
        } catch (IOException exception) {
            Logger.getLogger(FadseClient.class.getName()).log(Level.SEVERE, "Input stream could not be closed" + exception.getMessage(), exception);
        }
        try {
            socketChannel.close();
        } catch (IOException exception) {
            Logger.getLogger(FadseClient.class.getName()).log(Level.SEVERE, "Socket channel could not be closed" + exception.getMessage(), exception);
        }
    }
}