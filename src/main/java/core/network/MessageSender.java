package core.network;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import core.model.clients.FadseClientData;
import core.model.individual.FadseIndividual;
import input.model.setup.CommonSetupParameters;
import org.ini4j.Wini;

public class MessageSender {

    public static Message sendIndividual(FadseIndividual individual, FadseClientData n) throws Exception {
        MessageSender m = new MessageSender();

        return m.sendIndividual(individual, n, IdFactory.getId(), Message.TYPE_REQUEST);
    }

    public static Message sendIndividual(FadseIndividual individual, FadseClientData n, int type) throws Exception {
        MessageSender m = new MessageSender();
        return m.sendIndividual(individual, n, IdFactory.getId(), type);
    }

    public Message sendIndividual(FadseIndividual individual, FadseClientData n, String messageId, int type) throws Exception {
        Socket socket = new Socket(n.getIP(), n.getPort());
        ObjectOutputStream out;
        ObjectInputStream in = null;
        Message m = new Message();
        m.setIndividual(individual);
        m.setType(type);
        m.setMessageId(messageId);
        Map<String, String> problemConfigParameters = (Map<String, String>) individual.getInputData().get(CommonSetupParameters.PROBLEM_CONFIG);
        m.setSimulatorName(problemConfigParameters.get("realSimulator"));
        m.setClientListenPort(n.getPort());
        String currentDir = System.getProperty("user.dir");
        File dir = new File(currentDir);
        Wini ini = new Wini(new File(dir + FileSystems.getDefault().getSeparator() + "configs" + FileSystems.getDefault().getSeparator() + "fadseConfig.ini"));
        m.setServerIP(InetAddress.getByName(ini.get("Server", "ip")));
        m.setServerListenPort(ini.get("Server", "listenPort", int.class));
        out = new ObjectOutputStream(socket.getOutputStream());
        out.writeObject(m);
        out.flush();

        Message response;
        try {
            socket.setSoTimeout(60000);
            GetInputStream streamGetter = new GetInputStream(in, socket);
            Thread t = new Thread(streamGetter);
            t.start();

            t.join(60000);
            if (streamGetter.inputStream == null) {
                throw new IOException("Client: "+n+" has crashed, restart him");
            }
            in = streamGetter.inputStream;

            response = (Message) in.readObject();

            if (response.getType() != Message.TYPE_ACK) {
                if (response.getType() == Message.TYPE_ERR_SIMULATOR_NOT_INSTALLED) {
                    throw new Exception("Client responded, but the requested simulator is not installed on the client computer");
                } else {
                    throw new Exception("Client responded, but with bad response");
                }
            }

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MessageSender.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            out.close();
            try{
                assert in != null;
                in.close();}
            catch(Exception e){
                e.fillInStackTrace();
            }
            socket.close();
        }
        return m;
    }

    private static class GetInputStream implements Runnable {

        public ObjectInputStream inputStream;
        public Socket socket;

        public GetInputStream(ObjectInputStream inputStream, Socket socket) {
            this.inputStream = inputStream;
            this.socket = socket;
        }

        public void run() {
            try {
                inputStream = new ObjectInputStream(socket.getInputStream());
            } catch (IOException ex) {
                Logger.getLogger(MessageSender.class.getName()).log(Level.SEVERE, "MessageSender.run[1] "+ex.getMessage());
            }
        }
    }
}