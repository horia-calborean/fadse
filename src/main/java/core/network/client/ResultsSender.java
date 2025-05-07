package core.network.client;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import core.model.individual.FadseIndividual;
import core.network.Message;

public class ResultsSender {

    public void send(FadseIndividual ind, Message m) throws IOException {
        ObjectOutputStream out = null;
        Socket socket = null;
        ObjectInputStream in = null;
        try {
            m.setIndividual(ind);
            m.setType(Message.TYPE_RESPONSE);
            InetAddress address = m.getServerIP();
            int port = m.getServerListenPort();
            System.out.println("ResultsSender: sending to -"+address+":"+port);
            socket = new Socket(address, port);

            out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(m);
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            Message response;
            socket.setSoTimeout(60000);//wait for 10 seconds for a response
            response = (Message) in.readObject();
            if(response.getType()==Message.TYPE_ACK && response.getMessageId().equals(m.getMessageId())){
//            System.out.println("ResultsSender: ACK received for message - " + response.getMessageId());
            } else {
                send(ind, m);
            }
        } catch (SocketTimeoutException ex){
            Logger.getLogger(ResultsSender.class.getName()).log(Level.WARNING, "Server did not send back the ACK response. Retrying", ex);
            send(ind, m);
        }catch(EOFException ex){
            Logger.getLogger(ResultsSender.class.getName()).log(Level.WARNING, "Server did not send back the ACK response correctly. Retrying", ex);
            send(ind, m);
        } catch (ClassNotFoundException | IOException ex) {
            Logger.getLogger(ResultsSender.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            assert out != null;
            out.close();
            assert in != null;
            in.close();
            socket.close();
        }
    }
}