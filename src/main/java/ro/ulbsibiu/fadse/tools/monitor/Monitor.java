/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ro.ulbsibiu.fadse.tools.monitor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 *
 * @author Ralf
 */
public class Monitor {
    private static Monitor instance;
    private Socket socket;
    ObjectInputStream dis;
    ObjectOutputStream dos;

    private Monitor()  {
    }

    public static Monitor getInstance() {
        if (instance == null) {
            instance = new Monitor();
        }
        return instance;
    }

    private void connect(InetAddress address, int port) throws IOException {
        System.out.println("Trying to connect ...");

            System.out.println("address: "+ address);
            System.out.println("port: "+ port);
            socket = new Socket(address, port);
            System.out.println("Socket ok");
            dos = new ObjectOutputStream(socket.getOutputStream());
            dos.flush();
            dis = new ObjectInputStream(socket.getInputStream());
            System.out.println("Connected succesfully");
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }
    
    private void disconnect() {
        try {
        socket.close();
        dos.close();
        dis.close();
        } catch (Exception e) {
            System.out.println("Exception when closing...");
            e.printStackTrace();
        }
    }

    public String sendMessage(InetAddress ip, int port, String message) throws IOException, ClassNotFoundException {
        System.out.println("I will send " + message);
        connect(ip, port);
        System.out.println("connect done");
        dos.writeObject(message);
        System.out.println("Object written");
        dos.flush();
        System.out.println("flush done");
        String response = (String)dis.readObject();
        System.out.println("Response read: " + response);
                disconnect();
        return response;
    }

    public void checkConnection(InetAddress ip, int port) throws IOException {
        connect(ip, port);
        disconnect();
    }
}
