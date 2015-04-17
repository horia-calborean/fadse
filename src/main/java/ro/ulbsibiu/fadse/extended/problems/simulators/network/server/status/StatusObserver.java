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
package ro.ulbsibiu.fadse.extended.problems.simulators.network.server.status;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import jmetal.base.Solution;
import jmetal.base.SolutionSet;
import jmetal.util.Ranking;

import org.ini4j.Wini;

import ro.ulbsibiu.fadse.utils.Utils;

/**
 *
 * @author Horia Calborean
 */
public class StatusObserver implements Runnable {

    private SimulationStatus simulationStatus;
    private Utils u;

    public StatusObserver(SimulationStatus simulationStatus) {
        this.simulationStatus = simulationStatus;
        u = new Utils();
    }
    private ServerSocket serverSocket;

    public void run() {
        ObjectInputStream dis;
        ObjectOutputStream dos;
        try {

            String currentdir = System.getProperty("user.dir");
            File dir = new File(currentdir);
            Wini ini = new Wini(new File(dir + System.getProperty("file.separator") + "configs" + System.getProperty("file.separator") + "fadseConfig.ini"));
            serverSocket = new ServerSocket(ini.get("Monitor", "listenPort", int.class));
            Logger.getLogger(StatusObserver.class.getName()).log(Level.CONFIG, "Monitor Started ... listening on port: " + ini.get("Monitor", "listenPort", int.class));
            while (true) {
                try {
                    String result = "";
                    Logger.getLogger(StatusObserver.class.getName()).log(Level.INFO, "StatusObserver: Waiting for connection");
                    Socket socket = serverSocket.accept();
                    Logger.getLogger(StatusObserver.class.getName()).log(Level.INFO, "Statusobserver: connected");
                    dos = new ObjectOutputStream(socket.getOutputStream());
                    dos.flush();
                    dis = new ObjectInputStream(socket.getInputStream());
//                dos.writeObject(((JSONObject) JSONSerializer.toJSON( simualtionStatus.getSentMessages() )));//TODO switch to String and use JSON or XML
                    String command = (String) dis.readObject();
                    try {
                        Logger.getLogger(StatusObserver.class.getName()).log(Level.INFO,"Statusobserver: read command -> " + command);
                        if (command.equalsIgnoreCase("nrActiveSimulations")) {
                            result = Integer.toString(simulationStatus.getNumberOfActiveSimulations());

                        } else if (command.equalsIgnoreCase("activeSimulationsList")) {
                            for (String id : simulationStatus.getActiveSimulationsIds()) {
                                result += id + "#";
                            }
                        } else if (command.startsWith("startTimeOf:")) {
                            String id = command.substring(command.indexOf(":") + 1);
                            Simulation sim = simulationStatus.getSimulation(id);
                            result = Long.toString(sim.getSimulationStartedTime().getTime());
                        } else if (command.startsWith("runningTimeOf:")) {
                            String id = command.substring(command.indexOf(":") + 1);
                            Simulation sim = simulationStatus.getSimulation(id);
                            result = Float.toString((System.currentTimeMillis() - sim.getSimulationStartedTime().getTime()) / (1000 * 60)) + " min";
                        } else if (command.startsWith("clientIpOf:")) {
                            String id = command.substring(command.indexOf(":") + 1);
                            Simulation sim = simulationStatus.getSimulation(id);
                            result = sim.getNeighbor().getIp().getHostAddress();
                        } else if (command.startsWith("runningMoreThan:")) {
                            String minutes = command.substring(command.indexOf(":") + 1);
                            for (String id : simulationStatus.getActiveSimulationsIds()) {
                                Simulation sim = simulationStatus.getSimulation(id);
                                long simTime = (System.currentTimeMillis() - sim.getSimulationStartedTime().getTime()) / (1000 * 60);
                                long maxTime = Long.parseLong(minutes);
                                if (maxTime < simTime) {
                                    result += id + "#";
                                }
                            }
                        } else if (command.startsWith("getCurrentPop")) {

                            SolutionSet solutionSet = u.insertObjectivesValuesIntoSolutions(simulationStatus);

                            String headder = u.generateCSVHeadder(simulationStatus.getEnvironment());
                            result = headder;
                            result += u.generateCSV(solutionSet);
                        } else if (command.startsWith("getCurrentOptimalSet")) {
                            SolutionSet solutionSet =u.insertObjectivesValuesIntoSolutions(simulationStatus);
                            String headder = u.generateCSVHeadder(simulationStatus.getEnvironment());
                            result = headder;
                            SolutionSet finalSolutionSet = new SolutionSet(solutionSet.size());
                            Iterator i = solutionSet.iterator();
                            while (i.hasNext()) {
                                Solution s = (Solution) i.next();
                                if (s.getNumberOfViolatedConstraint() == 0) {
                                    finalSolutionSet.add(s);
                                }
                            }
                            Ranking r = new Ranking(finalSolutionSet);
                            result += u.generateCSV(r.getSubfront(0));
                        } else {
                            result = "Error: unknown command";
                            //TODO delete simulation X , stop client and mark ind as infeasible
                        }
                    } catch (Exception e) {
                        for (StackTraceElement ste : e.getStackTrace()) {
                            result = ste.toString() + "\n" + result;
                        }

                        result = "## EXCEPTION " + e.getClass() + " - " + e.getMessage() + "##\n" + result;

                        e.printStackTrace();
                    }
                    Logger.getLogger(StatusObserver.class.getName()).log(Level.INFO,"StatusObserver: writing result: " + result);
                    dos.writeObject(result);
                    dos.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(SimulationStatus.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
