/*
 * 
 *
 * This file is part of the FADSE tool.
 *
 *  Authors: Horia Andrei Calborean {horia.calborean at ulbsibiu.ro}, Andrei Zorila
 *  Copyright (c) 2009-2010
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification,
 *  are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *  The names of its contributors NOT may be used to endorse or promote products
 *  derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 *  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 *  OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 *  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 *  OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *
 */
package ro.ulbsibiu.fadse.extended.problems.simulators;

import java.util.LinkedList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.environment.Individual;
import ro.ulbsibiu.fadse.environment.Objective;

/**
 *
 * @author Horia Calborean <horia.calborean at ulbsibiu.ro>
 */
public class FalseSimulator extends SimulatorBase {

    public static final long serialVersionUID = 565464569930L;

    public FalseSimulator(Environment environment) throws ClassNotFoundException {

        super(environment);

    }

    @Override
    public void performSimulation(Individual ind) {
//        System.out.println("benchmark: "+ind.getBenchmark());
        Random r = new Random();
        try {
            Thread.sleep(10+r.nextInt(10) );//max 15 minutes time to complete a simulation but the wait time set in the XMl is 3
        } catch (InterruptedException ex) {
            Logger.getLogger(FalseSimulator.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        LinkedList<Objective> objectives = new LinkedList<Objective>();

        for (Objective o : environment.getInputDocument().getObjectives().values()) {
//            Objective result = new Objective(o.getName(), o.getType(), o.getUnit(), o.getDescription(), 1+r.nextInt(9), o.isMaximize());
            Objective result = new Objective(o.getName(), o.getType(), o.getUnit(), o.getDescription(), 5, o.isMaximize());
            objectives.add(result);
        }
        ind.setObjectives(objectives);
//        boolean infeasible = r.nextInt(100) > 80;
//        if (infeasible) {
//            ind.markAsInfeasibleAndSetBadValuesForObjectives("False simualtor decided it is bad 20% of the situations");
//        }

//        infeasible = r.nextInt(100) > 95;
//        if (infeasible) {
//            System.exit(0);
//        }
    }

    @Override
    public void closeSimulation(Individual individual) {
        System.out.println("I was killed because I have not responded fast enough");
    }
}
