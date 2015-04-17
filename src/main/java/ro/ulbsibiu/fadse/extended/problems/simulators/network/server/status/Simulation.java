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

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

import ro.ulbsibiu.fadse.extended.problems.simulators.network.Message;
import ro.ulbsibiu.fadse.extended.problems.simulators.network.server.Neighbor;
import jmetal.base.Solution;

/**
 *
 * @author Horia Calborean
 */
public class Simulation{

    private String id;
    private Message message;
    private Solution solution;
    private Neighbor neighbor;
    private int retries;
    private List<Neighbor> pastClients;
    private boolean active;
    private Timestamp simulationStartedTime;

    public Simulation(String id, Message message, Solution solution, Neighbor neighbor, boolean active) {
        init(id, message, solution, neighbor, active);
    }
    /**
     * Creates n active simulation
     * @param id
     * @param message
     * @param solution
     * @param neighbor
     */
    public Simulation(String id, Message message, Solution solution, Neighbor neighbor) {
        init(id, message, solution, neighbor, true);
    }

    private void init(String id, Message message, Solution solution, Neighbor neighbor, boolean active) {
        this.id = id;
        this.message = message;
        this.solution = solution;
        this.neighbor = neighbor;
        this.active = active;
        this.pastClients = new LinkedList<Neighbor>();
        this.retries = 0;
        this.simulationStartedTime = new Timestamp(System.currentTimeMillis());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public Neighbor getNeighbor() {
        return neighbor;
    }

    public void setNeighbor(Neighbor neighbor) {
        this.neighbor = neighbor;
    }

    public Solution getSolution() {
        return solution;
    }

    public void setSolution(Solution solution) {
        this.solution = solution;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<Neighbor> getPastClients() {
        return pastClients;
    }

    public void setPastClients(List<Neighbor> pastClients) {
        this.pastClients = pastClients;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries++;
    }

    public void increaseRetries() {
        this.retries++;
    }

    public void changeNeighbor(Neighbor n) {
        pastClients.add(this.neighbor);
        this.neighbor = n;
    }

    public Timestamp getSimulationStartedTime() {
        return simulationStartedTime;
    }

    public void setSimulationStartedTime(Timestamp simulationStartedTime) {
        this.simulationStartedTime = simulationStartedTime;
    }

  
}
