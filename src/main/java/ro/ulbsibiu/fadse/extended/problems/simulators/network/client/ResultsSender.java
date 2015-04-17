/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ulbsibiu.fadse.extended.problems.simulators.network.client;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import ro.ulbsibiu.fadse.environment.Individual;
import ro.ulbsibiu.fadse.extended.problems.simulators.network.Message;

/**
 *
 * @author Horia Calborean
 */
public class ResultsSender {

    public void send(Individual ind, Message m) throws IOException {
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
//            System.out.println("ResultsSender: Waiting for ACK");
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
            Logger.getLogger(ResultsSender.class.getName()).log(Level.WARNING, "Server did not send back the ACK response. Retring", ex);
            send(ind, m);
        }catch(EOFException ex){
            Logger.getLogger(ResultsSender.class.getName()).log(Level.WARNING, "Server did not send back the ACK response corectly. Retring", ex);
            send(ind, m);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ResultsSender.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ResultsSender.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            out.close();
            in.close();
            socket.close();
        }
    }
}
