/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ro.ulbsibiu.fadse.extended.problems.simulators.network.server;

/**
 *
 * @author Horia Calborean
 */
public class IdFactory {
    private static long id = 0;
    public static synchronized String getId(){
        return ""+(id++)+"";
    }
}
