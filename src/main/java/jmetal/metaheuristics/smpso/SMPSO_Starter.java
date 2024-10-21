/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jmetal.metaheuristics.smpso;

import ro.ulbsibiu.fadse.SimulationBoot;

/**
 *
 * @author Radu
 */
public class SMPSO_Starter {
    public static void main(String[] args){
        String[] smpsoArgs = new String[3];
        smpsoArgs[0] = "falsesimin_radu.xml";
        smpsoArgs[1] = "D:\\Work\\FADSE\\results1303158955283\\filled1303159004530.csv";
        smpsoArgs[2] = "D:\\Work\\FADSE\\results1303158955283\\speed1303159004530.spd";
        SimulationBoot.main(smpsoArgs);
    }
}
