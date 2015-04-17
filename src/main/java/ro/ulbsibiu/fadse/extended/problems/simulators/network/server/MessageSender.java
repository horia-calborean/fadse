/*
 * This file is part of the FADSE tool.
 * 
 *   Authors: Horia Andrei Calborean {horia.calborean at ulbsibiu.ro}, Andrei Zorila
 *   Copyright (c) 2009-2010
 *   All rights reserved.
 * 
 *   Redistribution and use in source and binary forms, with or without modification,
 *   are permitted provided that the following conditions are met:
 * 
 *   * Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 * 
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 * 
 *   The names of its contributors NOT may be used to endorse or promote products
 *   derived from this software without specific prior written permission.
 * 
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *   AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 *   THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *   PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 *   CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *   EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *   PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 *   OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 *   WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *   ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 *   OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package ro.ulbsibiu.fadse.extended.problems.simulators.network.server;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ini4j.Wini;

import ro.ulbsibiu.fadse.environment.Individual;
import ro.ulbsibiu.fadse.extended.problems.simulators.network.Message;

/**
 *
 * @author Horia Calborean
 */
public class MessageSender {

    public static Message sendIndividual(Individual individual, Neighbor n) throws UnknownHostException, IOException, Exception {
        MessageSender m = new MessageSender();

        return m.sendIndividual(individual, n, IdFactory.getId(), Message.TYPE_REQUEST);
    }

    public static Message sendIndividual(Individual individual, Neighbor n, int type) throws UnknownHostException, IOException, Exception {
        MessageSender m = new MessageSender();
        return m.sendIndividual(individual, n, IdFactory.getId(), type);
    }

    public Message sendIndividual(Individual individual, Neighbor n, String messageId, int type) throws UnknownHostException, IOException, Exception {
        Socket socket = new Socket(n.getIp(), n.getPort());
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        Message m = new Message();
        m.setIndividual(individual);
        m.setType(type);
        m.setMessageId(messageId);
        m.setSimulatorName(individual.getEnvironment().getInputDocument().getSimulatorParameter("realSimulator"));
        m.setClientListenport(n.getPort());
        String currentdir = System.getProperty("user.dir");
        File dir = new File(currentdir);
        Wini ini = new Wini(new File(dir + System.getProperty("file.separator") + "configs" + System.getProperty("file.separator") + "fadseConfig.ini"));
        m.setServerIP(InetAddress.getByName(ini.get("Server", "ip")));
        m.setServerListenPort(ini.get("Server", "listenPort", int.class));
        out = new ObjectOutputStream(socket.getOutputStream());
        out.writeObject(m);
        out.flush();
//        Logger.getLogger(MessageSender.class.getName()).log(Level.INFO,"Message Sent to : " + n.toString());
        Message response;
        try {
            socket.setSoTimeout(60000);//wait for 60 (was: 5) seconds for a response
            GetInputStream streamGetter = new GetInputStream(in, socket);
            Thread t = new Thread(streamGetter);
            t.start();
//            long currentTime = System.currentTimeMillis();
//            while (System.currentTimeMillis() - currentTime > 2000) {
//                if (streamGetter.inputStream != null) {
//                    break;
//                }
//            }
            t.join(60000);
            if (streamGetter.inputStream == null) {
                throw new IOException("Client: "+n+" has crashed, restart him");
            }
            in = streamGetter.inputStream;

            // Logger.getLogger(MessageSender.class.getName()).log(Level.INFO,"I'll wait now for a response...");
            response = (Message) in.readObject();
            // Logger.getLogger(MessageSender.class.getName()).log(Level.INFO,"I've got the response.");

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
            in.close();}
            catch(Exception e){
                e.printStackTrace();
            }
            socket.close();
        }
        return m;
    }

    private class GetInputStream implements Runnable {

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
